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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECSelectNotEmptyToAnySatisfyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSelectNotEmptyToAnySatisfyRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				// Pattern 1: RichIterable select().notEmpty() -> anySatisfy()
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).notEmpty();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }
					}
					"""
				),
				// Pattern 1: ImmutableList
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.ImmutableList;

					class TestImmutable {
					    boolean test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).notEmpty();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.ImmutableList;

					class TestImmutable {
					    boolean test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }
					}
					"""
				),
				// Pattern 1: MutableSet
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.set.MutableSet;

					class TestSet {
					    boolean test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.select(predicate).notEmpty();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.set.MutableSet;

					class TestSet {
					    boolean test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.anySatisfy(predicate);
					    }
					}
					"""
				),
				// Pattern 2: ArrayIterate.select().notEmpty() -> ArrayIterate.anySatisfy()
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterate {
					    boolean test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.select(array, predicate).notEmpty();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterate {
					    boolean test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.anySatisfy(array, predicate);
					    }
					}
					"""
				),
				// Pattern 2: ListIterate.select().notEmpty() -> ListIterate.anySatisfy()
				java(
					"""
					import java.util.List;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ListIterate;

					class TestListIterate {
					    boolean test(List<String> list, Predicate<String> predicate) {
					        return ListIterate.select(list, predicate).notEmpty();
					    }
					}
					""",
					"""
					import java.util.List;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ListIterate;

					class TestListIterate {
					    boolean test(List<String> list, Predicate<String> predicate) {
					        return ListIterate.anySatisfy(list, predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				// Do not replace when notEmpty is not called on select result
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class TestNoSelect {
					    boolean test(MutableList<String> list) {
					        return list.notEmpty();
					    }
					}
					"""
				),
				// Do not replace when select has intermediate operations
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class TestIntermediate {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).collect(String::toUpperCase).notEmpty();
					    }
					}
					"""
				),
				// Do not replace for non-EC types
				java(
					"""
					import java.util.ArrayList;
					import java.util.List;

					class TestJavaList {
					    void test() {
					        List<String> list = new ArrayList<>();
					        list.stream().filter(s -> s.length() > 5).findAny().isPresent();
					    }
					}
					"""
				)
			);
	}
}
