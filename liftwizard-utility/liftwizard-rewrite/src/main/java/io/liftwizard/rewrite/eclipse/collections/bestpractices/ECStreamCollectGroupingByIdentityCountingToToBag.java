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
 * Transforms stream collect groupingBy with Function.identity() and counting to Eclipse Collections toBag.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
 *
 * // After
 * list.toBag();
 * }</pre>
 *
 * <p>When the classifying function is {@code Function.identity()}, the result of
 * {@code countBy(Function.identity())} is semantically equivalent to {@code toBag()}.
 * The {@code toBag()} method is more direct and idiomatic for this use case.
 *
 * <p>A {@code Bag<T>} provides {@code occurrencesOf()} and {@code topOccurrences()} out of the box,
 * replacing the need for manual Map-based counting patterns.
 */
public class ECStreamCollectGroupingByIdentityCountingToToBag extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher GROUPING_BY_TWO_ARG_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors groupingBy(java.util.function.Function, java.util.stream.Collector)"
	);

	private static final MethodMatcher COUNTING_MATCHER = new MethodMatcher("java.util.stream.Collectors counting()");

	private static final MethodMatcher FUNCTION_IDENTITY_MATCHER = new MethodMatcher(
		"java.util.function.Function identity()"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))` to `toBag()`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))` "
			+ "to `collection.toBag()`. "
			+ "When the classifying function is Function.identity(), countBy is equivalent to toBag(). "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the toBag method directly on RichIterable, returning a Bag."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(COLLECT_MATCHER),
			new StreamCollectGroupingByIdentityCountingToToBagVisitor()
		);
	}

	private static final class StreamCollectGroupingByIdentityCountingToToBagVisitor
		extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!COLLECT_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			List<Expression> collectArguments = methodInvocation.getArguments();
			if (collectArguments.size() != 1) {
				return methodInvocation;
			}

			Expression collectorArg = collectArguments.get(0);
			if (!(collectorArg instanceof J.MethodInvocation collectorCall)) {
				return methodInvocation;
			}

			if (!this.isGroupingByIdentityWithCounting(collectorCall)) {
				return methodInvocation;
			}

			Expression collectSelect = methodInvocation.getSelect();
			if (!(collectSelect instanceof J.MethodInvocation streamCall)) {
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

			J.Identifier toBagMethodName = streamCall.getName().withSimpleName("toBag");

			// streamCall already has empty arguments from stream(), so we just
			// change the name and select, keeping the no-arg arguments list.
			return streamCall
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(toBagMethodName)
				.withPrefix(methodInvocation.getPrefix());
		}

		/**
		 * Checks whether the collector call is {@code Collectors.groupingBy(Function.identity(), Collectors.counting())}.
		 */
		private boolean isGroupingByIdentityWithCounting(J.MethodInvocation collectorCall) {
			if (!GROUPING_BY_TWO_ARG_MATCHER.matches(collectorCall)) {
				return false;
			}

			List<Expression> args = collectorCall.getArguments();
			if (args.size() != 2) {
				return false;
			}

			Expression classifyingFunction = args.get(0);
			if (!(classifyingFunction instanceof J.MethodInvocation identityCall)) {
				return false;
			}

			if (!FUNCTION_IDENTITY_MATCHER.matches(identityCall)) {
				return false;
			}

			Expression downstream = args.get(1);
			if (!(downstream instanceof J.MethodInvocation downstreamCall)) {
				return false;
			}

			return COUNTING_MATCHER.matches(downstreamCall);
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
