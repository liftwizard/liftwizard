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

package io.liftwizard.rewrite.eclipse.collections.removal;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ComparatorsNullSafeEqualsToObjectsEqualsTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ComparatorsNullSafeEqualsToObjectsEqualsRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.block.factory.Comparators;

					class Test {
					    void test() {
					        String left = "foo";
					        String right = "bar";
					        boolean equal = Comparators.nullSafeEquals(left, right);
					        boolean notEqual = !Comparators.nullSafeEquals(left, right);
					    }
					}
					""",
					"""
					import java.util.Objects;

					class Test {
					    void test() {
					        String left = "foo";
					        String right = "bar";
					        boolean equal = Objects.equals(left, right);
					        boolean notEqual = !Objects.equals(left, right);
					    }
					}
					"""
				)
			);
	}
}
