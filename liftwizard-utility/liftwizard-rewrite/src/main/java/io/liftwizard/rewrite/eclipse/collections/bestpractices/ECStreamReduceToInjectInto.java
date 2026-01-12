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

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms stream reduce operations to Eclipse Collections injectInto.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().reduce(0, Integer::sum);
 *
 * // After
 * list.injectInto(0, Integer::sum);
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code injectInto} method directly on {@code RichIterable}.
 * The injectInto method is Eclipse Collections' native fold operation.
 */
public class ECStreamReduceToInjectInto extends Recipe {

	private static final MethodMatcher REDUCE_WITH_IDENTITY_MATCHER = new MethodMatcher(
		"java.util.stream.Stream reduce(*, java.util.function.BinaryOperator)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().reduce(identity, accumulator)` to `injectInto(identity, function)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().reduce(identity, accumulator)` to `collection.injectInto(identity, function)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the injectInto method directly on RichIterable. The injectInto method is Eclipse Collections' native fold operation."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(REDUCE_WITH_IDENTITY_MATCHER),
			new StreamReduceToInjectIntoVisitor()
		);
	}

	private static final class StreamReduceToInjectIntoVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!REDUCE_WITH_IDENTITY_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			Expression select = methodInvocation.getSelect();
			if (!(select instanceof J.MethodInvocation streamCall)) {
				return methodInvocation;
			}

			if (!this.isStreamMethod(streamCall)) {
				return methodInvocation;
			}

			Expression collectionExpr = streamCall.getSelect();
			if (collectionExpr == null) {
				return methodInvocation;
			}

			if (!this.isEclipseCollectionsType(collectionExpr)) {
				return methodInvocation;
			}

			List<Expression> arguments = methodInvocation.getArguments();
			if (arguments.size() != 2) {
				return methodInvocation;
			}

			Expression identity = arguments.get(0);
			Expression accumulator = arguments.get(1);

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName("injectInto");

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(select.getPrefix()))
				.withName(newMethodName)
				.withArguments(List.of(identity, accumulator));
		}

		private boolean isStreamMethod(J.MethodInvocation method) {
			if (!"stream".equals(method.getSimpleName())) {
				return false;
			}
			if (!method.getArguments().isEmpty()) {
				if (method.getArguments().size() != 1) {
					return false;
				}
				if (!(method.getArguments().get(0) instanceof J.Empty)) {
					return false;
				}
			}
			return true;
		}

		private boolean isEclipseCollectionsType(Expression expression) {
			return TypeUtils.isAssignableTo("org.eclipse.collections.api.RichIterable", expression.getType());
		}
	}
}
