/*
 * Copyright 2026 Craig Motlin
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

import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompositeRecipeGeneratorTest {

	@Test
	void emptyExclusionsThrows() {
		var spec = new FilteredRecipeSpec();
		spec.setGeneratedRecipeName("io.liftwizard.migrate.UpgradeToJava21");
		spec.setBaseRecipeName("org.openrewrite.java.migrate.UpgradeToJava21");
		spec.setDisplayName("Migrate to Java 21");
		spec.setDescription("Upgrade to Java 21.");
		spec.setOutputFileName("upgrade-to-java.yml");

		var generator = new CompositeRecipeGenerator();
		Environment env = Environment.builder().build();

		assertThatThrownBy(() -> generator.generate(env, spec))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("io.liftwizard.migrate.UpgradeToJava21")
			.hasMessageContaining("no exclusions");
	}
}
