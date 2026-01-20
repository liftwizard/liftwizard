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
 * Transforms stream collect with Collectors.minBy/maxBy to Eclipse Collections minBy/maxBy.
 *
 * <ul>
 *   <li>{@code collection.stream().collect(Collectors.minBy(Comparator.comparing(fn))).orElse(null)} to {@code collection.minBy(fn)}
 *   <li>{@code collection.stream().collect(Collectors.maxBy(Comparator.comparing(fn))).orElse(null)} to {@code collection.maxBy(fn)}
 * </ul>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code minBy} and {@code maxBy} methods directly on {@code RichIterable}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.minBy(Comparator.comparing(Person::getAge))).orElse(null);
 *
 * // After
 * list.minBy(Person::getAge);
 * }</pre>
 */
public class ECStreamCollectorsMinMaxByToMinMaxBy extends Recipe {

	private static final MethodMatcher OR_ELSE_MATCHER = new MethodMatcher("java.util.Optional orElse(..)");

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher COMPARING_MATCHER = new MethodMatcher(
		"java.util.Comparator comparing(java.util.function.Function)"
	);

	private static final MethodMatcher MIN_BY_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors minBy(java.util.Comparator)"
	);

	private static final MethodMatcher MAX_BY_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors maxBy(java.util.Comparator)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.minBy/maxBy(Comparator.comparing(fn))).orElse(null)` to `minBy/maxBy(fn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.minBy(Comparator.comparing(fn))).orElse(null)` "
			+ "to `collection.minBy(fn)` and similarly for maxBy. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "minBy/maxBy methods directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(OR_ELSE_MATCHER), new StreamCollectorsMinMaxByToMinMaxByVisitor());
	}

	private static final class StreamCollectorsMinMaxByToMinMaxByVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!OR_ELSE_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			List<Expression> orElseArgs = methodInvocation.getArguments();
			if (orElseArgs.size() != 1 || !isNullLiteral(orElseArgs.getFirst())) {
				return methodInvocation;
			}

			Expression orElseSelect = methodInvocation.getSelect();
			if (!(orElseSelect instanceof J.MethodInvocation collectCall)) {
				return methodInvocation;
			}

			if (!COLLECT_MATCHER.matches(collectCall)) {
				return methodInvocation;
			}

			List<Expression> collectArgs = collectCall.getArguments();
			if (collectArgs.size() != 1) {
				return methodInvocation;
			}

			if (!(collectArgs.getFirst() instanceof J.MethodInvocation minMaxByCall)) {
				return methodInvocation;
			}

			String targetMethodName = this.getTargetMethodName(minMaxByCall);
			if (targetMethodName == null) {
				return methodInvocation;
			}

			List<Expression> minMaxByArgs = minMaxByCall.getArguments();
			if (minMaxByArgs.size() != 1) {
				return methodInvocation;
			}

			if (!(minMaxByArgs.getFirst() instanceof J.MethodInvocation comparingCall)) {
				return methodInvocation;
			}

			if (!COMPARING_MATCHER.matches(comparingCall)) {
				return methodInvocation;
			}

			List<Expression> comparingArgs = comparingCall.getArguments();
			if (comparingArgs.size() != 1) {
				return methodInvocation;
			}

			Expression collectSelect = collectCall.getSelect();
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

			Expression function = comparingArgs.getFirst();
			J.Identifier newMethodName = methodInvocation.getName().withSimpleName(targetMethodName);

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(orElseSelect.getPrefix()))
				.withName(newMethodName)
				.withArguments(List.of(function));
		}

		private String getTargetMethodName(J.MethodInvocation method) {
			if (MIN_BY_MATCHER.matches(method)) {
				return "minBy";
			}
			if (MAX_BY_MATCHER.matches(method)) {
				return "maxBy";
			}
			return null;
		}

		private static boolean isNullLiteral(Expression expression) {
			return expression instanceof J.Literal literal && literal.getValue() == null;
		}

		private boolean isStreamMethod(J.MethodInvocation method) {
			if (!"stream".equals(method.getSimpleName())) {
				return false;
			}
			if (method.getArguments().isEmpty()) {
				return true;
			}
			if (method.getArguments().size() != 1) {
				return false;
			}
			return method.getArguments().getFirst() instanceof J.Empty;
		}

		private boolean isEclipseCollectionsType(Expression expression) {
			return TypeUtils.isAssignableTo("org.eclipse.collections.api.RichIterable", expression.getType());
		}
	}
}
