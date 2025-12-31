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

public class ECSortedMapConstructorToFactory extends AbstractECConstructorToFactoryRecipe {

	public ECSortedMapConstructorToFactory() {
		super("TreeSortedMap", "map.sorted", "SortedMaps", "", "mutable");
	}

	@Override
	public String getDisplayName() {
		return "`new TreeSortedMap<>()` → `SortedMaps.mutable.empty()`";
	}

	@Override
	public String getDescription() {
		return "Replace `new TreeSortedMap()` constructor calls with `SortedMaps.mutable.empty()`.";
	}
}
