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

package io.liftwizard.rewrite.java.logging;

import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

public class RemoveUnnecessaryLogLevelGuards extends Recipe {

	private static final Set<String> LOGGER_METHODS = Set.of("trace", "debug", "info", "warn", "error");

	private static final MethodMatcher IS_TRACE_ENABLED = new MethodMatcher("org.slf4j.Logger isTraceEnabled()");
	private static final MethodMatcher IS_DEBUG_ENABLED = new MethodMatcher("org.slf4j.Logger isDebugEnabled()");
	private static final MethodMatcher IS_INFO_ENABLED = new MethodMatcher("org.slf4j.Logger isInfoEnabled()");
	private static final MethodMatcher IS_WARN_ENABLED = new MethodMatcher("org.slf4j.Logger isWarnEnabled()");
	private static final MethodMatcher IS_ERROR_ENABLED = new MethodMatcher("org.slf4j.Logger isErrorEnabled()");

	private static final MethodMatcher IS_TRACE_ENABLED_MARKER = new MethodMatcher(
		"org.slf4j.Logger isTraceEnabled(org.slf4j.Marker)"
	);
	private static final MethodMatcher IS_DEBUG_ENABLED_MARKER = new MethodMatcher(
		"org.slf4j.Logger isDebugEnabled(org.slf4j.Marker)"
	);
	private static final MethodMatcher IS_INFO_ENABLED_MARKER = new MethodMatcher(
		"org.slf4j.Logger isInfoEnabled(org.slf4j.Marker)"
	);
	private static final MethodMatcher IS_WARN_ENABLED_MARKER = new MethodMatcher(
		"org.slf4j.Logger isWarnEnabled(org.slf4j.Marker)"
	);
	private static final MethodMatcher IS_ERROR_ENABLED_MARKER = new MethodMatcher(
		"org.slf4j.Logger isErrorEnabled(org.slf4j.Marker)"
	);

	@Override
	public String getDisplayName() {
		return "Remove unnecessary log level guards";
	}

	@Override
	public String getDescription() {
		return "Remove if-statement guards around SLF4J logging calls when parameterized logging makes them unnecessary.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new RemoveLogLevelGuardsVisitor();
	}

	private static final class RemoveLogLevelGuardsVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
			J.Block visited = super.visitBlock(block, ctx);

			List<Statement> newStatements = Lists.mutable.empty();
			boolean modified = false;

			for (Statement stmt : visited.getStatements()) {
				if (stmt instanceof J.If ifStmt && this.shouldRemoveGuard(ifStmt)) {
					List<Statement> bodyStatements = this.extractStatements(ifStmt.getThenPart());

					// Compute the indentation for extracted statements (same level as if-statement)
					Space ifIndent = this.computeIndentationOnly(ifStmt.getPrefix());

					for (int i = 0; i < bodyStatements.size(); i++) {
						Statement bodyStmt = bodyStatements.get(i);
						if (i == 0) {
							// First statement gets the if-statement's prefix (including any comments)
							newStatements.add(bodyStmt.withPrefix(ifStmt.getPrefix()));
						} else {
							// Subsequent statements get the same indentation as the if-statement (without comments)
							newStatements.add(bodyStmt.withPrefix(ifIndent));
						}
					}
					modified = true;
				} else {
					newStatements.add(stmt);
				}
			}

			if (modified) {
				return visited.withStatements(newStatements);
			}
			return visited;
		}

		private boolean shouldRemoveGuard(J.If ifStatement) {
			if (ifStatement.getElsePart() != null) {
				return false;
			}

			Expression condition = ifStatement.getIfCondition().getTree();
			if (!this.isLogLevelGuardCondition(condition)) {
				return false;
			}

			List<Statement> bodyStatements = this.extractStatements(ifStatement.getThenPart());
			return this.allStatementsAreSafeLoggingCalls(bodyStatements);
		}

		private boolean isLogLevelGuardCondition(Expression condition) {
			if (!(condition instanceof J.MethodInvocation method)) {
				return false;
			}

			return (
				IS_TRACE_ENABLED.matches(method)
				|| IS_DEBUG_ENABLED.matches(method)
				|| IS_INFO_ENABLED.matches(method)
				|| IS_WARN_ENABLED.matches(method)
				|| IS_ERROR_ENABLED.matches(method)
				|| IS_TRACE_ENABLED_MARKER.matches(method)
				|| IS_DEBUG_ENABLED_MARKER.matches(method)
				|| IS_INFO_ENABLED_MARKER.matches(method)
				|| IS_WARN_ENABLED_MARKER.matches(method)
				|| IS_ERROR_ENABLED_MARKER.matches(method)
			);
		}

		private List<Statement> extractStatements(Statement thenPart) {
			if (thenPart instanceof J.Block block) {
				return block.getStatements();
			}
			return List.of(thenPart);
		}

		private Space computeIndentationOnly(Space prefix) {
			String whitespace = prefix.getWhitespace();
			int lastNewline = whitespace.lastIndexOf('\n');
			if (lastNewline >= 0) {
				return Space.format("\n" + whitespace.substring(lastNewline + 1));
			}
			return Space.format(whitespace);
		}

		private boolean allStatementsAreSafeLoggingCalls(List<Statement> statements) {
			if (statements.isEmpty()) {
				return false;
			}

			for (Statement stmt : statements) {
				if (!this.isSafeLoggingCall(stmt)) {
					return false;
				}
			}
			return true;
		}

		private boolean isSafeLoggingCall(Statement stmt) {
			if (!(stmt instanceof J.MethodInvocation logCall)) {
				return false;
			}
			return this.isValidLoggingCall(logCall);
		}

		private boolean isValidLoggingCall(J.MethodInvocation logCall) {
			String methodName = logCall.getSimpleName();
			if (!LOGGER_METHODS.contains(methodName)) {
				return false;
			}

			if (!this.isSlf4jLogger(logCall.getSelect())) {
				return false;
			}

			return this.allArgumentsAreSafe(logCall.getArguments());
		}

		private boolean isSlf4jLogger(Expression select) {
			if (select == null) {
				return false;
			}
			JavaType.FullyQualified type = TypeUtils.asFullyQualified(select.getType());
			return type != null && "org.slf4j.Logger".equals(type.getFullyQualifiedName());
		}

		private boolean allArgumentsAreSafe(List<Expression> arguments) {
			for (Expression arg : arguments) {
				if (!this.isArgumentSafe(arg)) {
					return false;
				}
			}
			return true;
		}

		private boolean isArgumentSafe(Expression argument) {
			if (argument instanceof J.Literal) {
				return true;
			}

			if (argument instanceof J.Identifier) {
				return true;
			}

			if (argument instanceof J.FieldAccess) {
				return true;
			}

			if (argument instanceof J.Binary binary) {
				return this.isArgumentSafe(binary.getLeft()) && this.isArgumentSafe(binary.getRight());
			}

			if (argument instanceof J.Parentheses<?> parens) {
				if (parens.getTree() instanceof Expression expr) {
					return this.isArgumentSafe(expr);
				}
				return false;
			}

			if (argument instanceof J.MethodInvocation method) {
				return this.isSafeMethodInvocation(method);
			}

			return false;
		}

		private boolean isSafeMethodInvocation(J.MethodInvocation method) {
			String methodName = method.getSimpleName();

			if ("getMessage".equals(methodName)) {
				Expression select = method.getSelect();
				if (select != null) {
					JavaType type = select.getType();
					return TypeUtils.isAssignableTo("java.lang.Throwable", type);
				}
			}

			return false;
		}
	}
}
