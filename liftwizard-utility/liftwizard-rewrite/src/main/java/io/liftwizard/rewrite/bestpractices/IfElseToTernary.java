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

package io.liftwizard.rewrite.bestpractices;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.SemanticallyEqual;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class IfElseToTernary extends Recipe {

	@Override
	public String getDisplayName() {
		return "`if`/`else` with one differing expression to ternary";
	}

	@Override
	public String getDescription() {
		return (
			"Replaces an `if`/`else` statement whose two branches are identical except for a single expression "
			+ "with one statement that uses a ternary (conditional) expression for the differing part. "
			+ "Handles a differing method argument, a differing method receiver, and a differing assignment value. "
			+ "Assumes the condition, the method receiver, and the surrounding arguments are free of side effects, "
			+ "since the transformation can change their evaluation order."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new IfElseToTernaryVisitor();
	}

	private record TernarySlot(Expression thenExpression, Expression elseExpression, boolean needsParentheses) {}

	private static final class IfElseToTernaryVisitor extends JavaVisitor<ExecutionContext> {

		@Override
		public J visitIf(J.If iff, ExecutionContext ctx) {
			J.If.Else elsePart = iff.getElsePart();
			if (elsePart == null) {
				return super.visitIf(iff, ctx);
			}

			Statement thenStatement = unwrapSingleStatement(iff.getThenPart());
			Statement elseStatement = unwrapSingleStatement(elsePart.getBody());
			if (thenStatement == null || elseStatement == null) {
				return super.visitIf(iff, ctx);
			}

			TernarySlot slot = findSingleDifference(thenStatement, elseStatement);
			if (slot == null) {
				return super.visitIf(iff, ctx);
			}

			if (slot.thenExpression() instanceof J.Ternary || slot.elseExpression() instanceof J.Ternary) {
				return super.visitIf(iff, ctx);
			}

			Expression condition = iff.getIfCondition().getTree();
			String templateCode = slot.needsParentheses()
				? "(#{any()} ? #{any()} : #{any()})"
				: "#{any()} ? #{any()} : #{any()}";
			JavaTemplate template = JavaTemplate.builder(templateCode).build();

			var transformed = (Statement) new TernaryInsertionVisitor(slot, condition, template).visit(
				thenStatement,
				ctx,
				getCursor()
			);

			Statement result = transformed.withPrefix(iff.getPrefix());

			Object parentValue = getCursor().getParentTreeCursor().getValue();
			if (parentValue instanceof J.If.Else && !(result instanceof J.Block)) {
				J.Block block = J.Block.createEmptyBlock().withStatements(List.of(result));
				return this.autoFormat(block.withPrefix(iff.getPrefix()), ctx);
			}

			return this.autoFormat(result, ctx);
		}

		private static Statement unwrapSingleStatement(Statement statement) {
			if (statement instanceof J.Block block) {
				List<Statement> statements = block.getStatements();
				if (statements.size() != 1) {
					return null;
				}
				return statements.get(0);
			}
			return statement;
		}

		private static TernarySlot findSingleDifference(Statement thenStatement, Statement elseStatement) {
			if (
				thenStatement instanceof J.MethodInvocation thenInvocation
				&& elseStatement instanceof J.MethodInvocation elseInvocation
			) {
				return findMethodInvocationDifference(thenInvocation, elseInvocation);
			}
			if (
				thenStatement instanceof J.Assignment thenAssignment
				&& elseStatement instanceof J.Assignment elseAssignment
			) {
				return findAssignmentDifference(thenAssignment, elseAssignment);
			}
			return null;
		}

		private static TernarySlot findMethodInvocationDifference(
			J.MethodInvocation thenInvocation,
			J.MethodInvocation elseInvocation
		) {
			if (!thenInvocation.getSimpleName().equals(elseInvocation.getSimpleName())) {
				return null;
			}

			List<Expression> thenArguments = thenInvocation.getArguments();
			List<Expression> elseArguments = elseInvocation.getArguments();
			if (thenArguments.size() != elseArguments.size()) {
				return null;
			}

			Expression thenSelect = thenInvocation.getSelect();
			Expression elseSelect = elseInvocation.getSelect();
			boolean selectsDiffer = !selectsEqual(thenSelect, elseSelect);

			int differingArgumentIndex = -1;
			int differingArgumentCount = 0;
			for (int i = 0; i < thenArguments.size(); i++) {
				if (!SemanticallyEqual.areEqual(thenArguments.get(i), elseArguments.get(i))) {
					differingArgumentIndex = i;
					differingArgumentCount++;
				}
			}

			int totalDifferences = differingArgumentCount + (selectsDiffer ? 1 : 0);
			if (totalDifferences != 1) {
				return null;
			}

			if (selectsDiffer) {
				if (thenSelect == null || elseSelect == null) {
					return null;
				}
				if (
					thenSelect instanceof J.MethodInvocation thenSelectInvocation
					&& elseSelect instanceof J.MethodInvocation elseSelectInvocation
				) {
					TernarySlot deeper = findMethodInvocationDifference(thenSelectInvocation, elseSelectInvocation);
					if (deeper != null) {
						return deeper;
					}
				}
				return new TernarySlot(thenSelect, elseSelect, true);
			}

			Expression thenArgument = thenArguments.get(differingArgumentIndex);
			Expression elseArgument = elseArguments.get(differingArgumentIndex);
			if (
				thenArgument instanceof J.MethodInvocation thenArgumentInvocation
				&& elseArgument instanceof J.MethodInvocation elseArgumentInvocation
			) {
				TernarySlot deeper = findMethodInvocationDifference(thenArgumentInvocation, elseArgumentInvocation);
				if (deeper != null) {
					return deeper;
				}
			}
			return new TernarySlot(thenArgument, elseArgument, false);
		}

		private static TernarySlot findAssignmentDifference(J.Assignment thenAssignment, J.Assignment elseAssignment) {
			if (!SemanticallyEqual.areEqual(thenAssignment.getVariable(), elseAssignment.getVariable())) {
				return null;
			}

			Expression thenValue = thenAssignment.getAssignment();
			Expression elseValue = elseAssignment.getAssignment();
			if (SemanticallyEqual.areEqual(thenValue, elseValue)) {
				return null;
			}

			return new TernarySlot(thenValue, elseValue, false);
		}

		private static boolean selectsEqual(Expression thenSelect, Expression elseSelect) {
			if (thenSelect == null && elseSelect == null) {
				return true;
			}
			if (thenSelect == null || elseSelect == null) {
				return false;
			}
			return SemanticallyEqual.areEqual(thenSelect, elseSelect);
		}
	}

	private static final class TernaryInsertionVisitor extends JavaVisitor<ExecutionContext> {

		private final TernarySlot slot;
		private final Expression condition;
		private final JavaTemplate template;

		private TernaryInsertionVisitor(TernarySlot slot, Expression condition, JavaTemplate template) {
			this.slot = slot;
			this.condition = condition;
			this.template = template;
		}

		@Override
		public J visitExpression(Expression expression, ExecutionContext ctx) {
			if (expression.getId().equals(this.slot.thenExpression().getId())) {
				return this.template.apply(
					getCursor(),
					expression.getCoordinates().replace(),
					this.condition,
					this.slot.thenExpression(),
					this.slot.elseExpression()
				);
			}
			return super.visitExpression(expression, ctx);
		}
	}
}
