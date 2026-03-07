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
 * Transforms stream collect groupingBy with counting to Eclipse Collections countBy.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
 *
 * // After
 * list.countBy(String::length);
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code countBy} method directly on {@code RichIterable}.
 * The method returns a {@code Bag<K>} which provides {@code occurrencesOf()} and {@code topOccurrences()}
 * instead of {@code Map<K, Long>}.
 */
public class ECStreamCollectGroupingByCountingToCountBy extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher GROUPING_BY_TWO_ARG_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors groupingBy(java.util.function.Function, java.util.stream.Collector)"
	);

	private static final MethodMatcher COUNTING_MATCHER = new MethodMatcher("java.util.stream.Collectors counting()");

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.groupingBy(fn, Collectors.counting()))` to `countBy(fn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.groupingBy(fn, Collectors.counting()))` "
			+ "to `collection.countBy(fn)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the countBy method directly on RichIterable, returning a Bag."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(COLLECT_MATCHER),
			new StreamCollectGroupingByCountingToCountByVisitor()
		);
	}

	private static final class StreamCollectGroupingByCountingToCountByVisitor
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

			Expression classifyingFunction = this.extractClassifyingFunction(collectorCall);
			if (classifyingFunction == null) {
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

			J.Identifier countByMethodName = streamCall.getName().withSimpleName("countBy");

			return streamCall
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(countByMethodName)
				.withArguments(List.of(classifyingFunction))
				.withPrefix(methodInvocation.getPrefix());
		}

		/**
		 * Extracts the classifying function from a groupingBy(fn, Collectors.counting()) call,
		 * returning null if the call is not a supported form.
		 */
		private Expression extractClassifyingFunction(J.MethodInvocation collectorCall) {
			if (!GROUPING_BY_TWO_ARG_MATCHER.matches(collectorCall)) {
				return null;
			}

			List<Expression> args = collectorCall.getArguments();
			if (args.size() != 2) {
				return null;
			}

			Expression downstream = args.get(1);
			if (!(downstream instanceof J.MethodInvocation downstreamCall)) {
				return null;
			}

			if (!COUNTING_MATCHER.matches(downstreamCall)) {
				return null;
			}

			return args.get(0);
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
