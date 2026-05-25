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

class ECStreamReduceToInjectIntoTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamReduceToInjectInto());
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
					    Integer withMethodReference(MutableList<Integer> list) {
					        return list.stream().reduce(0, Integer::sum);
					    }

					    Integer withLambda(MutableList<Integer> list) {
					        return list.stream().reduce(0, (a, b) -> a + b);
					    }

					    Integer withImmutableList(ImmutableList<Integer> list) {
					        return list.stream().reduce(0, Integer::sum);
					    }

					    Integer withMutableSet(MutableSet<Integer> set) {
					        return set.stream().reduce(1, (a, b) -> a * b);
					    }

					    String withStringConcat(MutableList<String> list) {
					        return list.stream().reduce("", String::concat);
					    }

					    void inIfCondition(MutableList<Integer> list) {
					        Integer sum = list.stream().reduce(0, Integer::sum);
					        if (list.stream().reduce(0, Integer::sum) > 100) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Integer withMethodReference(MutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }

					    Integer withLambda(MutableList<Integer> list) {
					        return list.injectInto(0, (a, b) -> a + b);
					    }

					    Integer withImmutableList(ImmutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }

					    Integer withMutableSet(MutableSet<Integer> set) {
					        return set.injectInto(1, (a, b) -> a * b);
					    }

					    String withStringConcat(MutableList<String> list) {
					        return list.injectInto("", String::concat);
					    }

					    void inIfCondition(MutableList<Integer> list) {
					        Integer sum = list.injectInto(0, Integer::sum);
					        if (list.injectInto(0, Integer::sum) > 100) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
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
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer withIntermediateOperations(MutableList<Integer> list) {
					        return list.stream()
					            .filter(i -> i > 0)
					            .reduce(0, Integer::sum);
					    }

					    Optional<Integer> withOptionalReduce(MutableList<Integer> list) {
					        return list.stream().reduce(Integer::sum);
					    }

					    Integer withThreeArgumentReduce(MutableList<String> list) {
					        return list.stream().reduce(0, (acc, s) -> acc + s.length(), Integer::sum);
					    }

					    Integer withJcfList(List<Integer> list) {
					        return list.stream().reduce(0, Integer::sum);
					    }
					}
					"""
				)
			);
	}
}
