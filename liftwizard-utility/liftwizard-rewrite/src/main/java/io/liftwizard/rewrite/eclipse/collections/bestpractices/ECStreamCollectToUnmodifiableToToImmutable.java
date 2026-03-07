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
 * Transforms stream collect to unmodifiable list/set operations to Eclipse Collections toImmutableList/toImmutableSet.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.toUnmodifiableList());
 * list.stream().collect(Collectors.toUnmodifiableSet());
 *
 * // After
 * list.toImmutableList();
 * list.toImmutableSet();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code toImmutableList} and {@code toImmutableSet} methods
 * directly on {@code RichIterable}.
 *
 * <p>Eclipse Collections immutable types provide a richer API than JDK unmodifiable wrappers,
 * so this is a safe and beneficial transformation.
 */
public class ECStreamCollectToUnmodifiableToToImmutable extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher TO_UNMODIFIABLE_LIST_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors toUnmodifiableList()"
	);

	private static final MethodMatcher TO_UNMODIFIABLE_SET_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors toUnmodifiableSet()"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.toUnmodifiableList/Set())` to `toImmutableList/Set()`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.toUnmodifiableList())` to `collection.toImmutableList()` "
			+ "and `collection.stream().collect(Collectors.toUnmodifiableSet())` to `collection.toImmutableSet()`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the toImmutableList and toImmutableSet methods directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COLLECT_MATCHER), new StreamCollectToUnmodifiableVisitor());
	}

	private static final class StreamCollectToUnmodifiableVisitor extends JavaIsoVisitor<ExecutionContext> {

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

			String targetMethodName;
			if (TO_UNMODIFIABLE_LIST_MATCHER.matches(collectorCall)) {
				targetMethodName = "toImmutableList";
			} else if (TO_UNMODIFIABLE_SET_MATCHER.matches(collectorCall)) {
				targetMethodName = "toImmutableSet";
			} else {
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

			J.Identifier toImmutableMethodName = streamCall.getName().withSimpleName(targetMethodName);

			return streamCall
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(toImmutableMethodName)
				.withPrefix(methodInvocation.getPrefix());
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
