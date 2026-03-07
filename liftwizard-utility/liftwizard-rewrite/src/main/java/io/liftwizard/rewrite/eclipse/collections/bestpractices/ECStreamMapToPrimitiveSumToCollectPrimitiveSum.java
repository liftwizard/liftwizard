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
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms stream mapToDouble/mapToInt/mapToLong + sum operations to Eclipse Collections
 * collectDouble/collectInt/collectLong + sum.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().mapToDouble(String::length).sum();
 * list.stream().mapToInt(String::length).sum();
 * list.stream().mapToLong(String::length).sum();
 *
 * // After
 * list.collectDouble(String::length).sum();
 * list.collectInt(String::length).sum();
 * list.collectLong(String::length).sum();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code collectDouble/collectInt/collectLong} methods directly
 * on {@code RichIterable}, and the resulting primitive collections have {@code sum()}.
 */
public class ECStreamMapToPrimitiveSumToCollectPrimitiveSum extends Recipe {

	private static final MethodMatcher DOUBLE_STREAM_SUM_MATCHER = new MethodMatcher(
		"java.util.stream.DoubleStream sum()"
	);

	private static final MethodMatcher INT_STREAM_SUM_MATCHER = new MethodMatcher("java.util.stream.IntStream sum()");

	private static final MethodMatcher LONG_STREAM_SUM_MATCHER = new MethodMatcher("java.util.stream.LongStream sum()");

	private static final MethodMatcher MAP_TO_DOUBLE_MATCHER = new MethodMatcher(
		"java.util.stream.Stream mapToDouble(java.util.function.ToDoubleFunction)"
	);

	private static final MethodMatcher MAP_TO_INT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream mapToInt(java.util.function.ToIntFunction)"
	);

	private static final MethodMatcher MAP_TO_LONG_MATCHER = new MethodMatcher(
		"java.util.stream.Stream mapToLong(java.util.function.ToLongFunction)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().mapToDouble(fn).sum()` to `collectDouble(fn).sum()`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().mapToDouble(fn).sum()` "
			+ "to `collection.collectDouble(fn).sum()`. "
			+ "Also handles mapToInt and mapToLong variants. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the collectDouble/collectInt/collectLong methods directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(
				new UsesMethod<>(DOUBLE_STREAM_SUM_MATCHER),
				new UsesMethod<>(INT_STREAM_SUM_MATCHER),
				new UsesMethod<>(LONG_STREAM_SUM_MATCHER)
			),
			new StreamMapToPrimitiveSumVisitor()
		);
	}

	private static final class StreamMapToPrimitiveSumVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!this.isPrimitiveSumMethod(methodInvocation)) {
				return methodInvocation;
			}

			Expression sumSelect = methodInvocation.getSelect();
			if (!(sumSelect instanceof J.MethodInvocation mapToPrimitiveCall)) {
				return methodInvocation;
			}

			String collectMethodName = this.getCollectMethodName(mapToPrimitiveCall);
			if (collectMethodName == null) {
				return methodInvocation;
			}

			List<Expression> mapToPrimitiveArgs = mapToPrimitiveCall.getArguments();
			if (mapToPrimitiveArgs.size() != 1) {
				return methodInvocation;
			}

			Expression mapToPrimitiveSelect = mapToPrimitiveCall.getSelect();
			if (!(mapToPrimitiveSelect instanceof J.MethodInvocation streamCall)) {
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

			// Build: collection.collectDouble(fn).sum()

			Expression mapperFunction = mapToPrimitiveArgs.get(0);

			// Reuse the mapToDouble() method type (1 arg) for the collectDouble(fn) call
			JavaType.Method mapToPrimitiveMethodType = mapToPrimitiveCall.getMethodType();
			JavaType.Method collectPrimitiveMethodType = mapToPrimitiveMethodType != null
				? mapToPrimitiveMethodType.withName(collectMethodName)
				: null;

			// Inner call: collection.collectDouble(fn)
			J.Identifier collectPrimitiveMethodName = mapToPrimitiveCall
				.getName()
				.withSimpleName(collectMethodName)
				.withType(collectPrimitiveMethodType);

			J.MethodInvocation collectPrimitiveCall = mapToPrimitiveCall
				.withSelect(collectionExpr.withPrefix(sumSelect.getPrefix()))
				.withName(collectPrimitiveMethodName)
				.withArguments(List.of(mapperFunction))
				.withMethodType(collectPrimitiveMethodType);

			// Outer call: .sum() - reuse existing sum method invocation structure
			return methodInvocation.withSelect(collectPrimitiveCall);
		}

		private boolean isPrimitiveSumMethod(J.MethodInvocation method) {
			return (
				DOUBLE_STREAM_SUM_MATCHER.matches(method)
				|| INT_STREAM_SUM_MATCHER.matches(method)
				|| LONG_STREAM_SUM_MATCHER.matches(method)
			);
		}

		/**
		 * Returns the Eclipse Collections collect method name for the matching mapToPrimitive call,
		 * or null if the call is not a supported form.
		 */
		private String getCollectMethodName(J.MethodInvocation mapToPrimitiveCall) {
			if (MAP_TO_DOUBLE_MATCHER.matches(mapToPrimitiveCall)) {
				return "collectDouble";
			}
			if (MAP_TO_INT_MATCHER.matches(mapToPrimitiveCall)) {
				return "collectInt";
			}
			if (MAP_TO_LONG_MATCHER.matches(mapToPrimitiveCall)) {
				return "collectLong";
			}
			return null;
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
