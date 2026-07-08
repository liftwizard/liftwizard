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

class ECStreamChainToListIterableTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamChainToListIterable());
	}

	@DocumentExample
	@Test
	void translateStreamChains() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        var result1 = list.stream().toList();
					        var result2 = list.stream().skip(1).toArray();
					        var result3 = list.stream().limit(2).toList();
					        var result4 = list.stream().filter(each -> !each.isEmpty()).toList();
					        var result5 = list.stream().map(String::trim).toList();
					        var result6 = list.stream().distinct().toList();
					        var result7 = list.stream().filter(each -> !each.isEmpty()).map(String::trim).toArray();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        var result1 = list.toList();
					        var result2 = list.drop(1).toArray();
					        var result3 = list.take(2);
					        var result4 = list.select(each -> !each.isEmpty());
					        var result5 = list.collect(String::trim);
					        var result6 = list.distinct();
					        var result7 = list.select(each -> !each.isEmpty()).collect(String::trim).toArray();
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
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean result1 = list.stream().anyMatch(String::isEmpty);
					        boolean result2 = list.stream().allMatch(each -> each.length() > 1);
					        boolean result3 = list.stream().noneMatch(String::isBlank);
					        boolean result4 = list.stream().filter(each -> !each.isEmpty()).anyMatch(each -> each.length() > 3);
					        list.stream().forEach(System.out::println);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean result1 = list.anySatisfy(String::isEmpty);
					        boolean result2 = list.allSatisfy(each -> each.length() > 1);
					        boolean result3 = list.noneSatisfy(String::isBlank);
					        boolean result4 = list.select(each -> !each.isEmpty()).anySatisfy(each -> each.length() > 3);
					        list.forEach(System.out::println);
					    }
					}
					"""
				)
			);
	}

	@Test
	void translateCollectorTerminals() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        List<String> result1 = list.stream().collect(Collectors.toList());
					        Set<String> result2 = list.stream().map(String::trim).collect(Collectors.toSet());
					        var result3 = list.stream().filter(each -> !each.isEmpty()).collect(Collectors.toUnmodifiableList());
					        var result4 = list.stream().map(String::trim).collect(Collectors.toUnmodifiableSet());
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.Set;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        List<String> result1 = list.toList();
					        Set<String> result2 = list.collect(String::trim).toSet();
					        var result3 = list.select(each -> !each.isEmpty()).toImmutableList();
					        var result4 = list.collect(String::trim).toImmutableSet();
					    }
					}
					"""
				)
			);
	}

	@Test
	void translateSortedCountAndFindFirstTerminals() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;
					import java.util.List;
					import java.util.Optional;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        List<String> result1 = list.stream().sorted().collect(Collectors.toList());
					        List<String> result2 = list.stream().sorted(Comparator.naturalOrder()).toList();
					        boolean result3 = list.stream().count() > 2;
					        boolean result4 = 2 < list.stream().filter(each -> !each.isEmpty()).count();
					        Optional<String> result5 = list.stream().filter(String::isEmpty).findFirst();
					        String result6 = list.stream().filter(String::isEmpty).findFirst().orElse("fallback");
					    }
					}
					""",
					"""
					import java.util.Comparator;
					import java.util.List;
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        List<String> result1 = list.toSortedList();
					        List<String> result2 = list.toSortedList(Comparator.naturalOrder());
					        boolean result3 = list.size() > 2;
					        boolean result4 = 2 < list.select(each -> !each.isEmpty()).size();
					        Optional<String> result5 = list.detectOptional(String::isEmpty);
					        String result6 = list.detectOptional(String::isEmpty).orElse("fallback");
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceUntranslatableReceivers() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;

					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(List<String> jcfList, MutableSet<String> set) {
					        var result1 = jcfList.stream().skip(1).toArray();
					        var result2 = set.stream().map(String::trim).toList();
					        var result3 = set.stream().anyMatch(String::isEmpty);
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
					import java.util.function.Predicate;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate, long n) {
					        var result1 = list.stream().collect(Collectors.groupingBy(String::length));
					        var result2 = list.stream().count();
					        var result3 = list.stream().toArray(String[]::new);
					        var result4 = list.stream().filter(predicate).toList();
					        var result5 = list.stream().skip(n).toArray();
					        var result6 = list.stream().findFirst();
					        var result7 = list.stream().skip(1);
					        Stream<String> stream = list.stream();
					    }
					}
					"""
				)
			);
	}
}
