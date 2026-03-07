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

class ECStreamMapToPrimitiveSumToCollectPrimitiveSumTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamMapToPrimitiveSumToCollectPrimitiveSum());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    double mapToDoubleSumWithMethodReference = mutableList.stream().mapToDouble(String::length).sum();

					    double mapToDoubleSumWithLambda = mutableList.stream().mapToDouble(s -> s.length() * 1.0).sum();

					    int mapToIntSum = mutableList.stream().mapToInt(String::length).sum();

					    long mapToLongSum = mutableList.stream().mapToLong(s -> (long) s.length()).sum();

					    double withImmutableList = immutableList.stream().mapToDouble(String::length).sum();

					    int withMutableSet = mutableSet.stream().mapToInt(String::length).sum();
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    double mapToDoubleSumWithMethodReference = mutableList.collectDouble(String::length).sum();

					    double mapToDoubleSumWithLambda = mutableList.collectDouble(s -> s.length() * 1.0).sum();

					    int mapToIntSum = mutableList.collectInt(String::length).sum();

					    long mapToLongSum = mutableList.collectLong(s -> (long) s.length()).sum();

					    double withImmutableList = immutableList.collectDouble(String::length).sum();

					    int withMutableSet = mutableSet.collectInt(String::length).sum();
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
					import java.util.OptionalDouble;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    ArrayList<String> arrayList = new ArrayList<>();
					    MutableList<String> mutableList;

					    double nonEclipseCollectionsType = arrayList.stream().mapToDouble(String::length).sum();

					    OptionalDouble mapToDoubleWithoutSum = mutableList.stream().mapToDouble(String::length).average();
					}
					"""
				)
			);
	}
}
