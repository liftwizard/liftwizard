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

class ECMapConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECMapConstructorToFactory());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.map.MutableMap;
					import org.eclipse.collections.impl.map.mutable.UnifiedMap;
					import java.util.List;

					class Test<T> {
					    private final MutableMap<String, String> fieldInterfaceEmpty = new UnifiedMap<>();
					    private final MutableMap<String, Integer> fieldInterfaceCapacity = new UnifiedMap<>(10);
					    private final MutableMap<String, String> fieldInterfaceMap = new UnifiedMap<>(this.fieldInterfaceEmpty);

					    void test() {
					        MutableMap<String, Integer> diamondMap = new UnifiedMap<>();
					        MutableMap<String, List<Integer>> nestedGenerics = new UnifiedMap<>();
					        MutableMap<String, ?> wildcardGenerics = new UnifiedMap<>();
					        MutableMap<String, ? extends Number> boundedWildcards = new UnifiedMap<>();
					        MutableMap<String, Integer> explicitSimple = new UnifiedMap<String, Integer>();
					        MutableMap<String, List<Integer>> explicitNested = new UnifiedMap<String, List<Integer>>();
					        MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = new UnifiedMap<String, MutableMap<T, Integer>>();
					        org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified = new org.eclipse.collections.impl.map.mutable.UnifiedMap<>();
					        MutableMap<String, Integer> withCapacity = new UnifiedMap<>(16);
					        MutableMap<Integer, String> withCapacity32 = new UnifiedMap<>(32);
					        MutableMap<String, Integer> mapFromOther = new UnifiedMap<>(diamondMap);
					    }
					}

					class A<K, V> {
					    @Override
					    public MutableMap<K, V> newEmpty() {
					        return new UnifiedMap<>();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.Maps;
					import org.eclipse.collections.api.map.MutableMap;

					import java.util.List;

					class Test<T> {
					    private final MutableMap<String, String> fieldInterfaceEmpty = Maps.mutable.empty();
					    private final MutableMap<String, Integer> fieldInterfaceCapacity = Maps.mutable.withInitialCapacity(10);
					    private final MutableMap<String, String> fieldInterfaceMap = Maps.mutable.withMap(this.fieldInterfaceEmpty);

					    void test() {
					        MutableMap<String, Integer> diamondMap = Maps.mutable.empty();
					        MutableMap<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
					        MutableMap<String, ?> wildcardGenerics = Maps.mutable.empty();
					        MutableMap<String, ? extends Number> boundedWildcards = Maps.mutable.empty();
					        MutableMap<String, Integer> explicitSimple = Maps.mutable.<String, Integer>empty();
					        MutableMap<String, List<Integer>> explicitNested = Maps.mutable.<String, List<Integer>>empty();
					        MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = Maps.mutable.<String, MutableMap<T, Integer>>empty();
					        org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified = Maps.mutable.empty();
					        MutableMap<String, Integer> withCapacity = Maps.mutable.withInitialCapacity(16);
					        MutableMap<Integer, String> withCapacity32 = Maps.mutable.withInitialCapacity(32);
					        MutableMap<String, Integer> mapFromOther = Maps.mutable.withMap(diamondMap);
					    }
					}

					class A<K, V> {
					    @Override
					    public MutableMap<K, V> newEmpty() {
					        return Maps.mutable.empty();
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
					import org.eclipse.collections.impl.map.mutable.UnifiedMap;
					import java.util.HashMap;

					class Test {
					    private final UnifiedMap<String, Integer> fieldConcreteType = new UnifiedMap<>();
					    private final UnifiedMap<String, Integer> fieldConcreteCapacity = new UnifiedMap<>(10);
					    private final UnifiedMap<String, Integer> fieldConcreteMap = new UnifiedMap<>(this.fieldConcreteType);

					    void test() {
					        UnifiedMap<String, Integer> concreteTypeEmpty = new UnifiedMap<>();
					        UnifiedMap<String, Integer> concreteTypeCapacity = new UnifiedMap<>(10);
					        UnifiedMap<String, Integer> concreteTypeMap = new UnifiedMap<>(concreteTypeEmpty);
					        HashMap<String, Integer> jdkMap = new HashMap<>();
					    }
					}
					"""
				)
			);
	}
}
