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

import org.eclipse.collections.api.factory.Lists;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

/**
 * Transforms {@code iterable.select(x -> !pred(x))} to {@code iterable.reject(x -> pred(x))}
 * and {@code iterable.reject(x -> !pred(x))} to {@code iterable.select(x -> pred(x))}.
 *
 * <p>This recipe detects lambda expressions passed to {@code select} or {@code reject} where
 * the lambda body contains a negation pattern:
 * <ul>
 *   <li>Negated expression using the {@code !} operator</li>
 *   <li>Not-equal comparison using {@code !=}</li>
 * </ul>
 *
 * <p>The recipe flips {@code select} to {@code reject} (and vice versa) while removing the
 * negation from the lambda body, resulting in cleaner and more readable code.
 *
 * <p>Examples:
 * <pre>{@code
 * // Before
 * list.select(attr -> !attr.isProcessingDate())
 * list.select(attr -> attr.getSomething() != 0)
 * list.reject(s -> !s.isEmpty())
 *
 * // After
 * list.reject(attr -> attr.isProcessingDate())
 * list.reject(attr -> attr.getSomething() == 0)
 * list.select(s -> s.isEmpty())
 * }</pre>
 */
public class ECSimplifyNegatedSelectReject extends Recipe {

	private static final MethodMatcher SELECT_MATCHER = new MethodMatcher(
		"org.eclipse.collections.api.RichIterable select(org.eclipse.collections.api.block.predicate.Predicate)",
		true
	);

	private static final MethodMatcher REJECT_MATCHER = new MethodMatcher(
		"org.eclipse.collections.api.RichIterable reject(org.eclipse.collections.api.block.predicate.Predicate)",
		true
	);

	@Override
	public String getDisplayName() {
		return "`select(x -> !pred(x))` to `reject(x -> pred(x))`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `iterable.select(x -> !pred(x))` to `iterable.reject(x -> pred(x))` "
			+ "and `iterable.reject(x -> !pred(x))` to `iterable.select(x -> pred(x))` "
			+ "for Eclipse Collections types. Also handles `!=` comparisons by flipping to `==`."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(new UsesMethod<>(SELECT_MATCHER), new UsesMethod<>(REJECT_MATCHER)),
			new SelectRejectNegatedLambdaVisitor()
		);
	}

	private static final class SelectRejectNegatedLambdaVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			boolean isSelect = SELECT_MATCHER.matches(methodInvocation);
			boolean isReject = REJECT_MATCHER.matches(methodInvocation);

			if (!isSelect && !isReject) {
				return methodInvocation;
			}

			if (methodInvocation.getArguments().isEmpty()) {
				return methodInvocation;
			}

			Expression argument = methodInvocation.getArguments().get(0);
			if (!(argument instanceof J.Lambda lambda)) {
				return methodInvocation;
			}

			J lambdaBody = lambda.getBody();

			NegationResult negationResult = this.extractNegatedExpression(lambdaBody);
			if (negationResult == null) {
				return methodInvocation;
			}

			Expression newBody = negationResult.nonNegatedExpression();

			J.Lambda newLambda = lambda.withBody(newBody);

			String newMethodName = isSelect ? "reject" : "select";
			J.Identifier methodName = methodInvocation.getName();
			J.Identifier newMethodIdentifier = methodName.withSimpleName(newMethodName);

			return methodInvocation.withName(newMethodIdentifier).withArguments(Lists.fixedSize.with(newLambda));
		}

		/**
		 * Extracts the non-negated expression from a negation pattern.
		 *
		 * @param expression the expression to analyze
		 * @return a NegationResult containing the non-negated expression, or null if not a negation pattern
		 */
		private NegationResult extractNegatedExpression(J expression) {
			// Pattern 1: !expr
			if (expression instanceof J.Unary unary && unary.getOperator() == J.Unary.Type.Not) {
				Expression negatedExpression = unary.getExpression();
				Space unaryPrefix = unary.getPrefix();
				Expression newBody = this.unwrapParentheses(negatedExpression).withPrefix(unaryPrefix);
				return new NegationResult(newBody);
			}

			// Pattern 2: expr != value
			if (expression instanceof J.Binary binary && binary.getOperator() == J.Binary.Type.NotEqual) {
				J.Binary flippedBinary = binary.withOperator(J.Binary.Type.Equal);
				return new NegationResult(flippedBinary);
			}

			return null;
		}

		private Expression unwrapParentheses(Expression expression) {
			if (expression instanceof J.Parentheses<?> parens) {
				J inner = parens.getTree();
				if (inner instanceof Expression innerExpr) {
					return innerExpr;
				}
			}
			return expression;
		}

		private record NegationResult(Expression nonNegatedExpression) {}
	}
}
