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
 * Transforms stream min/max operations to Eclipse Collections minOptional/maxOptional.
 *
 * <ul>
 *   <li>{@code collection.stream().min(comparator)} to {@code collection.minOptional(comparator)}
 *   <li>{@code collection.stream().max(comparator)} to {@code collection.maxOptional(comparator)}
 * </ul>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code minOptional} and {@code maxOptional} methods directly
 * on {@code RichIterable}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().min(Comparator.naturalOrder());
 *
 * // After
 * list.minOptional(Comparator.naturalOrder());
 * }</pre>
 */
public class ECStreamMinMaxToMinMax extends Recipe {

	private static final MethodMatcher MIN_MATCHER = new MethodMatcher(
		"java.util.stream.Stream min(java.util.Comparator)"
	);

	private static final MethodMatcher MAX_MATCHER = new MethodMatcher(
		"java.util.stream.Stream max(java.util.Comparator)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().min/max(comparator)` to `minOptional/maxOptional(comparator)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().min(comparator)` to `collection.minOptional(comparator)` and "
			+ "`collection.stream().max(comparator)` to `collection.maxOptional(comparator)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "minOptional/maxOptional methods directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(new UsesMethod<>(MIN_MATCHER), new UsesMethod<>(MAX_MATCHER)),
			new StreamMinMaxToMinMaxOptionalVisitor()
		);
	}

	private static final class StreamMinMaxToMinMaxOptionalVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			String optionalMethod = this.getOptionalMethod(methodInvocation);
			if (optionalMethod == null) {
				return methodInvocation;
			}

			Expression select = methodInvocation.getSelect();
			if (!(select instanceof J.MethodInvocation streamCall)) {
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

			List<Expression> arguments = methodInvocation.getArguments();
			if (arguments.isEmpty()) {
				return methodInvocation;
			}

			Expression comparator = arguments.get(0);

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName(optionalMethod);

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(select.getPrefix()))
				.withName(newMethodName)
				.withArguments(List.of(comparator));
		}

		private String getOptionalMethod(J.MethodInvocation method) {
			if (MIN_MATCHER.matches(method)) {
				return "minOptional";
			}
			if (MAX_MATCHER.matches(method)) {
				return "maxOptional";
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
