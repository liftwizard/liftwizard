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
		    public void log(Priority priority, Object message) {}
		    public void log(Priority priority, Object message, Throwable t) {}
		}
		""";

	private static final String LOG4J_LOGGER_STUB = """
		package org.apache.log4j;
		public class Logger extends Category {
		    public static Logger getLogger(Class clazz) { return null; }
		}
		""";

	private static final String LOG4J_PRIORITY_STUB = """
		package org.apache.log4j;
		public class Priority {
		    public static final Priority FATAL = null;
		    public static final Priority ERROR = null;
		}
		""";

	private static final String LOG4J_LEVEL_STUB = """
		package org.apache.log4j;
		public class Level extends Priority {
		    public static final Level FATAL = null;
		    public static final Level ERROR = null;
		    public static final Level DEBUG = null;
		}
		""";

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new UsesLog4jFatal())
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					LOG4J_CATEGORY_STUB,
					LOG4J_LOGGER_STUB,
					LOG4J_PRIORITY_STUB,
					LOG4J_LEVEL_STUB
				)
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				// fatal method usage marks the compilation unit (UsesMethod), while the Level.FATAL
				// constant is marked in place; the Preconditions.or composition surfaces these across
				// two cycles when both appear in one file.
				(spec) -> spec.expectedCyclesThatMakeChanges(2),
				java(
					"""
					import org.apache.log4j.Level;
					import org.apache.log4j.Logger;

					import java.util.Optional;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void fatalStringLiteral() {
					        LOGGER.fatal("Simple message");
					    }

					    void fatalStringVariable(String message) {
					        LOGGER.fatal(message);
					    }

					    void fatalObjectArgument(Object myObject) {
					        LOGGER.fatal(myObject);
					    }

					    void fatalThrowableArgument(Exception exception) {
					        LOGGER.fatal("Failure", exception);
					    }

					    void fatalMethodReference(Optional<Object> value) {
					        value.ifPresent(LOGGER::fatal);
					    }

					    void bareLevelFatalConstant() {
					        Level level = Level.FATAL;
					    }

					    void logAtFatalLevel() {
					        LOGGER.log(Level.FATAL, "boom");
					    }
					}
					""",
					"""
					/*~~>*/import org.apache.log4j.Level;
					import org.apache.log4j.Logger;

					import java.util.Optional;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void fatalStringLiteral() {
					        LOGGER.fatal("Simple message");
					    }

					    void fatalStringVariable(String message) {
					        LOGGER.fatal(message);
					    }

					    void fatalObjectArgument(Object myObject) {
					        LOGGER.fatal(myObject);
					    }

					    void fatalThrowableArgument(Exception exception) {
					        LOGGER.fatal("Failure", exception);
					    }

					    void fatalMethodReference(Optional<Object> value) {
					        value.ifPresent(LOGGER::fatal);
					    }

					    void bareLevelFatalConstant() {
					        Level level = /*~~>*/Level.FATAL;
					    }

					    void logAtFatalLevel() {
					        LOGGER.log(/*~~>*/Level.FATAL, "boom");
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
					import org.apache.log4j.Level;
					import org.apache.log4j.Logger;

					import java.util.Optional;

					class CustomLogger {
					    void fatal(String message) {}
					}

					class CustomLevel {
					    public static final String FATAL = "FATAL";
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

					    void doesNotDetectOtherLevelConstants() {
					        Level error = Level.ERROR;
					        Level debug = Level.DEBUG;
					        LOGGER.log(Level.ERROR, "oops");
					    }

					    void doesNotDetectOtherLevelMethodReference(Optional<Object> value) {
					        value.ifPresent(LOGGER::error);
					    }

					    void doesNotDetectUnrelatedFatal(String message) {
					        custom.fatal(message);
					    }

					    void doesNotDetectUnrelatedFatalConstant() {
					        String fatal = CustomLevel.FATAL;
					    }
					}
					"""
				)
			);
	}
}
