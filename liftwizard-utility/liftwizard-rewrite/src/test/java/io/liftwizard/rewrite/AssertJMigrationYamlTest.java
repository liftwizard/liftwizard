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

package io.liftwizard.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

class AssertJMigrationYamlTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.assertj.AssertJMigration")
			.parser(
				JavaParser.fromJavaVersion()
					.dependsOn(
						"""
						package org.eclipse.collections.impl.test;

						import java.util.Map;
						import java.util.concurrent.Callable;
						import org.eclipse.collections.api.block.predicate.Predicate;

						public final class Verify {
						    public static <T> void assertCount(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {}
						    public static void assertEmpty(String message, Iterable<?> iterable) {}
						    public static void assertEmpty(Iterable<?> iterable) {}
						    public static void assertEmpty(String message, Map<?, ?> map) {}
						    public static void assertEmpty(Map<?, ?> map) {}
						    public static void assertNotEmpty(String message, Iterable<?> iterable) {}
						    public static void assertNotEmpty(Iterable<?> iterable) {}
						    public static void assertNotEmpty(String message, Map<?, ?> map) {}
						    public static void assertNotEmpty(Map<?, ?> map) {}
						    public static void assertSize(String message, int expectedSize, Iterable<?> iterable) {}
						    public static void assertSize(int expectedSize, Iterable<?> iterable) {}
						    public static void assertSize(String message, int expectedSize, Object[] array) {}
						    public static void assertSize(int expectedSize, Object[] array) {}
						    public static void assertSize(String mapName, int expectedSize, Map<?, ?> map) {}
						    public static void assertSize(int expectedSize, Map<?, ?> map) {}
						    public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Runnable code) {}
						    public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Callable<?> code) {}
						}
						"""
					)
					.classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
					.styles(AbstractRewriteStyles.styles())
			);
	}

	@Test
	void transformsVerifyAssertCount() {
		this.rewriteRun(this.javaFixture("transformsVerifyAssertCount/01"));
	}

	@Test
	void transformsVerifyAssertEmpty() {
		this.rewriteRun(this.javaFixture("transformsVerifyAssertEmpty/01"));
	}

	@Test
	void transformsVerifyAssertNotEmpty() {
		this.rewriteRun(this.javaFixture("transformsVerifyAssertNotEmpty/01"));
	}

	@Test
	void transformsVerifyAssertSize() {
		this.rewriteRun(this.javaFixture("transformsVerifyAssertSize/01"));
	}

	@Test
	void transformsVerifyAssertThrows() {
		this.rewriteRun(
			(spec) ->
				spec.typeValidationOptions(
					TypeValidation.builder().identifiers(false).methodInvocations(false).build()
				),
			this.javaFixture("transformsVerifyAssertThrows/01")
		);
	}

	@Test
	void appliesStaticImportOptimization() {
		this.rewriteRun(this.javaFixture("appliesStaticImportOptimization/01"));
	}
}
