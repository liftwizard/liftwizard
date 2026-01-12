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

	@Test
	@DocumentExample
	void replaceStreamReduceWithInjectInto() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<Integer> list) {
					        return list.stream().reduce(0, Integer::sum);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamReduceWithLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<Integer> list) {
					        return list.stream().reduce(0, (a, b) -> a + b);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<Integer> list) {
					        return list.injectInto(0, (a, b) -> a + b);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamReduceWithImmutableList() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    Integer test(ImmutableList<Integer> list) {
					        return list.stream().reduce(0, Integer::sum);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    Integer test(ImmutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamReduceWithMutableSet() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Integer test(MutableSet<Integer> set) {
					        return set.stream().reduce(1, (a, b) -> a * b);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Integer test(MutableSet<Integer> set) {
					        return set.injectInto(1, (a, b) -> a * b);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamReduceWithStringConcat() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    String test(MutableList<String> list) {
					        return list.stream().reduce("", String::concat);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    String test(MutableList<String> list) {
					        return list.injectInto("", String::concat);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceInIfCondition() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list) {
					        Integer sum = list.stream().reduce(0, Integer::sum);
					        if (list.stream().reduce(0, Integer::sum) > 100) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list) {
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
	void doNotReplaceStreamWithIntermediateOperations() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<Integer> list) {
					        // Should not replace when there are intermediate operations
					        return list.stream()
					            .filter(i -> i > 0)
					            .reduce(0, Integer::sum);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceOptionalReduce() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.Optional;

					class Test {
					    Optional<Integer> test(MutableList<Integer> list) {
					        // Should not replace reduce that returns Optional (no identity)
					        return list.stream().reduce(Integer::sum);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceThreeArgumentReduce() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Integer test(MutableList<String> list) {
					        // Should not replace the three-argument reduce (parallel reduce)
					        return list.stream().reduce(0, (acc, s) -> acc + s.length(), Integer::sum);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceNonEclipseCollections() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;

					class Test {
					    Integer test(List<Integer> list) {
					        // Should not replace JCF List
					        return list.stream().reduce(0, Integer::sum);
					    }
					}
					"""
				)
			);
	}
}
