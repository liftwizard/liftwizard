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

class ECArraysStreamToArrayAdapterTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECArraysStreamToArrayAdapter());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.List;

					class Test {
					    void test(String[] values, String[] names) {
					        var result1 = Arrays.stream(values).toList();
					        List<String> result2 = Arrays.stream(names).toList();
					        var result3 = Arrays.stream(new String[]{"a", "b", "c"}).toList();
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

					import java.util.List;

					class Test {
					    void test(String[] values, String[] names) {
					        var result1 = ArrayAdapter.adapt(values).toList();
					        List<String> result2 = ArrayAdapter.adapt(names).toList();
					        var result3 = ArrayAdapter.adapt(new String[]{"a", "b", "c"}).toList();
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

					class Test {
					    void test(int[] intValues, long[] longValues, double[] doubleValues, String[] values) {
					        var result1 = Arrays.stream(intValues).sum();
					        var result2 = Arrays.stream(longValues).sum();
					        var result3 = Arrays.stream(doubleValues).sum();
					        var result4 = Arrays.stream(values, 1, 3).toList();
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceUntranslatableChains() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.function.Predicate;
					import java.util.stream.Collectors;

					class Test {
					    void test(String[] values, Predicate<String> predicate, long n) {
					        var result1 = Arrays.stream(values).collect(Collectors.toSet());
					        var result2 = Arrays.stream(values).count();
					        var result3 = Arrays.stream(values).toArray(String[]::new);
					        var result4 = Arrays.stream(values).filter(predicate).toList();
					        var result5 = Arrays.stream(values).skip(n).toArray();
					        var result6 = Arrays.stream(values).sorted().toList();
					        var result7 = Arrays.stream(values).skip(1);
					    }
					}
					"""
				)
			);
	}

	@Test
	void translateStreamChains() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;

					class Test {
					    void test(String[] values) {
					        var result1 = Arrays.stream(values).skip(1).toArray();
					        var result2 = Arrays.stream(values).limit(2).toList();
					        var result3 = Arrays.stream(values).filter(each -> !each.isEmpty()).toList();
					        var result4 = Arrays.stream(values).map(String::trim).toList();
					        var result5 = Arrays.stream(values).distinct().toList();
					        var result6 = Arrays.stream(values).filter(each -> !each.isEmpty()).map(String::trim).toArray();
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

					class Test {
					    void test(String[] values) {
					        var result1 = ArrayAdapter.adapt(values).drop(1).toArray();
					        var result2 = ArrayAdapter.adapt(values).take(2);
					        var result3 = ArrayAdapter.adapt(values).select(each -> !each.isEmpty());
					        var result4 = ArrayAdapter.adapt(values).collect(String::trim);
					        var result5 = ArrayAdapter.adapt(values).distinct();
					        var result6 = ArrayAdapter.adapt(values).select(each -> !each.isEmpty()).collect(String::trim).toArray();
					    }
					}
					"""
				)
			);
	}

	@Test
	void translateMatchTerminals() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;

					class Test {
					    void test(String[] values) {
					        boolean result1 = Arrays.stream(values).anyMatch(String::isEmpty);
					        boolean result2 = Arrays.stream(values).allMatch(each -> each.length() > 1);
					        boolean result3 = Arrays.stream(values).noneMatch(String::isBlank);
					        boolean result4 = Arrays.stream(values).filter(each -> !each.isEmpty()).anyMatch(each -> each.length() > 3);
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

					class Test {
					    void test(String[] values) {
					        boolean result1 = ArrayAdapter.adapt(values).anySatisfy(String::isEmpty);
					        boolean result2 = ArrayAdapter.adapt(values).allSatisfy(each -> each.length() > 1);
					        boolean result3 = ArrayAdapter.adapt(values).noneSatisfy(String::isBlank);
					        boolean result4 = ArrayAdapter.adapt(values).select(each -> !each.isEmpty()).anySatisfy(each -> each.length() > 3);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceStreamTypedUsages() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.stream.Stream;

					class Test {
					    Stream<String> test(String[] values) {
					        Stream<String> stream = Arrays.stream(values);
					        this.consume(Arrays.stream(values));
					        return Arrays.stream(values);
					    }

					    void consume(Stream<String> stream) {
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceSafeTerminalConsumers() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;

					class Test {
					    void test(String[] values) {
					        var result1 = Arrays.stream(values).toArray();
					        var result2 = Arrays.stream(values).iterator();
					        Arrays.stream(values).forEach(each -> each.trim());
					        Arrays.stream(values).forEach(System.out::println);
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

					class Test {
					    void test(String[] values) {
					        var result1 = ArrayAdapter.adapt(values).toArray();
					        var result2 = ArrayAdapter.adapt(values).iterator();
					        ArrayAdapter.adapt(values).forEach(each -> each.trim());
					        ArrayAdapter.adapt(values).forEach(System.out::println);
					    }
					}
					"""
				)
			);
	}
}
