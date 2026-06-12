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
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UsesLog4jFatalTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new UsesLog4jFatal())
			.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "reload4j"));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
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
					import org.apache.log4j.Level;
					import org.apache.log4j.Logger;

					import java.util.Optional;

					class Test {
					    private static final Logger LOGGER = Logger.getLogger(Test.class);

					    void fatalStringLiteral() {
					        /*~~>*/LOGGER.fatal("Simple message");
					    }

					    void fatalStringVariable(String message) {
					        /*~~>*/LOGGER.fatal(message);
					    }

					    void fatalObjectArgument(Object myObject) {
					        /*~~>*/LOGGER.fatal(myObject);
					    }

					    void fatalThrowableArgument(Exception exception) {
					        /*~~>*/LOGGER.fatal("Failure", exception);
					    }

					    void fatalMethodReference(Optional<Object> value) {
					        value.ifPresent(/*~~>*/LOGGER::fatal);
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
