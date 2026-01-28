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

package io.liftwizard.rewrite.java.style;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesJavaVersion;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Empty;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.NewClass;
import org.openrewrite.java.tree.J.ParameterizedType;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.J.VariableDeclarations.NamedVariable;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.marker.Markers;

/**
 * Replaces explicit type declarations with {@code var} keyword when the initializer is a constructor call with an exactly matching type.
 *
 * <p>This recipe is more conservative than OpenRewrite's {@code UseVarForObject}
 */
public class ExplicitTypeToVar extends Recipe {

	@Override
	public String getDisplayName() {
		return "Explicit type → `var` for constructor assignments";
	}

	@Override
	public String getDescription() {
		return (
			"Replace explicit type declarations with `var` when the variable is initialized with a "
			+ "constructor call of exactly the same type. Does not transform when declared type "
			+ "differs from constructor type (e.g., interface vs implementation)."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesJavaVersion<>(10), new ExplicitTypeToVarVisitor());
	}

	private static final class ExplicitTypeToVarVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public VariableDeclarations visitVariableDeclarations(VariableDeclarations vd, ExecutionContext ctx) {
			VariableDeclarations result = super.visitVariableDeclarations(vd, ctx);

			if (!this.isApplicable(result)) {
				return result;
			}

			return this.transformToVar(result);
		}

		private boolean isApplicable(VariableDeclarations vd) {
			// Must have exactly one variable
			List<NamedVariable> variables = vd.getVariables();
			if (variables.size() != 1) {
				return false;
			}

			NamedVariable variable = variables.get(0);

			// Must have an initializer
			Expression initializer = variable.getInitializer();
			if (initializer == null) {
				return false;
			}

			// Skip null literal initializers
			if (Literal.isLiteralValue(initializer, null)) {
				return false;
			}

			// Initializer must be a constructor call (NewClass)
			if (!(initializer instanceof NewClass)) {
				return false;
			}

			// Must have a type expression (not already var)
			TypeTree typeTree = vd.getTypeExpression();
			if (typeTree == null) {
				return false;
			}

			// Skip if already using var
			if (typeTree instanceof Identifier varId && "var".equals(varId.getSimpleName())) {
				return false;
			}

			// Skip field declarations - var is only allowed for local variables
			if (this.isFieldDeclaration()) {
				return false;
			}

			// Declared type must exactly match constructor type
			return this.typesMatch(vd, (NewClass) initializer);
		}

		private boolean isFieldDeclaration() {
			Cursor parent = this.getCursor().getParentTreeCursor();
			if (parent.getParent() == null) {
				return false;
			}
			Cursor grandparent = parent.getParentTreeCursor();
			return (
				parent.getValue() instanceof J.Block
				&& (grandparent.getValue() instanceof J.ClassDeclaration || grandparent.getValue() instanceof NewClass)
			);
		}

		private boolean typesMatch(VariableDeclarations vd, NewClass newClass) {
			JavaType declaredType = vd.getType();
			JavaType constructorType = newClass.getType();

			if (declaredType == null || constructorType == null) {
				return false;
			}

			String declaredFqn = this.getFullyQualifiedName(declaredType);
			String constructorFqn = this.getFullyQualifiedName(constructorType);

			if (declaredFqn == null || constructorFqn == null) {
				return false;
			}

			return declaredFqn.equals(constructorFqn);
		}

		private String getFullyQualifiedName(JavaType type) {
			if (!(type instanceof JavaType.FullyQualified fq)) {
				return null;
			}
			return fq.getFullyQualifiedName();
		}

		private VariableDeclarations transformToVar(VariableDeclarations vd) {
			NamedVariable variable = vd.getVariables().get(0);
			NewClass initializer = (NewClass) variable.getInitializer();

			Space prefix = vd.getTypeExpression() == null ? Space.EMPTY : vd.getTypeExpression().getPrefix();

			Identifier varIdentifier = new Identifier(
				Tree.randomId(),
				prefix,
				Markers.EMPTY,
				Lists.fixedSize.empty(),
				"var",
				initializer.getType(),
				null
			);

			// Check if we need to transfer type arguments from declared type to constructor
			NewClass newInitializer = this.maybeTransferTypeArguments(vd, initializer);
			if (newInitializer != initializer) {
				NamedVariable newVariable = variable.withInitializer(newInitializer);
				return vd.withTypeExpression(varIdentifier).withVariables(Lists.fixedSize.of(newVariable));
			}

			return vd.withTypeExpression(varIdentifier);
		}

		private NewClass maybeTransferTypeArguments(VariableDeclarations vd, NewClass initializer) {
			TypeTree typeExpression = vd.getTypeExpression();

			// Check if declared type has type parameters
			if (!(typeExpression instanceof ParameterizedType paramType)) {
				return initializer;
			}

			List<Expression> declaredTypeParams = paramType.getTypeParameters();
			if (declaredTypeParams == null || declaredTypeParams.isEmpty()) {
				return initializer;
			}

			// Check if constructor uses diamond operator (empty type args)
			TypeTree constructorClazz = initializer.getClazz();
			if (!(constructorClazz instanceof ParameterizedType constructorParamType)) {
				return initializer;
			}

			List<Expression> constructorTypeParams = constructorParamType.getTypeParameters();
			if (constructorTypeParams == null || !this.isDiamondOperator(constructorTypeParams)) {
				// Not using diamond, or already has explicit type args
				return initializer;
			}

			// Transfer type arguments from declared type to constructor
			ParameterizedType newClazz = constructorParamType.withTypeParameters(declaredTypeParams);
			return initializer.withClazz(newClazz);
		}

			private boolean isDiamondOperator(List<Expression> typeParams) {
			// Diamond operator <> is represented as a list with a single J.Empty element or as an empty list
			return typeParams.isEmpty() || typeParams.stream().allMatch(Empty.class::isInstance);
		}
	}
}
