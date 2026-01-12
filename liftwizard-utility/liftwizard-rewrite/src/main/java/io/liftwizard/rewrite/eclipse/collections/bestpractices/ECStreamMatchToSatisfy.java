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
 * Transforms stream match operations to Eclipse Collections satisfy operations.
 *
 * <ul>
 *   <li>{@code collection.stream().anyMatch(pred)} to {@code collection.anySatisfy(pred)}
 *   <li>{@code collection.stream().allMatch(pred)} to {@code collection.allSatisfy(pred)}
 *   <li>{@code collection.stream().noneMatch(pred)} to {@code collection.noneSatisfy(pred)}
 * </ul>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has these methods directly on {@code RichIterable}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().anyMatch(s -> s.isEmpty());
 *
 * // After
 * list.anySatisfy(s -> s.isEmpty());
 * }</pre>
 */
public class ECStreamMatchToSatisfy extends Recipe {

	private static final MethodMatcher ANY_MATCH_MATCHER = new MethodMatcher(
		"java.util.stream.Stream anyMatch(java.util.function.Predicate)"
	);

	private static final MethodMatcher ALL_MATCH_MATCHER = new MethodMatcher(
		"java.util.stream.Stream allMatch(java.util.function.Predicate)"
	);

	private static final MethodMatcher NONE_MATCH_MATCHER = new MethodMatcher(
		"java.util.stream.Stream noneMatch(java.util.function.Predicate)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().anyMatch/allMatch/noneMatch(pred)` to `anySatisfy/allSatisfy/noneSatisfy(pred)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().anyMatch(pred)` to `collection.anySatisfy(pred)`, "
			+ "`stream().allMatch(pred)` to `allSatisfy(pred)`, and `stream().noneMatch(pred)` to `noneSatisfy(pred)`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has these methods directly."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(
				new UsesMethod<>(ANY_MATCH_MATCHER),
				new UsesMethod<>(ALL_MATCH_MATCHER),
				new UsesMethod<>(NONE_MATCH_MATCHER)
			),
			new StreamMatchToSatisfyVisitor()
		);
	}

	private static final class StreamMatchToSatisfyVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			String satisfyMethod = this.getSatisfyMethod(methodInvocation);
			if (satisfyMethod == null) {
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

			Expression predicate = arguments.get(0);

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName(satisfyMethod);

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(select.getPrefix()))
				.withName(newMethodName)
				.withArguments(List.of(predicate));
		}

		private String getSatisfyMethod(J.MethodInvocation method) {
			if (ANY_MATCH_MATCHER.matches(method)) {
				return "anySatisfy";
			}
			if (ALL_MATCH_MATCHER.matches(method)) {
				return "allSatisfy";
			}
			if (NONE_MATCH_MATCHER.matches(method)) {
				return "noneSatisfy";
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
