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
 * Transforms stream collect toMap with Function.identity() to Eclipse Collections groupByUniqueKey.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.toMap(Donut::code, Function.identity()));
 *
 * // After
 * list.groupByUniqueKey(Donut::code);
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code groupByUniqueKey} method directly on {@code RichIterable}.
 * The method returns a {@code MutableMap<K, V>} directly.
 *
 * <p>This recipe only matches the two-argument form of {@code Collectors.toMap(keyFn, Function.identity())}
 * where the value mapper is {@code Function.identity()}.
 */
public class ECStreamCollectToMapToGroupByUniqueKey extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher TO_MAP_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors toMap(java.util.function.Function, java.util.function.Function)"
	);

	private static final MethodMatcher FUNCTION_IDENTITY_MATCHER = new MethodMatcher(
		"java.util.function.Function identity()"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.toMap(keyFn, Function.identity()))` to `groupByUniqueKey(keyFn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.toMap(keyFn, Function.identity()))` "
			+ "to `collection.groupByUniqueKey(keyFn)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the groupByUniqueKey method directly on RichIterable, returning a MutableMap."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(COLLECT_MATCHER),
			new StreamCollectToMapToGroupByUniqueKeyVisitor()
		);
	}

	private static final class StreamCollectToMapToGroupByUniqueKeyVisitor extends JavaIsoVisitor<ExecutionContext> {

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

			if (!TO_MAP_MATCHER.matches(collectorCall)) {
				return methodInvocation;
			}

			List<Expression> toMapArguments = collectorCall.getArguments();
			if (toMapArguments.size() != 2) {
				return methodInvocation;
			}

			Expression valueFn = toMapArguments.get(1);
			if (!(valueFn instanceof J.MethodInvocation identityCall)) {
				return methodInvocation;
			}

			if (!FUNCTION_IDENTITY_MATCHER.matches(identityCall)) {
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

			Expression keyFn = toMapArguments.get(0);

			J.Identifier groupByUniqueKeyMethodName = streamCall.getName().withSimpleName("groupByUniqueKey");

			return streamCall
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(groupByUniqueKeyMethodName)
				.withArguments(List.of(keyFn))
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
