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

class EnforceConsistentArgumentLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentArgumentLineWrapping());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
					    void method(String a, String b, String c) {}
					    String methodReturning(String a, String b) { return a; }
					    void method2(String a, String b) {}

					    void test() {
					        method("first",
					                "second",
					                "third");
					        method2("first",
					                "second");
					        method(
					                "first", "second", "third");
					        methodReturning("first",
					                "second").toString();
					    }
					}""",
					"""
					class Test {
					    void method(String a, String b, String c) {}
					    String methodReturning(String a, String b) { return a; }
					    void method2(String a, String b) {}

					    void test() {
					        method(
					                "first",
					                "second",
					                "third");
					        method2(
					                "first",
					                "second");
					        method(
					                "first",
					                "second",
					                "third");
					        methodReturning(
					                "first",
					                "second").toString();
					    }
					}"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
					    void method(String a, String b, String c) {}
					    void method1(String a) {}

					    void test() {
					        method("first", "second", "third");
					        method(
					                "first",
					                "second",
					                "third");
					        method1("first");
					        method1(
					                "first");
					    }
					}"""
				)
			);
	}
}
