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

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.RemoveUnusedImports;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Cannot be reimplemented as Refaster templates because:
 * 1. Refaster only matches expression patterns, not type declarations
 * 2. This recipe analyzes the type of initializers and conditionally replaces variable type expressions
 * 3. Refaster cannot perform type compatibility analysis (checking if initializer type is assignable to target type)
 * 4. Refaster cannot selectively replace just the type portion of a variable declaration
 *
 * This transformation requires semantic analysis of the type system via JavaIsoVisitor,
 * not structural pattern matching.
 */
public abstract class AbstractJCFTypeToMutableTypeRecipe extends Recipe {

	private final String jcfInterface;
	private final String ecPackage;
	private final String ecInterface;

	protected AbstractJCFTypeToMutableTypeRecipe(String jcfInterface, String ecPackage, String ecInterface) {
		this.jcfInterface = Objects.requireNonNull(jcfInterface);
		this.ecPackage = Objects.requireNonNull(ecPackage);
		this.ecInterface = Objects.requireNonNull(ecInterface);
	}

	@Override
	public final String getDisplayName() {
		String typeParams = this.jcfInterface.equals("Map") ? "<K, V>" : "<T>";
		return '`' + this.jcfInterface + typeParams + "` → `" + this.ecInterface + typeParams + '`';
	}

	@Override
	public final String getDescription() {
		String typeParams = this.jcfInterface.equals("Map") ? "<K, V>" : "<T>";
		return MessageFormat.format(
			"Replace `java.util.{0}{1}` with `org.eclipse.collections.api.{2}.{3}{1}` when the variable is initialized with a {3}.",
			this.jcfInterface,
			typeParams,
			this.ecPackage,
			this.ecInterface
		);
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
		return new JCFTypeToMutableTypeVisitor(this.jcfInterface, this.ecPackage, this.ecInterface);
	}

	private static final class JCFTypeToMutableTypeVisitor extends JavaIsoVisitor<ExecutionContext> {

		private final String jcfInterface;
		private final String ecPackage;
		private final String ecInterface;

		private JCFTypeToMutableTypeVisitor(String jcfInterface, String ecPackage, String ecInterface) {
			this.jcfInterface = Objects.requireNonNull(jcfInterface);
			this.ecPackage = Objects.requireNonNull(ecPackage);
			this.ecInterface = Objects.requireNonNull(ecInterface);
		}

		@Override
		public J.VariableDeclarations visitVariableDeclarations(
			J.VariableDeclarations multiVariable,
			ExecutionContext ctx
		) {
			boolean isField = multiVariable
				.getVariables()
				.stream()
				.anyMatch((namedVariable) -> namedVariable.isField(this.getCursor()));
			boolean isFinal = multiVariable.hasModifier(J.Modifier.Type.Final);

			J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

			if (vd.getTypeExpression() == null || !this.isJavaUtilType(vd.getTypeExpression())) {
				return vd;
			}

			if (isField && !isFinal) {
				return vd;
			}

			String fullyQualifiedName = this.ecPackage + '.' + this.ecInterface;
			boolean shouldTransform = vd
				.getVariables()
				.stream()
				.filter((variable) -> variable.getInitializer() != null)
				.map((variable) -> variable.getInitializer().getType())
				.filter(Objects::nonNull)
				.anyMatch((initializerType) -> TypeUtils.isAssignableTo(fullyQualifiedName, initializerType));

			if (!shouldTransform) {
				return vd;
			}

			TypeTree typeExpr = vd.getTypeExpression();
			TypeTree newTypeExpr = this.getNewTypeExpr(typeExpr, fullyQualifiedName);

			if (newTypeExpr == null) {
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			this.maybeAddImport(fullyQualifiedName);
			this.doAfterVisit(new RemoveUnusedImports().getVisitor());
			return vd.withTypeExpression(newTypeExpr);
		}

		private TypeTree getNewTypeExpr(TypeTree typeExpr, String fullyQualifiedName) {
			if (typeExpr instanceof J.Identifier) {
				return ((J.Identifier) typeExpr).withSimpleName(this.ecInterface).withType(
					JavaType.buildType(fullyQualifiedName)
				);
			}

			if (typeExpr instanceof J.FieldAccess) {
				return new J.Identifier(
					Tree.randomId(),
					typeExpr.getPrefix(),
					typeExpr.getMarkers(),
					Lists.fixedSize.empty(),
					this.ecInterface,
					JavaType.buildType(fullyQualifiedName),
					null
				);
			}

			if (!(typeExpr instanceof J.ParameterizedType paramType)) {
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			J clazz = paramType.getClazz();

			if (clazz instanceof J.Identifier) {
				J.Identifier newClazz = ((J.Identifier) clazz).withSimpleName(this.ecInterface).withType(
					JavaType.buildType(fullyQualifiedName)
				);
				return paramType.withClazz(newClazz);
			}

			if (clazz instanceof J.FieldAccess) {
				J.Identifier mutableTypeIdent = new J.Identifier(
					Tree.randomId(),
					clazz.getPrefix(),
					clazz.getMarkers(),
					Lists.fixedSize.empty(),
					this.ecInterface,
					JavaType.buildType(fullyQualifiedName),
					null
				);
				return paramType.withClazz(mutableTypeIdent);
			}

			throw new AssertionError("Unexpected parameterized type class: " + clazz.getClass().getSimpleName());
		}

		private boolean isJavaUtilType(J typeExpression) {
			J currentExpression = typeExpression;
			while (true) {
				String javaUtilTypeName = this.jcfInterface;
				if (currentExpression instanceof J.Identifier identifier) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
					return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
				}
				if (currentExpression instanceof J.ParameterizedType paramType) {
					currentExpression = paramType.getClazz();
					continue;
				}
				if (currentExpression instanceof J.FieldAccess fieldAccess) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
					return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
				}

				return false;
			}
		}
	}
}
