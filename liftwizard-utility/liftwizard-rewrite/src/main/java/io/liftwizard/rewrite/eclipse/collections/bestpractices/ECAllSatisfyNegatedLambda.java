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
 * Transforms {@code iterable.allSatisfy(x -> !pred(x))} to {@code iterable.noneSatisfy(x -> pred(x))}.
 *
 * <p>This recipe detects lambda expressions passed to {@code allSatisfy} where the lambda body
 * is a negated expression (using the {@code !} operator), and transforms them to use
 * {@code noneSatisfy} with the non-negated predicate instead.
 *
 * <p>This improves code readability by eliminating double negation patterns.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.allSatisfy(s -> !s.isEmpty())
 *
 * // After
 * list.noneSatisfy(s -> s.isEmpty())
 * }</pre>
 */
public class ECAllSatisfyNegatedLambda extends Recipe {

	private static final MethodMatcher ALL_SATISFY_MATCHER = new MethodMatcher(
		"org.eclipse.collections.api.RichIterable allSatisfy(org.eclipse.collections.api.block.predicate.Predicate)"
	);

	@Override
	public String getDisplayName() {
		return "`allSatisfy(x -> !pred(x))` to `noneSatisfy(x -> pred(x))`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `iterable.allSatisfy(x -> !pred(x))` to `iterable.noneSatisfy(x -> pred(x))` "
			+ "for Eclipse Collections types, eliminating the double negation pattern."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(ALL_SATISFY_MATCHER), new AllSatisfyNegatedLambdaVisitor());
	}

	private static final class AllSatisfyNegatedLambdaVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!ALL_SATISFY_MATCHER.matches(methodInvocation)) {
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
			if (!(lambdaBody instanceof J.Unary unary)) {
				return methodInvocation;
			}

			if (unary.getOperator() != J.Unary.Type.Not) {
				return methodInvocation;
			}

			Expression negatedExpression = unary.getExpression();

			Space unaryPrefix = unary.getPrefix();
			Expression newBody = this.unwrapParentheses(negatedExpression).withPrefix(unaryPrefix);

			J.Lambda newLambda = lambda.withBody(newBody);

			J.Identifier methodName = methodInvocation.getName();
			J.Identifier newMethodName = methodName.withSimpleName("noneSatisfy");

			return methodInvocation
				.withName(newMethodName)
				.withArguments(Lists.fixedSize.with(newLambda));
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
	}
}
