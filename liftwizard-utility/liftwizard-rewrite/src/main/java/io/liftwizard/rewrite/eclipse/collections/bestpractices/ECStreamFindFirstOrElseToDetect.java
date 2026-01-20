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
 * Transforms stream filter+findFirst+orElse(null) operations to Eclipse Collections detect.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().filter(s -> s.isEmpty()).findFirst().orElse(null);
 *
 * // After
 * list.detect(s -> s.isEmpty());
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code detect} method directly on {@code RichIterable}.
 *
 * <p>Note: Only handles the {@code orElse(null)} case. The {@code orElse(defaultValue)} case
 * would require transforming to {@code detectIfNone(pred, () -> defaultValue)}, which is not
 * yet supported due to type resolution limitations with JavaTemplate.
 */
public class ECStreamFindFirstOrElseToDetect extends Recipe {

	private static final MethodMatcher OR_ELSE_MATCHER = new MethodMatcher("java.util.Optional orElse(..)");

	private static final MethodMatcher FIND_FIRST_MATCHER = new MethodMatcher("java.util.stream.Stream findFirst()");

	private static final MethodMatcher FILTER_MATCHER = new MethodMatcher(
		"java.util.stream.Stream filter(java.util.function.Predicate)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().filter(pred).findFirst().orElse(null)` to `detect(pred)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().filter(pred).findFirst().orElse(null)` to `collection.detect(pred)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the detect method directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(OR_ELSE_MATCHER), new StreamFilterFindFirstOrElseToDetectVisitor());
	}

	private static final class StreamFilterFindFirstOrElseToDetectVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!OR_ELSE_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			List<Expression> orElseArguments = methodInvocation.getArguments();
			if (orElseArguments.size() != 1) {
				return methodInvocation;
			}

			Expression defaultValue = orElseArguments.get(0);
			if (!(defaultValue instanceof J.Literal literal) || literal.getValue() != null) {
				return methodInvocation;
			}

			Expression orElseSelect = methodInvocation.getSelect();
			if (!(orElseSelect instanceof J.MethodInvocation findFirstCall)) {
				return methodInvocation;
			}

			if (!FIND_FIRST_MATCHER.matches(findFirstCall)) {
				return methodInvocation;
			}

			Expression findFirstSelect = findFirstCall.getSelect();
			if (!(findFirstSelect instanceof J.MethodInvocation filterCall)) {
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

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName("detect");

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(orElseSelect.getPrefix()))
				.withName(newMethodName)
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
