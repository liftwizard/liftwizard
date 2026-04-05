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

class ParameterizedLoggingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new ParameterizedLogging("org.slf4j.Logger info(..)", null))
			.parser(JavaParser.fromJavaVersion().classpath("slf4j-api"));
	}

	@DocumentExample
	@Test
	void stringConcatenationToParameterized() {
		this.rewriteRun(
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String name) {
					        LOGGER.info("Hello " + name);
					    }
					}
					""",
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String name) {
					        LOGGER.info("Hello {}", name);
					    }
					}
					"""
				)
			);
	}

	@Test
	void multipleVariablesConcatenated() {
		this.rewriteRun(
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String first, String last) {
					        LOGGER.info("User " + first + " " + last + " logged in");
					    }
					}
					""",
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String first, String last) {
					        LOGGER.info("User {} {} logged in", first, last);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doesNotTransformStringArgument() {
		this.rewriteRun(
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test() {
					        LOGGER.info("Simple message");
					    }
					}
					"""
				)
			);
	}

	@Test
	void doesNotTransformAlreadyParameterized() {
		this.rewriteRun(
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String name) {
					        LOGGER.info("Hello {}", name);
					    }
					}
					"""
				)
			);
	}

	@Test
	void concatenationWithThrowableAsLastArg() {
		this.rewriteRun(
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String name, Exception e) {
					        LOGGER.info("Error for " + name, e);
					    }
					}
					""",
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(String name, Exception e) {
					        LOGGER.info("Error for {}", name, e);
					    }
					}
					"""
				)
			);
	}

	@Test
	void removeToStringWhenEnabled() {
		this.rewriteRun(
				(spec) -> spec.recipe(new ParameterizedLogging("org.slf4j.Logger info(..)", true)),
				java(
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(Object obj) {
					        LOGGER.info("Value: " + obj.toString());
					    }
					}
					""",
					"""
					import org.slf4j.Logger;
					import org.slf4j.LoggerFactory;

					class Test {
					    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

					    void test(Object obj) {
					        LOGGER.info("Value: {}", obj);
					    }
					}
					"""
				)
			);
	}
}
