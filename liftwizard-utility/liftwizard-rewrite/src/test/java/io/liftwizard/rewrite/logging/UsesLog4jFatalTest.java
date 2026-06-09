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

class UsesLog4jFatalTest implements RewriteTest {

	private static final String LOG4J_CATEGORY_STUB = """
		package org.apache.log4j;
		public class Category {
		    public static Logger getLogger(Class clazz) { return null; }
		    public void debug(Object message) {}
		    public void info(Object message) {}
		    public void warn(Object message) {}
		    public void error(Object message) {}
		    public void fatal(Object message) {}
		    public void fatal(Object message, Throwable t) {}
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
			.recipe(new UsesLog4jFatal())
			.parser(JavaParser.fromJavaVersion().dependsOn(LOG4J_CATEGORY_STUB, LOG4J_LOGGER_STUB));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.apache.log4j.Logger;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void detectsStringLiteral() {
					        LOGGER.fatal("Simple message");
					    }

					    void detectsStringVariable(String message) {
					        LOGGER.fatal(message);
					    }

					    void detectsObjectArgument(Object myObject) {
					        LOGGER.fatal(myObject);
					    }

					    void detectsThrowableArgument(Exception exception) {
					        LOGGER.fatal("Failure", exception);
					    }
					}
					""",
					"""
					import org.apache.log4j.Logger;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void detectsStringLiteral() {
					        /*~~>*/LOGGER.fatal("Simple message");
					    }

					    void detectsStringVariable(String message) {
					        /*~~>*/LOGGER.fatal(message);
					    }

					    void detectsObjectArgument(Object myObject) {
					        /*~~>*/LOGGER.fatal(myObject);
					    }

					    void detectsThrowableArgument(Exception exception) {
					        /*~~>*/LOGGER.fatal("Failure", exception);
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

					class CustomLogger {
					    void fatal(String message) {}
					}

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);
					    private final CustomLogger custom = new CustomLogger();

					    void doesNotDetectOtherLevels(String message) {
					        LOGGER.debug(message);
					        LOGGER.info(message);
					        LOGGER.warn(message);
					        LOGGER.error(message);
					    }

					    void doesNotDetectUnrelatedFatal(String message) {
					        custom.fatal(message);
					    }
					}
					"""
				)
			);
	}
}
