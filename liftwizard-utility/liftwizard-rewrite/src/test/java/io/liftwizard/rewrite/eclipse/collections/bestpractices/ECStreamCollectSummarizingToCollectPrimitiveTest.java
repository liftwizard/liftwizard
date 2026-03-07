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

class ECStreamCollectSummarizingToCollectPrimitiveTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectSummarizingToCollectPrimitive());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.DoubleSummaryStatistics;
					import java.util.IntSummaryStatistics;
					import java.util.LongSummaryStatistics;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> set;

					    void test() {
					        DoubleSummaryStatistics result1 = mutableList.stream().collect(Collectors.summarizingDouble(String::length));
					        DoubleSummaryStatistics result2 = mutableList.stream().collect(Collectors.summarizingDouble(s -> s.length() * 1.0));
					        IntSummaryStatistics result3 = mutableList.stream().collect(Collectors.summarizingInt(String::length));
					        LongSummaryStatistics result4 = mutableList.stream().collect(Collectors.summarizingLong(s -> (long) s.length()));
					        DoubleSummaryStatistics result5 = immutableList.stream().collect(Collectors.summarizingDouble(String::length));
					        IntSummaryStatistics result6 = set.stream().collect(Collectors.summarizingInt(String::length));
					    }
					}
					""",
					"""
					import java.util.DoubleSummaryStatistics;
					import java.util.IntSummaryStatistics;
					import java.util.LongSummaryStatistics;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> set;

					    void test() {
					        DoubleSummaryStatistics result1 = mutableList.collectDouble(String::length).summaryStatistics();
					        DoubleSummaryStatistics result2 = mutableList.collectDouble(s -> s.length() * 1.0).summaryStatistics();
					        IntSummaryStatistics result3 = mutableList.collectInt(String::length).summaryStatistics();
					        LongSummaryStatistics result4 = mutableList.collectLong(s -> (long) s.length()).summaryStatistics();
					        DoubleSummaryStatistics result5 = immutableList.collectDouble(String::length).summaryStatistics();
					        IntSummaryStatistics result6 = set.collectInt(String::length).summaryStatistics();
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
					import java.util.ArrayList;
					import java.util.DoubleSummaryStatistics;
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    ArrayList<String> arrayList = new ArrayList<>();
					    MutableList<String> list;

					    void test() {
					        DoubleSummaryStatistics result1 = arrayList.stream().collect(Collectors.summarizingDouble(String::length));
					        List<String> result2 = list.stream().collect(Collectors.toList());
					    }
					}
					"""
				)
			);
	}
}
