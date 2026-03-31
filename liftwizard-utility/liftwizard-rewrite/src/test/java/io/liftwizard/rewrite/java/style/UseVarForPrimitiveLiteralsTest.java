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

package io.liftwizard.rewrite.java.style;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

class UseVarForPrimitiveLiteralsTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new UseVarForPrimitiveLiterals()).allSources((src) -> src.markers(javaVersion(17)));
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
						void test() {
							// int literal
							int i = 42;

							// long literal with suffix
							long l1 = 42L;

							// long literal without suffix - needs L added
							long l2 = 42;

							// float literal with suffix
							float f1 = 3.14F;

							// float literal with lowercase suffix
							float f2 = 3.14f;

							// double literal with decimal point
							double d1 = 3.14;

							// double literal with suffix
							double d2 = 42D;

							// double literal without suffix or decimal - needs D added
							double d3 = 42;

							// boolean literals
							boolean b1 = true;
							boolean b2 = false;

							// char literal
							char c = 'a';

							// String literal
							String s = "hello";
						}
					}
					""",
					"""
					class Test {
						void test() {
							// int literal
							var i = 42;

							// long literal with suffix
							var l1 = 42L;

							// long literal without suffix - needs L added
							var l2 = 42L;

							// float literal with suffix
							var f1 = 3.14F;

							// float literal with lowercase suffix
							var f2 = 3.14f;

							// double literal with decimal point
							var d1 = 3.14;

							// double literal with suffix
							var d2 = 42D;

							// double literal without suffix or decimal - needs D added
							var d3 = 42D;

							// boolean literals
							var b1 = true;
							var b2 = false;

							// char literal
							var c = 'a';

							// String literal
							var s = "hello";
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
					class Test {
						// Field declarations - var not allowed
						private int fieldInt = 42;
						private String fieldStr = "hello";

						void test() {
							// Method return - type not obvious from literal
							int fromMethod = Integer.parseInt("42");
							boolean fromMethodBool = "hello".isEmpty();
							String fromMethodStr = String.valueOf(42);

							// byte - looks like int literal
							byte b = 42;

							// short - looks like int literal
							short s = 42;

							// Already using var
							var existing = 42;

							// No initializer
							int noInit;

							// Multiple variables
							int a = 1, x = 2;

							// Null initializer
							String nullInit = null;

							// Ternary initializer (still a literal check, but ternary is not a literal)
							int ternary = true ? 1 : 2;

							// Constructor call (not a literal)
							StringBuilder sb = new StringBuilder();
						}
					}
					"""
				)
			);
	}
}
