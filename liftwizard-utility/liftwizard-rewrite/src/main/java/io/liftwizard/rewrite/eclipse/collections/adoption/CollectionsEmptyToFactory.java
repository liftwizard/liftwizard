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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class CollectionsEmptyToFactory extends Recipe {

	@Override
	public String getDisplayName() {
		return "Replace Collections.empty*() with Eclipse Collections factories";
	}

	@Override
	public String getDescription() {
		return "Replace `Collections.emptyList()`, `Collections.emptySet()`, `Collections.emptyMap()`, `Collections.emptySortedSet()`, and `Collections.emptySortedMap()` with Eclipse Collections factory methods.";
	}

	@Override
	public Set<String> getTags() {
		return Collections.singleton("eclipse-collections");
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(15);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new CollectionsEmptyToFactoryVisitor();
	}

	private static final class CollectionsEmptyToFactoryVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final MethodMatcher EMPTY_LIST = new MethodMatcher("java.util.Collections emptyList()");
		private static final MethodMatcher EMPTY_SET = new MethodMatcher("java.util.Collections emptySet()");
		private static final MethodMatcher EMPTY_MAP = new MethodMatcher("java.util.Collections emptyMap()");
		private static final MethodMatcher EMPTY_SORTED_SET = new MethodMatcher(
			"java.util.Collections emptySortedSet()"
		);
		private static final MethodMatcher EMPTY_SORTED_MAP = new MethodMatcher(
			"java.util.Collections emptySortedMap()"
		);

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

			String factoryClass = null;
			String factoryMethod = null;

			if (EMPTY_LIST.matches(mi)) {
				factoryClass = "Lists";
				factoryMethod = "fixedSize";
			} else if (EMPTY_SET.matches(mi)) {
				factoryClass = "Sets";
				factoryMethod = "fixedSize";
			} else if (EMPTY_MAP.matches(mi)) {
				factoryClass = "Maps";
				factoryMethod = "fixedSize";
			} else if (EMPTY_SORTED_SET.matches(mi)) {
				factoryClass = "SortedSets";
				factoryMethod = "mutable";
			} else if (EMPTY_SORTED_MAP.matches(mi)) {
				factoryClass = "SortedMaps";
				factoryMethod = "mutable";
			}

			if (factoryClass == null) {
				return mi;
			}

			String typeParams = this.extractTypeParameters(mi);
			String factoryImport = "org.eclipse.collections.api.factory." + factoryClass;
			this.maybeAddImport(factoryImport);
			this.maybeRemoveImport("java.util.Collections");

			String typeParamsTemplate = typeParams.isEmpty() ? "" : "<" + typeParams + ">";
			String templateSource = factoryClass + "." + factoryMethod + "." + typeParamsTemplate + "empty()";

			JavaTemplate template = JavaTemplate.builder(templateSource)
				.imports(factoryImport)
				.contextSensitive()
				.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
				.build();

			return template.apply(this.getCursor(), mi.getCoordinates().replace());
		}

		private String extractTypeParameters(J.MethodInvocation mi) {
			if (mi.getTypeParameters() != null && !mi.getTypeParameters().isEmpty()) {
				return mi
					.getTypeParameters()
					.stream()
					.map((tp) -> this.formatTypeTree(tp))
					.collect(Collectors.joining(", "));
			}
			return "";
		}

		private String formatTypeTree(Object tree) {
			if (tree instanceof J.Identifier identifier) {
				return identifier.getSimpleName();
			}
			if (tree instanceof J.ParameterizedType paramType) {
				String base = this.formatTypeTree(paramType.getClazz());
				if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
					String params = paramType
						.getTypeParameters()
						.stream()
						.map((tp) -> this.formatTypeTree(tp))
						.collect(Collectors.joining(", "));
					return base + "<" + params + ">";
				}
				return base;
			}
			return tree.toString();
		}
	}
}
