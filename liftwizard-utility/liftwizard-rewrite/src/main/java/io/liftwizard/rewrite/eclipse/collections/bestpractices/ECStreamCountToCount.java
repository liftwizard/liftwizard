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
 * Transforms stream filter count operations to Eclipse Collections count.
 *
 * <p>This recipe transforms:
 * <pre>{@code
 * collection.stream().filter(pred).count()
 * }</pre>
 *
 * <p>to:
 * <pre>{@code
 * collection.count(pred)
 * }</pre>
 *
 * <p>This eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the count method directly on {@code RichIterable}.
 *
 * <p><strong>Note:</strong> Eclipse Collections {@code count()} returns {@code int}, while
 * {@code stream().filter().count()} returns {@code long}. This transformation is safe when
 * the count value fits in an int (up to 2^31-1 elements).
 */
public class ECStreamCountToCount extends Recipe {

	private static final MethodMatcher COUNT_MATCHER = new MethodMatcher("java.util.stream.Stream count()");

	private static final MethodMatcher FILTER_MATCHER = new MethodMatcher(
		"java.util.stream.Stream filter(java.util.function.Predicate)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().filter(pred).count()` -> `count(pred)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().filter(pred).count()` to `collection.count(pred)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has the count method directly."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COUNT_MATCHER), new StreamFilterCountVisitor());
	}

	private static final class StreamFilterCountVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!COUNT_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			Expression countSelect = methodInvocation.getSelect();
			if (!(countSelect instanceof J.MethodInvocation filterCall)) {
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

			Expression predicate = filterArguments.get(0);

			J.Identifier countMethodName = methodInvocation.getName().withSimpleName("count");

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(countSelect.getPrefix()))
				.withName(countMethodName)
				.withArguments(List.of(predicate));
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
