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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.OrderImports;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public abstract class AbstractJCFConstructorToFactoryRecipe extends Recipe {

	private final String sourceTypeSimpleName;
	private final String targetFactorySimpleName;

	protected AbstractJCFConstructorToFactoryRecipe(String sourceTypeSimpleName, String targetFactorySimpleName) {
		this.sourceTypeSimpleName = Objects.requireNonNull(sourceTypeSimpleName);
		this.targetFactorySimpleName = Objects.requireNonNull(targetFactorySimpleName);
	}

	@Override
	public final Set<String> getTags() {
		return Sets.fixedSize.with("eclipse-collections");
	}

	@Override
	public final Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(10);
	}

	@Override
	public final TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ConstructorToFactoryVisitor(this.sourceTypeSimpleName, this.targetFactorySimpleName);
	}

	private static final class ConstructorToFactoryVisitor extends JavaVisitor<ExecutionContext> {

		private final String sourceTypeSimpleName;
		private final String targetFactorySimpleName;

		private ConstructorToFactoryVisitor(String sourceTypeSimpleName, String targetFactorySimpleName) {
			this.sourceTypeSimpleName = Objects.requireNonNull(sourceTypeSimpleName);
			this.targetFactorySimpleName = Objects.requireNonNull(targetFactorySimpleName);
		}

		@Override
		public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
			J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

			JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
			if (type == null || !("java.util." + this.sourceTypeSimpleName).equals(type.getFullyQualifiedName())) {
				return nc;
			}

			List<Expression> arguments = nc.getArguments();

			boolean isEmptyConstructor =
				arguments.isEmpty() || (arguments.size() == 1 && arguments.get(0) instanceof J.Empty);
			boolean isInitialCapacityConstructor =
				arguments.size() == 1
				&& !(arguments.get(0) instanceof J.Empty)
				&& isNumericType(arguments.get(0).getType());
			boolean isComparatorConstructor =
				arguments.size() == 1
				&& !(arguments.get(0) instanceof J.Empty)
				&& isComparatorType(arguments.get(0).getType());
			boolean isCollectionConstructor =
				arguments.size() == 1
				&& !(arguments.get(0) instanceof J.Empty)
				&& !isNumericType(arguments.get(0).getType())
				&& !isComparatorType(arguments.get(0).getType());

			if (
				!isEmptyConstructor
				&& !isInitialCapacityConstructor
				&& !isComparatorConstructor
				&& !isCollectionConstructor
			) {
				return nc;
			}

			if (this.isVariableTypeConcreteClass()) {
				return nc;
			}

			String typeParams = this.extractTypeParameterString(nc);

			this.maybeRemoveImport("java.util." + this.sourceTypeSimpleName);
			this.maybeAddImport("org.eclipse.collections.api.factory." + this.targetFactorySimpleName);
			this.doAfterVisit(new OrderImports(false).getVisitor());

			String typeParamsTemplate = typeParams.isEmpty() ? "" : "<" + typeParams + ">";
			String prefix = this.targetFactorySimpleName + ".mutable." + typeParamsTemplate;
			String templateSource =
				prefix
				+ this.getTemplateSource(
					isInitialCapacityConstructor,
					isComparatorConstructor,
					isCollectionConstructor
				);
			JavaTemplate template = JavaTemplate.builder(templateSource)
				.imports("org.eclipse.collections.api.factory." + this.targetFactorySimpleName)
				.contextSensitive()
				.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"))
				.build();

			if (isInitialCapacityConstructor || isComparatorConstructor || isCollectionConstructor) {
				return template.apply(this.getCursor(), nc.getCoordinates().replace(), arguments.get(0));
			} else {
				return template.apply(this.getCursor(), nc.getCoordinates().replace());
			}
		}

		private String getTemplateSource(
			boolean isInitialCapacityConstructor,
			boolean isComparatorConstructor,
			boolean isCollectionConstructor
		) {
			if (isInitialCapacityConstructor) {
				return "withInitialCapacity(#{any(int)})";
			}

			if (isComparatorConstructor) {
				return "with(#{any(java.util.Comparator)})";
			}

			if (!isCollectionConstructor) {
				return "empty()";
			}

			return this.getMethodName() + "(#{any(" + this.getParamType() + ")})";
		}

		private String getMethodName() {
			if (this.sourceTypeSimpleName.equals("TreeMap")) {
				return "withSortedMap";
			}
			if (this.sourceTypeSimpleName.contains("Map")) {
				return "withMap";
			}
			return "withAll";
		}

		private String getParamType() {
			return this.sourceTypeSimpleName.equals("TreeMap") || this.sourceTypeSimpleName.contains("Map")
				? "java.util.Map"
				: "java.lang.Iterable";
		}

		private static boolean isNumericType(JavaType type) {
			if (!(type instanceof JavaType.Primitive primitive)) {
				return false;
			}
			return switch (primitive) {
				case Int, Long, Short, Byte -> true;
				default -> false;
			};
		}

		private static boolean isComparatorType(JavaType type) {
			JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type);
			return fullyQualified != null && "java.util.Comparator".equals(fullyQualified.getFullyQualifiedName());
		}

		private String extractTypeParameterString(J.NewClass nc) {
			if (!(nc.getClazz() instanceof J.ParameterizedType parameterizedType)) {
				return "";
			}

			List<Expression> typeParameters = parameterizedType.getTypeParameters();
			if (Iterate.isEmpty(typeParameters)) {
				return "";
			}

			boolean hasActualTypeParams = typeParameters.stream().anyMatch((tp) -> !(tp instanceof J.Empty));
			if (!hasActualTypeParams) {
				return "";
			}

			return typeParameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
		}

		private boolean isVariableTypeConcreteClass() {
			Cursor parentTreeCursor = this.getCursor().getParentTreeCursor();
			if (!(parentTreeCursor.getValue() instanceof J.VariableDeclarations.NamedVariable)) {
				return false;
			}

			if (!(parentTreeCursor.getParentTreeCursor().getValue() instanceof J.VariableDeclarations variableDecls)) {
				return false;
			}

			JavaType.FullyQualified variableType = TypeUtils.asFullyQualified(variableDecls.getType());
			if (variableType == null) {
				return false;
			}

			String variableTypeName = variableType.getFullyQualifiedName();
			return ("java.util." + this.sourceTypeSimpleName).equals(variableTypeName);
		}
	}
}
