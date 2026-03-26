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

package io.liftwizard.rewrite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;
import org.openrewrite.config.RecipeDescriptor;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

class YamlRecipeValidationTest {

	@Test
	void allYamlRecipeReferencesResolve() throws IOException {
		Environment env = Environment.builder()
			.scanRuntimeClasspath()
			.build();

		Set<String> knownRecipeNames = env.listRecipeDescriptors().stream()
			.map(RecipeDescriptor::getName)
			.collect(Collectors.toCollection(LinkedHashSet::new));

		ImmutableList<String> liftwizardReferences = getReferencedRecipeNames()
			.select(name -> name.startsWith("io.liftwizard."));

		assertThat(liftwizardReferences).isNotEmpty();

		assertThat(knownRecipeNames).containsAll(liftwizardReferences);
	}

	private static ImmutableList<String> getReferencedRecipeNames() throws IOException {
		MutableList<String> result = Lists.mutable.empty();
		Yaml yaml = new Yaml();
		Enumeration<URL> resources = Thread.currentThread()
			.getContextClassLoader()
			.getResources("META-INF/rewrite");

		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			if (!"file".equals(url.getProtocol())) {
				continue;
			}

			File dir = new File(url.getFile());
			File[] ymlFiles = dir.listFiles((d, name) -> name.endsWith(".yml"));
			if (ymlFiles == null) {
				continue;
			}

			for (File ymlFile : ymlFiles) {
				try (InputStream is = ymlFile.toURI().toURL().openStream()) {
					for (Object document : yaml.loadAll(is)) {
						if (document instanceof Map<?, ?> map) {
							Object recipeList = map.get("recipeList");
							if (recipeList instanceof List<?> list) {
								for (Object entry : list) {
									if (entry instanceof String recipeName) {
										result.add(recipeName);
									}
								}
							}
						}
					}
				}
			}
		}

		return result.toImmutable();
	}
}
