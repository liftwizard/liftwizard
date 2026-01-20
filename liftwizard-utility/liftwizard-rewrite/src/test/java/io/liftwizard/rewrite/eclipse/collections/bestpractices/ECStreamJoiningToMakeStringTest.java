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

class ECStreamJoiningToMakeStringTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamJoiningToMakeString());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> stringList;
					    MutableList<Integer> intList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    // Basic joining with MutableList<String>
					    String result1 = stringList.stream().collect(Collectors.joining(", "));

					    // stream().map(Object::toString).collect(joining)
					    String result2 = intList.stream().map(Object::toString).collect(Collectors.joining(", "));

					    // Empty delimiter
					    String result3 = stringList.stream().collect(Collectors.joining(""));

					    // ImmutableList
					    String result4 = immutableList.stream().collect(Collectors.joining("-"));

					    // MutableSet
					    String result5 = mutableSet.stream().collect(Collectors.joining("|"));

					    // stream().map(String::toString).collect(joining)
					    String result6 = stringList.stream().map(String::toString).collect(Collectors.joining(", "));
					}
					""",
					"""
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> stringList;
					    MutableList<Integer> intList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    // Basic joining with MutableList<String>
					    String result1 = stringList.makeString(", ");

					    // stream().map(Object::toString).collect(joining)
					    String result2 = intList.makeString(", ");

					    // Empty delimiter
					    String result3 = stringList.makeString("");

					    // ImmutableList
					    String result4 = immutableList.makeString("-");

					    // MutableSet
					    String result5 = mutableSet.makeString("|");

					    // stream().map(String::toString).collect(joining)
					    String result6 = stringList.makeString(", ");
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
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    List<String> jcfList;

					    // No delimiter - Collectors.joining() uses "" as default, makeString() uses ", "
					    String invalid1 = mutableList.stream().collect(Collectors.joining());

					    // Different map function (not toString)
					    String invalid2 = mutableList.stream().map(String::toUpperCase).collect(Collectors.joining(", "));

					    // Three-arg joining (with prefix and suffix)
					    String invalid3 = mutableList.stream().collect(Collectors.joining(", ", "[", "]"));

					    // Non-Eclipse Collections type (JCF List)
					    String invalid4 = jcfList.stream().collect(Collectors.joining(", "));

					    // Intermediate operations (filter before collect)
					    String invalid5() {
					        return mutableList.stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(", "));
					    }
					}
					"""
				)
			);
	}
}
