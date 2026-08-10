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

class VerifyAssertSizeToAssertJTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new VerifyAssertSizeToAssertJRecipes())
			.parser(
				JavaParser.fromJavaVersion()
					.dependsOn(
						"""
						package org.eclipse.collections.impl.test;

						import java.util.Map;

						public final class Verify {
						    public static void assertSize(String message, int expectedSize, Iterable<?> iterable) {}
						    public static void assertSize(int expectedSize, Iterable<?> iterable) {}
						    public static void assertSize(String message, int expectedSize, Object[] array) {}
						    public static void assertSize(int expectedSize, Object[] array) {}
						    public static void assertSize(String mapName, int expectedSize, Map<?, ?> map) {}
						    public static void assertSize(int expectedSize, Map<?, ?> map) {}
						}
						"""
					)
					.classpath("eclipse-collections-api", "eclipse-collections")
					.styles(AbstractRewriteStyles.styles())
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(this.javaFixture("replacePatterns/01"));
	}
}
