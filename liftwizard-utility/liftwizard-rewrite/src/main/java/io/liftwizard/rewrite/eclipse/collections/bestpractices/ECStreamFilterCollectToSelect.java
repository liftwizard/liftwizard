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
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms stream filter+collect operations to Eclipse Collections select.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().filter(s -> s.isEmpty()).collect(Collectors.toList());
 *
 * // After
 * list.select(s -> s.isEmpty());
 * }</pre>
 *
 * <p>Also handles toSet():
 * <pre>{@code
 * // Before
 * list.stream().filter(s -> s.isEmpty()).collect(Collectors.toSet());
 *
 * // After
 * list.select(s -> s.isEmpty()).toSet();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code select} method directly on {@code RichIterable}.
 *
 * <p>Note: This recipe does NOT convert toUnmodifiableList() or toUnmodifiableSet() because
 * unmodifiable and immutable are different concepts.
 */
public class ECStreamFilterCollectToSelect extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher FILTER_MATCHER = new MethodMatcher(
		"java.util.stream.Stream filter(java.util.function.Predicate)"
	);

	private static final MethodMatcher TO_LIST_MATCHER = new MethodMatcher("java.util.stream.Collectors toList()");

	private static final MethodMatcher TO_SET_MATCHER = new MethodMatcher("java.util.stream.Collectors toSet()");

	@Override
	public String getDisplayName() {
		return "`stream().filter(pred).collect(Collectors.toList())` to `select(pred)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().filter(pred).collect(Collectors.toList())` to `collection.select(pred)` "
			+ "and `collection.stream().filter(pred).collect(Collectors.toSet())` to `collection.select(pred).toSet()`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the select method directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COLLECT_MATCHER), new StreamFilterCollectToSelectVisitor());
	}

	private static final class StreamFilterCollectToSelectVisitor extends JavaIsoVisitor<ExecutionContext> {

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

			boolean isToList = TO_LIST_MATCHER.matches(collectorCall);
			boolean isToSet = TO_SET_MATCHER.matches(collectorCall);

			if (!isToList && !isToSet) {
				return methodInvocation;
			}

			Expression collectSelect = methodInvocation.getSelect();
			if (!(collectSelect instanceof J.MethodInvocation filterCall)) {
				return methodInvocation;
			}

			if (!FILTER_MATCHER.matches(filterCall)) {
				return methodInvocation;
			}

			Expression filterSelect = filterCall.getSelect();
			if (!(filterSelect instanceof J.MethodInvocation streamCall)) {
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

			List<Expression> filterArguments = filterCall.getArguments();
			if (filterArguments.isEmpty()) {
				return methodInvocation;
			}

			J.Identifier selectMethodName = methodInvocation.getName().withSimpleName("select");

			if (isToSet) {
				J.MethodInvocation selectCall = filterCall
					.withSelect(collectionExpr.withPrefix(Space.EMPTY))
					.withName(filterCall.getName().withSimpleName("select"));

				J.Identifier toSetMethodName = streamCall.getName().withSimpleName("toSet");
				return streamCall
					.withSelect(selectCall.withPrefix(collectSelect.getPrefix()))
					.withName(toSetMethodName)
					.withPrefix(methodInvocation.getPrefix());
			}

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(selectMethodName)
				.withArguments(filterArguments);
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
