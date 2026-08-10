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

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.openrewrite.config.Environment;
import org.openrewrite.style.NamedStyles;

/**
 * Applies the same styles to the recipe tests that the rewrite-maven-plugin profiles apply to real consumers, so that
 * the fixtures record what those consumers will actually get.
 *
 * <p>The styles are loaded from META-INF/rewrite/styles.yml rather than rebuilt here, so that there is one definition
 * of them rather than a test copy that can drift from the shipped one.
 */
public interface AbstractRewriteStyles {
	String STYLE_NAME = "io.liftwizard.NoStarImports";

	ImmutableList<NamedStyles> STYLES = loadStyles();

	static ImmutableList<NamedStyles> styles(NamedStyles... additionalStyles) {
		return Lists.immutable.with(additionalStyles).newWithAll(STYLES);
	}

	private static ImmutableList<NamedStyles> loadStyles() {
		Environment environment = Environment.builder().scanRuntimeClasspath("io.liftwizard").build();
		ImmutableList<NamedStyles> result = Lists.immutable.withAll(environment.activateStyles(STYLE_NAME));

		// activateStyles skips a style it cannot find rather than failing, which would leave the fixtures passing only
		// because OpenRewrite autodetected a style from the sources it parses.
		if (result.isEmpty()) {
			throw new AssertionError(
				"Found no style named %s on the classpath. It is declared in %s.".formatted(
						STYLE_NAME,
						"liftwizard-rewrite/src/main/resources/META-INF/rewrite/styles.yml"
					)
			);
		}

		return result;
	}
}
