/*
 * Copyright 2025 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.rewrite.logging;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UsesLog4j1ObjectLoggingTest implements RewriteTest {

	private static final String LOG4J_CATEGORY_STUB = """
		package org.apache.log4j;
		public class Category {
		    public static Logger getLogger(Class clazz) { return null; }
		    public void debug(Object message) {}
		    public void info(Object message) {}
		    public void warn(Object message) {}
		    public void error(Object message) {}
		    public void error(Object message, Throwable t) {}
		    public void fatal(Object message) {}
		}
		""";

	private static final String LOG4J_LOGGER_STUB = """
		package org.apache.log4j;
		public class Logger extends Category {
		    public static Logger getLogger(Class clazz) { return null; }
		}
		""";

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new UsesLog4j1ObjectLogging())
			.parser(JavaParser.fromJavaVersion().dependsOn(LOG4J_CATEGORY_STUB, LOG4J_LOGGER_STUB));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.apache.log4j.Logger;

					class MyEvent {
					    String name;
					}

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void detectsObjectArgument(Object myObject) {
					        LOGGER.info(myObject);
					    }

					    void detectsCustomTypeArgument(MyEvent event) {
					        LOGGER.info(event);
					    }

					    void detectsAcrossLogLevels(Object obj) {
					        LOGGER.debug(obj);
					        LOGGER.warn(obj);
					        LOGGER.error(obj);
					        LOGGER.fatal(obj);
					    }
					}
					""",
					"""
					import org.apache.log4j.Logger;

					class MyEvent {
					    String name;
					}

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void detectsObjectArgument(Object myObject) {
					        /*~~>*/LOGGER.info(myObject);
					    }

					    void detectsCustomTypeArgument(MyEvent event) {
					        /*~~>*/LOGGER.info(event);
					    }

					    void detectsAcrossLogLevels(Object obj) {
					        /*~~>*/LOGGER.debug(obj);
					        /*~~>*/LOGGER.warn(obj);
					        /*~~>*/LOGGER.error(obj);
					        /*~~>*/LOGGER.fatal(obj);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.apache.log4j.Logger;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void doesNotDetectStringLiteral() {
					        LOGGER.info("Simple message");
					    }

					    void doesNotDetectStringVariable(String message) {
					        LOGGER.info(message);
					    }

					    void doesNotDetectStringConcatenation(String name) {
					        LOGGER.info("Hello " + name);
					    }

					    void doesNotDetectThrowables(Exception exception) {
					        LOGGER.error(exception);
					        LOGGER.error(exception.getClass().getName(), exception);
					    }
					}
					"""
				)
			);
	}

	@Test
	void detectsObjectArgumentWhenLoggerInheritanceIsSevered() {
		String severedLoggerStub = """
			package org.apache.log4j;
			public class Logger {
			    public static Logger getLogger(Class clazz) { return null; }
			    public void debug(Object message) {}
			    public void info(Object message) {}
			    public void warn(Object message) {}
			    public void error(Object message) {}
			    public void fatal(Object message) {}
			}
			""";

		this.rewriteRun(
				(spec) -> spec.parser(JavaParser.fromJavaVersion().dependsOn(severedLoggerStub)),
				java(
					"""
					import org.apache.log4j.Logger;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void test(Object myObject) {
					        LOGGER.info(myObject);
					    }
					}
					""",
					"""
					import org.apache.log4j.Logger;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void test(Object myObject) {
					        /*~~>*/LOGGER.info(myObject);
					    }
					}
					"""
				)
			);
	}
}
