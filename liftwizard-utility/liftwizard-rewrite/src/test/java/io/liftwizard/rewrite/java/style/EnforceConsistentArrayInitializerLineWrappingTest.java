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

class EnforceConsistentArrayInitializerLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentArrayInitializerLineWrapping());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
					    void test() {
					        String[] a = new String[] { "first",
					                "second",
					                "third" };
					        String[] b = new String[] { "first",
					                "second" };
					        String[] c = new String[] {
					                "first", "second", "third" };
					        String[] d = new String[] {
					                "a", "b",
					                "c", "d",
					                "e" };
					        String[] e = new String[] {
					                "a", "b",
					                "c", "d",
					                "e", "f", "g" };
					    }
					}""",
					"""
					class Test {
					    void test() {
					        String[] a = new String[] {
					                "first",
					                "second",
					                "third" };
					        String[] b = new String[] {
					                "first",
					                "second" };
					        String[] c = new String[] {
					                "first",
					                "second",
					                "third" };
					        String[] d = new String[] {
					                "a",
					                "b",
					                "c",
					                "d",
					                "e" };
					        String[] e = new String[] {
					                "a",
					                "b",
					                "c",
					                "d",
					                "e",
					                "f",
					                "g" };
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
					    void test() {
					        String[] a = new String[] { "first", "second", "third" };
					        String[] b = new String[] {
					                "first",
					                "second",
					                "third" };
					        String[] c = new String[] { "first" };
					        String[] d = new String[] {
					                "first" };
					        String[] e = new String[] {
					                "a", "b",
					                "c", "d",
					                "e", "f" };
					        String[] f = new String[] {
					                "a", "b", "c",
					                "d", "e", "f" };
					        String[] g = new String[] {
					                "a", "b", "c",
					                "d", "e", "f",
					                "g", "h", "i" };
					    }
					}"""
				)
			);
	}
}
