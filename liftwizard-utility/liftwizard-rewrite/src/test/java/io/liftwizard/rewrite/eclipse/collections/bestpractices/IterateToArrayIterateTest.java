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

class IterateToArrayIterateTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new IterateToArrayIterate());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void testWithPredicate(String[] array) {
					        Predicate<String> predicate = s -> s.length() > 5;
					        boolean result = Iterate.anySatisfy(Arrays.asList(array), predicate);
					    }

					    void testMultipleMethods(String[] names, Integer[] numbers) {
					        Predicate<String> predicate = s -> s.length() > 5;
					        Function<Integer, String> function = Object::toString;
					        boolean any = Iterate.anySatisfy(Arrays.asList(names), predicate);
					        boolean all = Iterate.allSatisfy(Arrays.asList(names), predicate);
					        boolean none = Iterate.noneSatisfy(Arrays.asList(names), predicate);
					        String detected = Iterate.detect(Arrays.asList(names), predicate);
					        int count = Iterate.count(Arrays.asList(numbers), n -> n > 0);
					        java.util.Collection<String> collected = Iterate.collect(Arrays.asList(numbers), function);
					    }

					    void testWithLambda(String[] array) {
					        boolean result = Iterate.anySatisfy(Arrays.asList(array), s -> s.startsWith("test"));
					    }

					    void testWithMethodReference(String[] array) {
					        boolean result = Iterate.anySatisfy(Arrays.asList(array), String::isEmpty);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class Test {
					    void testWithPredicate(String[] array) {
					        Predicate<String> predicate = s -> s.length() > 5;
					        boolean result = ArrayIterate.anySatisfy(array, predicate);
					    }

					    void testMultipleMethods(String[] names, Integer[] numbers) {
					        Predicate<String> predicate = s -> s.length() > 5;
					        Function<Integer, String> function = Object::toString;
					        boolean any = ArrayIterate.anySatisfy(names, predicate);
					        boolean all = ArrayIterate.allSatisfy(names, predicate);
					        boolean none = ArrayIterate.noneSatisfy(names, predicate);
					        String detected = ArrayIterate.detect(names, predicate);
					        int count = ArrayIterate.count(numbers, n -> n > 0);
					        java.util.Collection<String> collected = ArrayIterate.collect(numbers, function);
					    }

					    void testWithLambda(String[] array) {
					        boolean result = ArrayIterate.anySatisfy(array, s -> s.startsWith("test"));
					    }

					    void testWithMethodReference(String[] array) {
					        boolean result = ArrayIterate.anySatisfy(array, String::isEmpty);
					    }
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
					import java.util.Arrays;
					import java.util.List;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    boolean testNonArraysAsListCalls(List<String> list) {
					        return Iterate.anySatisfy(list, s -> s.length() > 5);
					    }

					    boolean testWithMultipleArraysAsListArguments(String[] array) {
					        Predicate<String[]> predicate = arr -> arr.length > 0;
					        return Iterate.anySatisfy(Arrays.asList(array, new String[]{"extra"}), predicate);
					    }
					}
					"""
				)
			);
	}
}
