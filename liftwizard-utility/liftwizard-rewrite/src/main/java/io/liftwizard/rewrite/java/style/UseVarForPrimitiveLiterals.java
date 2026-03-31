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
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.NewClass;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.J.VariableDeclarations.NamedVariable;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.Primitive;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

/**
 * Replaces explicit primitive and String type declarations with {@code var} when the initializer is a literal value.
 *
 * <p>This is a conservative alternative to OpenRewrite's {@code UseVarForPrimitive}, which aggressively converts
 * all primitive-typed variables including method return values. This recipe only targets literal assignments
 * where the type is obvious from the value itself.
 *
 * <p>Skips {@code byte} and {@code short} literals because their type is not obvious from the literal value
 * (they look like {@code int} literals). Adds type suffixes ({@code L}, {@code F}, {@code D}) when needed
 * to preserve type information.
 */
public class UseVarForPrimitiveLiterals extends Recipe {

	@Override
	public String getDisplayName() {
		return "Use `var` for primitive and String literal assignments";
	}

	@Override
	public String getDescription() {
		return (
			"Replace explicit type declarations with `var` when the variable is initialized with a "
			+ "literal value (primitive or String). Does not transform method return values, "
			+ "constructor calls, or other non-literal initializers. Skips `byte` and `short` "
			+ "because their literals are indistinguishable from `int` literals."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesJavaVersion<>(10), new UseVarForPrimitiveLiteralsVisitor());
	}

	private static final class UseVarForPrimitiveLiteralsVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public VariableDeclarations visitVariableDeclarations(VariableDeclarations vd, ExecutionContext ctx) {
			VariableDeclarations result = super.visitVariableDeclarations(vd, ctx);

			if (!this.isApplicable(result)) {
				return result;
			}

			return this.transformToVar(result);
		}

		private boolean isApplicable(VariableDeclarations vd) {
			List<NamedVariable> variables = vd.getVariables();
			if (variables.size() != 1) {
				return false;
			}

			NamedVariable variable = variables.get(0);

			Expression initializer = variable.getInitializer();
			if (initializer == null) {
				return false;
			}

			if (!(initializer instanceof Literal literal)) {
				return false;
			}

			if (Literal.isLiteralValue(initializer, null)) {
				return false;
			}

			var typeTree = vd.getTypeExpression();
			if (typeTree == null) {
				return false;
			}

			if (typeTree instanceof Identifier varId && "var".equals(varId.getSimpleName())) {
				return false;
			}

			if (this.isFieldDeclaration()) {
				return false;
			}

			JavaType type = vd.getType();
			if (type == null) {
				return false;
			}

			// Skip byte and short: their literals look like int literals
			if (type == Primitive.Byte || type == Primitive.Short) {
				return false;
			}

			return true;
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

		private VariableDeclarations transformToVar(VariableDeclarations vd) {
			NamedVariable variable = vd.getVariables().get(0);
			Literal literal = (Literal) variable.getInitializer();

			Space prefix = vd.getTypeExpression() == null ? Space.EMPTY : vd.getTypeExpression().getPrefix();

			Identifier varIdentifier = new Identifier(
				Tree.randomId(),
				prefix,
				Markers.EMPTY,
				Lists.fixedSize.empty(),
				"var",
				vd.getType(),
				null
			);

			Literal expandedLiteral = this.maybeAddTypeSuffix(vd, literal);
			if (expandedLiteral != literal) {
				NamedVariable newVariable = variable.withInitializer(expandedLiteral);
				return vd.withTypeExpression(varIdentifier).withVariables(Lists.fixedSize.of(newVariable));
			}

			return vd.withTypeExpression(varIdentifier);
		}

		private Literal maybeAddTypeSuffix(VariableDeclarations vd, Literal literal) {
			String valueSource = literal.getValueSource();
			if (valueSource == null) {
				return literal;
			}

			JavaType type = vd.getType();

			if (type == Primitive.Long && !valueSource.endsWith("l") && !valueSource.endsWith("L")) {
				return literal.withValueSource(valueSource + "L");
			}

			if (type == Primitive.Float && !valueSource.endsWith("f") && !valueSource.endsWith("F")) {
				return literal.withValueSource(valueSource + "F");
			}

			if (
				type == Primitive.Double
				&& !valueSource.endsWith("d")
				&& !valueSource.endsWith("D")
				&& !valueSource.contains(".")
			) {
				return literal.withValueSource(valueSource + "D");
			}

			return literal;
		}
	}
}
