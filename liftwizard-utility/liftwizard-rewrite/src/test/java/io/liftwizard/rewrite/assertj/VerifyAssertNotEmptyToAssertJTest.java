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

package io.liftwizard.rewrite.assertj;

import io.liftwizard.rewrite.AbstractRewriteFixtures;
import io.liftwizard.rewrite.AbstractRewriteStyles;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class VerifyAssertNotEmptyToAssertJTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new VerifyAssertNotEmptyToAssertJRecipes())
			.parser(
				JavaParser.fromJavaVersion()
					.dependsOn(
						"""
						package org.eclipse.collections.impl.test;

						import java.util.Map;

						public final class Verify {
						    public static void assertNotEmpty(String message, Iterable<?> iterable) {}
						    public static void assertNotEmpty(Iterable<?> iterable) {}
						    public static void assertNotEmpty(String message, Map<?, ?> map) {}
						    public static void assertNotEmpty(Map<?, ?> map) {}
						    public static <T> void assertNotEmpty(String message, T[] array) {}
						    public static <T> void assertNotEmpty(T[] array) {}
						}
						"""
					)
					.classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
					.styles(AbstractRewriteStyles.styles())
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(this.javaFixture("replacePatterns/01"));
	}
}
