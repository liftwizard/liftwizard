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

class ECConcreteToInterfaceRecipeTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipes(
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.list.mutable.FastList",
				"org.eclipse.collections.api.list.MutableList"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.mutable.UnifiedSet",
				"org.eclipse.collections.api.set.MutableSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.mutable.UnifiedMap",
				"org.eclipse.collections.api.map.MutableMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.bag.mutable.HashBag",
				"org.eclipse.collections.api.bag.MutableBag"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet",
				"org.eclipse.collections.api.set.sorted.MutableSortedSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap",
				"org.eclipse.collections.api.map.sorted.MutableSortedMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.stack.mutable.ArrayStack",
				"org.eclipse.collections.api.stack.MutableStack"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.mutable.ConcurrentHashMap",
				"org.eclipse.collections.api.map.MutableMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet",
				"org.eclipse.collections.api.set.MutableSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter",
				"org.eclipse.collections.api.set.sorted.MutableSortedSet"
			)
		);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(this.javaFixture("replacePatterns/01"));
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"));
	}
}
