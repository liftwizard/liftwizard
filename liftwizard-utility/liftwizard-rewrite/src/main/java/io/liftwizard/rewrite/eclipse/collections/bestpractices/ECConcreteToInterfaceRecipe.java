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
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.RemoveUnusedImports;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Configurable recipe that replaces Eclipse Collections concrete types
 * with their interface equivalents.
 *
 * <p>For example, replaces {@code FastList<T>} with {@code MutableList<T>}.
 *
 * <p>This recipe only transforms variable declarations (local variables and final fields),
 * not method parameters or return types.
 *
 * <p>Cannot be reimplemented as Refaster templates because:
 * <ol>
 *   <li>Refaster only matches expression patterns, not type declarations</li>
 *   <li>This recipe analyzes declared types and conditionally replaces type expressions</li>
 *   <li>Refaster cannot selectively replace just the type portion of a variable declaration</li>
 * </ol>
 *
 * <p>This transformation requires semantic analysis of the type system via JavaIsoVisitor,
 * not structural pattern matching.
 */
public class ECConcreteToInterfaceRecipe extends Recipe {

	@Option(
		displayName = "Concrete type",
		description = "Fully qualified name of the concrete Eclipse Collections implementation class.",
		example = "org.eclipse.collections.impl.list.mutable.FastList"
	)
	private final String concreteTypeFqn;

	@Option(
		displayName = "Interface type",
		description = "Fully qualified name of the Eclipse Collections interface to use instead.",
		example = "org.eclipse.collections.api.list.MutableList"
	)
	private final String interfaceTypeFqn;

	@JsonCreator
	public ECConcreteToInterfaceRecipe(
		@JsonProperty("concreteTypeFqn") String concreteTypeFqn,
		@JsonProperty("interfaceTypeFqn") String interfaceTypeFqn
	) {
		this.concreteTypeFqn = concreteTypeFqn;
		this.interfaceTypeFqn = interfaceTypeFqn;
	}

	@Override
	public String getDisplayName() {
		String concreteSimpleName = this.getSimpleName(this.concreteTypeFqn);
		String interfaceSimpleName = this.getSimpleName(this.interfaceTypeFqn);
		String typeParams = concreteSimpleName.contains("Map") ? "<K, V>" : "<T>";
		return '`' + concreteSimpleName + typeParams + "` → `" + interfaceSimpleName + typeParams + '`';
	}

	@Override
	public String getDescription() {
		String concreteSimpleName = this.getSimpleName(this.concreteTypeFqn);
		String interfaceSimpleName = this.getSimpleName(this.interfaceTypeFqn);
		String typeParams = concreteSimpleName.contains("Map") ? "<K, V>" : "<T>";
		return MessageFormat.format(
			"Replace `{0}{1}` with `{2}{1}` in variable declarations.",
			concreteSimpleName,
			typeParams,
			interfaceSimpleName
		);
	}

	private String getSimpleName(String fullyQualifiedName) {
		int lastDot = fullyQualifiedName.lastIndexOf('.');
		return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
	}

	@Override
	public Set<String> getTags() {
		return Collections.singleton("eclipse-collections");
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(10);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ECConcreteToInterfaceVisitor(this.concreteTypeFqn, this.interfaceTypeFqn);
	}

	private static final class ECConcreteToInterfaceVisitor extends JavaIsoVisitor<ExecutionContext> {

		private final String concreteTypeFqn;
		private final String interfaceTypeFqn;
		private final String interfaceSimpleName;

		private ECConcreteToInterfaceVisitor(String concreteTypeFqn, String interfaceTypeFqn) {
			this.concreteTypeFqn = Objects.requireNonNull(concreteTypeFqn);
			this.interfaceTypeFqn = Objects.requireNonNull(interfaceTypeFqn);
			int lastDot = interfaceTypeFqn.lastIndexOf('.');
			this.interfaceSimpleName = lastDot >= 0 ? interfaceTypeFqn.substring(lastDot + 1) : interfaceTypeFqn;
		}

		@Override
		public J.VariableDeclarations visitVariableDeclarations(
			J.VariableDeclarations multiVariable,
			ExecutionContext ctx
		) {
			boolean isField = multiVariable
				.getVariables()
				.stream()
				.anyMatch((namedVariable) -> namedVariable.isField(getCursor()));
			boolean isFinal = multiVariable.hasModifier(J.Modifier.Type.Final);

			J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

			if (vd.getTypeExpression() == null || !this.isConcreteType(vd.getTypeExpression())) {
				return vd;
			}

			if (isField && !isFinal) {
				return vd;
			}

			boolean hasInitializer = vd
				.getVariables()
				.stream()
				.anyMatch((variable) -> variable.getInitializer() != null);

			if (!hasInitializer) {
				return vd;
			}

			TypeTree typeExpr = vd.getTypeExpression();
			TypeTree newTypeExpr = this.getNewTypeExpr(typeExpr);

			if (newTypeExpr == null) {
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			this.maybeAddImport(this.interfaceTypeFqn);
			this.doAfterVisit(new RemoveUnusedImports().getVisitor());
			return vd.withTypeExpression(newTypeExpr);
		}

		private TypeTree getNewTypeExpr(TypeTree typeExpr) {
			if (typeExpr instanceof J.Identifier) {
				return ((J.Identifier) typeExpr).withSimpleName(this.interfaceSimpleName).withType(
					JavaType.buildType(this.interfaceTypeFqn)
				);
			}

			if (typeExpr instanceof J.FieldAccess) {
				return new J.Identifier(
					UUID.randomUUID(),
					typeExpr.getPrefix(),
					typeExpr.getMarkers(),
					Collections.emptyList(),
					this.interfaceSimpleName,
					JavaType.buildType(this.interfaceTypeFqn),
					null
				);
			}

			if (!(typeExpr instanceof J.ParameterizedType paramType)) {
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			J clazz = paramType.getClazz();

			if (clazz instanceof J.Identifier) {
				J.Identifier newClazz = ((J.Identifier) clazz).withSimpleName(this.interfaceSimpleName).withType(
					JavaType.buildType(this.interfaceTypeFqn)
				);
				return paramType.withClazz(newClazz);
			}

			if (clazz instanceof J.FieldAccess) {
				J.Identifier interfaceTypeIdent = new J.Identifier(
					UUID.randomUUID(),
					clazz.getPrefix(),
					clazz.getMarkers(),
					Collections.emptyList(),
					this.interfaceSimpleName,
					JavaType.buildType(this.interfaceTypeFqn),
					null
				);
				return paramType.withClazz(interfaceTypeIdent);
			}

			throw new AssertionError("Unexpected parameterized type class: " + clazz.getClass().getSimpleName());
		}

		private boolean isConcreteType(J typeExpression) {
			J currentExpression = typeExpression;
			while (true) {
				if (currentExpression instanceof J.Identifier identifier) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
					return type != null && this.concreteTypeFqn.equals(type.getFullyQualifiedName());
				}
				if (currentExpression instanceof J.ParameterizedType paramType) {
					currentExpression = paramType.getClazz();
					continue;
				}
				if (currentExpression instanceof J.FieldAccess fieldAccess) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
					return type != null && this.concreteTypeFqn.equals(type.getFullyQualifiedName());
				}

				return false;
			}
		}
	}
}
