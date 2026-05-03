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

package io.liftwizard.rewrite.dropwizard.migration;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class Dropwizard3UtilMigrationTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3UtilMigration")
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					"""
					package io.dropwizard.util;

					import java.util.Collections;

					public final class Sets {
					    @SafeVarargs
					    public static <T> java.util.Set<T> of(T... elements) {
					        return Collections.emptySet();
					    }
					}
					"""
				)
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
			java(
				"""
				import io.dropwizard.util.Sets;
				import java.util.Set;

				class MyConfig {
				    Set<String> allowed = Sets.of("a", "b", "c");
				}
				""",
				"""
				import java.util.Set;

				class MyConfig {
				    Set<String> allowed = Set.of("a", "b", "c");
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
				import java.util.Set;

				class MyConfig {
				    Set<String> allowed = Set.of("a", "b", "c");
				}
				"""
			)
		);
	}
}
