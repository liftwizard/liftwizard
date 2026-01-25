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

class ECListConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECListConstructorToFactory());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import java.util.Map;

					class Test<T> {
					    private final MutableList<String> fieldInterfaceEmpty = new FastList<>();
					    private final MutableList<Integer> fieldInterfaceCapacity = new FastList<>(10);
					    private final MutableList<String> fieldInterfaceCollection = new FastList<>(fieldInterfaceEmpty);

					    void test() {
					        MutableList<String> diamondList = new FastList<>();
					        MutableList rawList = new FastList();
					        MutableList<Map<String, Integer>> nestedGenerics = new FastList<>();
					        MutableList<? extends Number> wildcardGenerics = new FastList<>();
					        MutableList<String> explicitSimple = new FastList<String>();
					        MutableList<Map<String, Integer>> explicitNested = new FastList<Map<String, Integer>>();
					        MutableList<MutableList<T>> nestedTypeParam = new FastList<MutableList<T>>();
					        org.eclipse.collections.api.list.MutableList<String> fullyQualified = new org.eclipse.collections.impl.list.mutable.FastList<>();
					        MutableList<String> withCapacity = new FastList<>(16);
					        MutableList<Integer> withCapacity32 = new FastList<>(32);
					        MutableList<String> listFromOther = new FastList<>(diamondList);
					    }
					}

					class A<T> {
					    @Override
					    public MutableList<T> newEmpty() {
					        return new FastList<>();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.list.MutableList;

					import java.util.Map;

					class Test<T> {
					    private final MutableList<String> fieldInterfaceEmpty = Lists.mutable.empty();
					    private final MutableList<Integer> fieldInterfaceCapacity = Lists.mutable.withInitialCapacity(10);
					    private final MutableList<String> fieldInterfaceCollection = Lists.mutable.withAll(fieldInterfaceEmpty);

					    void test() {
					        MutableList<String> diamondList = Lists.mutable.empty();
					        MutableList rawList = Lists.mutable.empty();
					        MutableList<Map<String, Integer>> nestedGenerics = Lists.mutable.empty();
					        MutableList<? extends Number> wildcardGenerics = Lists.mutable.empty();
					        MutableList<String> explicitSimple = Lists.mutable.<String>empty();
					        MutableList<Map<String, Integer>> explicitNested = Lists.mutable.<Map<String, Integer>>empty();
					        MutableList<MutableList<T>> nestedTypeParam = Lists.mutable.<MutableList<T>>empty();
					        org.eclipse.collections.api.list.MutableList<String> fullyQualified = Lists.mutable.empty();
					        MutableList<String> withCapacity = Lists.mutable.withInitialCapacity(16);
					        MutableList<Integer> withCapacity32 = Lists.mutable.withInitialCapacity(32);
					        MutableList<String> listFromOther = Lists.mutable.withAll(diamondList);
					    }
					}

					class A<T> {
					    @Override
					    public MutableList<T> newEmpty() {
					        return Lists.mutable.empty();
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
					import org.eclipse.collections.impl.list.mutable.FastList;

					class Test {
					    private final FastList<String> fieldConcreteType = new FastList<>();

					    void test() {
					        FastList<String> concreteTypeEmpty = new FastList<>();
					        FastList<String> concreteTypeCapacity = new FastList<>(10);
					        FastList<String> concreteTypeCollection = new FastList<>(concreteTypeEmpty);
					    }
					}
					"""
				)
			);
	}
}
