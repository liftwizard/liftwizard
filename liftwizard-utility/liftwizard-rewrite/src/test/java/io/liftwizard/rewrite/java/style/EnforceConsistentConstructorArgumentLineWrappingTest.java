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

class EnforceConsistentConstructorArgumentLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentConstructorArgumentLineWrapping());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					class Foo {
					    Foo(String a, String b, String c) {}
					    Foo(String a, String b) {}
					    Foo(String a, String b, String c, String d, String e) {}
					    Foo(String a, String b, String c, String d, String e, String f, String g) {}

					    void test() {
					        new Foo("first",
					                "second",
					                "third");
					        new Foo("first",
					                "second");
					        new Foo(
					                "first", "second", "third");
					        new Foo(
					                "a", "b",
					                "c", "d",
					                "e");
					        new Foo(
					                "a", "b",
					                "c", "d",
					                "e", "f", "g");
					    }
					}""",
					"""
					class Foo {
					    Foo(String a, String b, String c) {}
					    Foo(String a, String b) {}
					    Foo(String a, String b, String c, String d, String e) {}
					    Foo(String a, String b, String c, String d, String e, String f, String g) {}

					    void test() {
					        new Foo(
					                "first",
					                "second",
					                "third");
					        new Foo(
					                "first",
					                "second");
					        new Foo(
					                "first",
					                "second",
					                "third");
					        new Foo(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e");
					        new Foo(
					                "a",
					                "b",
					                "c",
					                "d",
					                "e",
					                "f",
					                "g");
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
					class Foo {
					    Foo(String a, String b, String c) {}
					    Foo(String a) {}
					    Foo(String a, String b, String c, String d, String e, String f) {}
					    Foo(String a, String b, String c, String d, String e, String f, String g, String h) {}
					    Foo(String a, String b, String c, String d, String e, String f, String g, String h, String i) {}

					    void test() {
					        new Foo("first", "second", "third");
					        new Foo(
					                "first",
					                "second",
					                "third");
					        new Foo("first");
					        new Foo(
					                "first");
					        new Foo(
					                "a", "b",
					                "c", "d",
					                "e", "f");
					        new Foo(
					                "a", "b", "c",
					                "d", "e", "f");
					        new Foo(
					                "a", "b", "c",
					                "d", "e", "f",
					                "g", "h", "i");
					        new Foo(
					                "a", "b", "c", "d",
					                "e", "f", "g", "h");
					    }
					}"""
				)
			);
	}
}
