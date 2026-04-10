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

package io.liftwizard.generator.rewrite;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

public class FilteredRecipeSpec {

	private String generatedRecipeName;
	private String displayName;
	private String description;
	private String baseRecipeName;
	private List<RecipeExclusion> exclusions = Lists.mutable.empty();
	private String outputFileName;

	public FilteredRecipeSpec() {}

	public String getGeneratedRecipeName() {
		return this.generatedRecipeName;
	}

	public void setGeneratedRecipeName(String generatedRecipeName) {
		this.generatedRecipeName = generatedRecipeName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBaseRecipeName() {
		return this.baseRecipeName;
	}

	public void setBaseRecipeName(String baseRecipeName) {
		this.baseRecipeName = baseRecipeName;
	}

	public List<RecipeExclusion> getExclusions() {
		return this.exclusions;
	}

	public void setExclusions(List<RecipeExclusion> exclusions) {
		this.exclusions = exclusions;
	}

	public String getOutputFileName() {
		return this.outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
}
