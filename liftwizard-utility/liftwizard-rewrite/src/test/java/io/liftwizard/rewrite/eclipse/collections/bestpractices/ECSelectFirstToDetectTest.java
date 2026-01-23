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

class ECSelectFirstToDetectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSelectFirstToDetectRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				// Pattern 1: OrderedIterable select().getFirstOptional() -> detectOptional()
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).getFirstOptional();
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
					    }
					}
					"""
				),
				// Pattern 1: RichIterable select().getFirst() -> detect()
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class TestGetFirst {
					    String test(MutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).getFirst();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class TestGetFirst {
					    String test(MutableList<String> list, Predicate<String> predicate) {
					        return list.detect(predicate);
					    }
					}
					"""
				),
				// Pattern 1: ImmutableList
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.ImmutableList;

					class TestImmutable {
					    Optional<String> test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).getFirstOptional();
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.ImmutableList;

					class TestImmutable {
					    Optional<String> test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
					    }
					}
					"""
				),
				// Pattern 1: MutableSet with getFirst() (not getFirstOptional())
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.set.MutableSet;

					class TestSet {
					    Integer test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.select(predicate).getFirst();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.set.MutableSet;

					class TestSet {
					    Integer test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.detect(predicate);
					    }
					}
					"""
				),
				// Pattern 2: ArrayIterate.select().getFirstOptional() -> ArrayIterate.detectOptional()
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterate {
					    Optional<String> test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.select(array, predicate).getFirstOptional();
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterate {
					    Optional<String> test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.detectOptional(array, predicate);
					    }
					}
					"""
				),
				// Pattern 2: ArrayIterate.select().getFirst() -> ArrayIterate.detect()
				java(
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterateGetFirst {
					    String test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.select(array, predicate).getFirst();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class TestArrayIterateGetFirst {
					    String test(String[] array, Predicate<String> predicate) {
					        return ArrayIterate.detect(array, predicate);
					    }
					}
					"""
				),
				// Pattern 2: ListIterate.select().getFirstOptional() -> ListIterate.detectOptional()
				java(
					"""
					import java.util.List;
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ListIterate;

					class TestListIterate {
					    Optional<String> test(List<String> list, Predicate<String> predicate) {
					        return ListIterate.select(list, predicate).getFirstOptional();
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ListIterate;

					class TestListIterate {
					    Optional<String> test(List<String> list, Predicate<String> predicate) {
					        return ListIterate.detectOptional(list, predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				// Do not replace when getFirstOptional is not called on select result
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class TestNoSelect {
					    Optional<String> test(MutableList<String> list) {
					        return list.getFirstOptional();
					    }
					}
					"""
				),
				// Do not replace when getFirst is not called on select result
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class TestNoSelectGetFirst {
					    String test(MutableList<String> list) {
					        return list.getFirst();
					    }
					}
					"""
				),
				// Do not replace when select has intermediate operations
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class TestIntermediate {
					    Optional<String> test(MutableList<String> list, Predicate<String> predicate) {
					        return list.select(predicate).collect(String::toUpperCase).getFirstOptional();
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
					        list.stream().filter(s -> s.length() > 5).findFirst();
					    }
					}
					"""
				)
			);
	}
}
