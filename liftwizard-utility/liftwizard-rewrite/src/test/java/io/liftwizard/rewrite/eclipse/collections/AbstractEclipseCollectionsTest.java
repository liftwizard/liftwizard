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

package io.liftwizard.rewrite.eclipse.collections;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.style.ImportLayoutStyle;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.Tree.randomId;

public abstract class AbstractEclipseCollectionsTest implements RewriteTest {

	protected static final NamedStyles NO_STAR_IMPORT_STYLE = new NamedStyles(
		randomId(),
		"no-star-imports",
		"No star imports style",
		"Prevents OpenRewrite from collapsing imports into star imports",
		Sets.fixedSize.empty(),
		Lists.fixedSize.with(
			ImportLayoutStyle.builder()
				.classCountToUseStarImport(9999)
				.nameCountToUseStarImport(9999)
				.importPackage("org.eclipse.collections.*")
				.blankLine()
				.importAllOthers()
				.blankLine()
				.importStaticAllOthers()
				.build()
		)
	);

	@Override
	public void defaults(RecipeSpec spec) {
		spec.parser(
			JavaParser.fromJavaVersion()
				.classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
				.styles(Lists.fixedSize.with(NO_STAR_IMPORT_STYLE))
		);
	}
}
