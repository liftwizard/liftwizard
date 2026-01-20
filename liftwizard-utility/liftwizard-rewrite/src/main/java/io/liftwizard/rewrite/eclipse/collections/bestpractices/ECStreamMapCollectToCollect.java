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
 * Transforms stream map+collect operations to Eclipse Collections collect.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().map(s -> s.length()).collect(Collectors.toList());
 *
 * // After
 * list.collect(s -> s.length());
 * }</pre>
 *
 * <p>Also handles toSet():
 * <pre>{@code
 * // Before
 * list.stream().map(s -> s.length()).collect(Collectors.toSet());
 *
 * // After
 * list.collect(s -> s.length()).toSet();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code collect} method directly on {@code RichIterable}.
 *
 * <p>Note: This recipe does NOT convert toUnmodifiableList() or toUnmodifiableSet() because
 * unmodifiable and immutable are different concepts.
 */
public class ECStreamMapCollectToCollect extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher MAP_MATCHER = new MethodMatcher(
		"java.util.stream.Stream map(java.util.function.Function)"
	);

	private static final MethodMatcher TO_LIST_MATCHER = new MethodMatcher("java.util.stream.Collectors toList()");

	private static final MethodMatcher TO_SET_MATCHER = new MethodMatcher("java.util.stream.Collectors toSet()");

	@Override
	public String getDisplayName() {
		return "`stream().map(fn).collect(Collectors.toList())` to `collect(fn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().map(fn).collect(Collectors.toList())` to `collection.collect(fn)` "
			+ "and `collection.stream().map(fn).collect(Collectors.toSet())` to `collection.collect(fn).toSet()`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the collect method directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COLLECT_MATCHER), new StreamMapCollectToCollectVisitor());
	}

	private static final class StreamMapCollectToCollectVisitor extends JavaIsoVisitor<ExecutionContext> {

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
			if (!(collectSelect instanceof J.MethodInvocation mapCall)) {
				return methodInvocation;
			}

			if (!MAP_MATCHER.matches(mapCall)) {
				return methodInvocation;
			}

			Expression mapSelect = mapCall.getSelect();
			if (!(mapSelect instanceof J.MethodInvocation streamCall)) {
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

			List<Expression> mapArguments = mapCall.getArguments();
			if (mapArguments.isEmpty()) {
				return methodInvocation;
			}

			J.Identifier collectMethodName = methodInvocation.getName().withSimpleName("collect");

			if (isToSet) {
				J.MethodInvocation collectCall = mapCall
					.withSelect(collectionExpr.withPrefix(Space.EMPTY))
					.withName(mapCall.getName().withSimpleName("collect"));

				J.Identifier toSetMethodName = streamCall.getName().withSimpleName("toSet");
				return streamCall
					.withSelect(collectCall.withPrefix(collectSelect.getPrefix()))
					.withName(toSetMethodName)
					.withPrefix(methodInvocation.getPrefix());
			}

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(collectMethodName)
				.withArguments(mapArguments);
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
