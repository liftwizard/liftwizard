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

class EnforceConsistentMethodInvocationArgumentLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentMethodInvocationArgumentLineWrapping());
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
					    void method5(String a, String b, String c, String d, String e) {}
					    void method6(String a, String b, String c, String d, String e, String f) {}
					    void method7(String a, String b, String c, String d, String e, String f, String g) {}
					    void method10(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j) {}

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
					        method6("a", "b",
					                "c", "d",
					                "e", "f");
					        method5(
					                "a", "b",
					                "c", "d",
					                "e");
					        method7(
					                "a", "b",
					                "c", "d",
					                "e", "f", "g");
					        method10(
					                "a", "b", "c",
					                "d", "e", "f",
					                "g", "h", "i", "j");
					    }
					}""",
					"""
					class Test {
					    void method(String a, String b, String c) {}
					    String methodReturning(String a, String b) { return a; }
					    void method2(String a, String b) {}
					    void method5(String a, String b, String c, String d, String e) {}
					    void method6(String a, String b, String c, String d, String e, String f) {}
					    void method7(String a, String b, String c, String d, String e, String f, String g) {}
					    void method10(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j) {}

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
					        method6(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e",
					                "f");
					        method5(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e");
					        method7(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e",
					                "f",
					                "g");
					        method10(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e",
					                "f",
					                "g",
					                "h",
					                "i",
					                "j");
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
					    void method4(String a, String b, String c, String d) {}
					    void method6(String a, String b, String c, String d, String e, String f) {}
					    void method8(String a, String b, String c, String d, String e, String f, String g, String h) {}
					    void method9(String a, String b, String c, String d, String e, String f, String g, String h, String i) {}

					    void test() {
					        method("first", "second", "third");
					        method(
					                "first",
					                "second",
					                "third");
					        method1("first");
					        method1(
					                "first");
					        method6(
					                "a", "b",
					                "c", "d",
					                "e", "f");
					        method6(
					                "a", "b", "c",
					                "d", "e", "f");
					        method4(
					                "a", "b",
					                "c", "d");
					        method9(
					                "a", "b", "c",
					                "d", "e", "f",
					                "g", "h", "i");
					        method8(
					                "a", "b", "c", "d",
					                "e", "f", "g", "h");
					    }
					}"""
				)
			);
	}
}
