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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ECImplFactoryToApiFactoryTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new ECImplFactoryToApiFactoryRecipes())
			.parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.factory.Lists;
					import org.eclipse.collections.impl.factory.Sets;
					import org.eclipse.collections.impl.factory.Maps;
					import org.eclipse.collections.impl.factory.Bags;
					import org.eclipse.collections.impl.factory.Stacks;
					import org.eclipse.collections.impl.factory.SortedSets;
					import org.eclipse.collections.impl.factory.SortedMaps;
					import org.eclipse.collections.impl.factory.SortedBags;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					public class Example {
					    private MutableList<String> listField = Lists.mutable.empty();
					    private final MutableSet<String> setField;

					    public Example() {
					        this.setField = Sets.mutable.empty();
					    }

					    void method() {
					        var list = Lists.mutable.empty();
					        var set = Sets.mutable.empty();
					        var map = Maps.mutable.empty();
					        var bag = Bags.mutable.empty();
					        var stack = Stacks.mutable.empty();
					        var sortedSet = SortedSets.mutable.empty();
					        var sortedMap = SortedMaps.mutable.empty();
					        var sortedBag = SortedBags.mutable.empty();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.*;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					public class Example {
					    private MutableList<String> listField = Lists.mutable.empty();
					    private final MutableSet<String> setField;

					    public Example() {
					        this.setField = Sets.mutable.empty();
					    }

					    void method() {
					        var list = Lists.mutable.empty();
					        var set = Sets.mutable.empty();
					        var map = Maps.mutable.empty();
					        var bag = Bags.mutable.empty();
					        var stack = Stacks.mutable.empty();
					        var sortedSet = SortedSets.mutable.empty();
					        var sortedMap = SortedMaps.mutable.empty();
					        var sortedBag = SortedBags.mutable.empty();
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceAlreadyApiFactory() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Sets;
					import org.eclipse.collections.api.factory.Maps;

					public class Example {
					    void method() {
					        var list = Lists.mutable.empty();
					        var set = Sets.mutable.empty();
					        var map = Maps.mutable.empty();
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotTransformStaticUtilityMethods() {
		this.rewriteRun(
				java(
					"""
					import java.util.ArrayList;
					import java.util.HashMap;
					import java.util.List;
					import java.util.Map;
					import java.util.Set;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.map.MutableMap;
					import org.eclipse.collections.impl.factory.Lists;
					import org.eclipse.collections.impl.factory.Maps;
					import org.eclipse.collections.impl.factory.Sets;

					public class Example {
					    void setsUnion(Set<String> a, Set<String> b) {
					        Set<String> union = Sets.union(a, b);
					    }

					    void listsAdapt() {
					        List<String> javaList = new ArrayList<>();
					        MutableList<String> adapted = Lists.adapt(javaList);
					    }

					    void mapsAdapt() {
					        Map<String, String> javaMap = new HashMap<>();
					        MutableMap<String, String> adapted = Maps.adapt(javaMap);
					    }
					}
					"""
				)
			);
	}
}
