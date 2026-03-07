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
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

/**
 * Transforms stream collect+Collectors.summarizing operations to Eclipse Collections
 * collectPrimitive+summaryStatistics.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.summarizingDouble(String::length));
 * list.stream().collect(Collectors.summarizingInt(String::length));
 * list.stream().collect(Collectors.summarizingLong(String::length));
 *
 * // After
 * list.collectDouble(String::length).summaryStatistics();
 * list.collectInt(String::length).summaryStatistics();
 * list.collectLong(String::length).summaryStatistics();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code collectDouble/collectInt/collectLong} methods directly
 * on {@code RichIterable}, and the resulting primitive collections have {@code summaryStatistics()}.
 */
public class ECStreamCollectSummarizingToCollectPrimitive extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher SUMMARIZING_DOUBLE_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors summarizingDouble(java.util.function.ToDoubleFunction)"
	);

	private static final MethodMatcher SUMMARIZING_INT_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors summarizingInt(java.util.function.ToIntFunction)"
	);

	private static final MethodMatcher SUMMARIZING_LONG_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors summarizingLong(java.util.function.ToLongFunction)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.summarizing*(fn))` to `collect*(fn).summaryStatistics()`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.summarizingDouble(fn))` "
			+ "to `collection.collectDouble(fn).summaryStatistics()`. "
			+ "Also handles summarizingInt and summarizingLong variants. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the collectDouble/collectInt/collectLong methods directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(COLLECT_MATCHER),
			new StreamCollectSummarizingToCollectPrimitiveVisitor()
		);
	}

	private static final class StreamCollectSummarizingToCollectPrimitiveVisitor
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
			if (!(collectorArg instanceof J.MethodInvocation summarizingCall)) {
				return methodInvocation;
			}

			String collectMethodName = this.getCollectMethodName(summarizingCall);
			if (collectMethodName == null) {
				return methodInvocation;
			}

			List<Expression> summarizingArgs = summarizingCall.getArguments();
			if (summarizingArgs.size() != 1) {
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

			// Build: collection.collectDouble(fn).summaryStatistics()

			// Reuse the collect() method type (1 arg) for the collectDouble(fn) call
			JavaType.Method collectMethodType = methodInvocation.getMethodType();
			JavaType.Method collectPrimitiveMethodType = collectMethodType != null
				? collectMethodType.withName(collectMethodName)
				: null;

			// Reuse the stream() method type (0 args) for the summaryStatistics() call
			JavaType.Method streamMethodType = streamCall.getMethodType();
			JavaType.Method summaryStatsMethodType = streamMethodType != null
				? streamMethodType.withName("summaryStatistics")
				: null;

			Expression mapperFunction = summarizingArgs.get(0);

			// Inner call: collection.collectDouble(fn)
			J.Identifier collectPrimitiveMethodName = streamCall
				.getName()
				.withSimpleName(collectMethodName)
				.withType(collectPrimitiveMethodType);

			J.MethodInvocation collectPrimitiveCall = streamCall
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(collectPrimitiveMethodName)
				.withArguments(List.of(mapperFunction))
				.withMethodType(collectPrimitiveMethodType);

			// Outer call: .summaryStatistics()
			J.Identifier summaryStatsMethodName = methodInvocation
				.getName()
				.withSimpleName("summaryStatistics")
				.withType(summaryStatsMethodType);

			return methodInvocation
				.withSelect(collectPrimitiveCall)
				.withName(summaryStatsMethodName)
				.withArguments(List.of(new J.Empty(java.util.UUID.randomUUID(), Space.EMPTY, Markers.EMPTY)))
				.withMethodType(summaryStatsMethodType);
		}

		/**
		 * Returns the Eclipse Collections collect method name for the matching summarizing call,
		 * or null if the call is not a supported form.
		 */
		private String getCollectMethodName(J.MethodInvocation summarizingCall) {
			if (SUMMARIZING_DOUBLE_MATCHER.matches(summarizingCall)) {
				return "collectDouble";
			}
			if (SUMMARIZING_INT_MATCHER.matches(summarizingCall)) {
				return "collectInt";
			}
			if (SUMMARIZING_LONG_MATCHER.matches(summarizingCall)) {
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
