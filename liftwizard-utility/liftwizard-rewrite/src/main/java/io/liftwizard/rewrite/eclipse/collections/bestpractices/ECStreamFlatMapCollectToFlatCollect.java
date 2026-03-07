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
 * Transforms stream flatMap+collect operations to Eclipse Collections flatCollect.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().flatMap(x -> x.items().stream()).collect(Collectors.toList());
 *
 * // After
 * list.flatCollect(x -> x.items());
 * }</pre>
 *
 * <p>Also handles toSet():
 * <pre>{@code
 * // Before
 * list.stream().flatMap(x -> x.items().stream()).collect(Collectors.toSet());
 *
 * // After
 * list.flatCollect(x -> x.items()).toSet();
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code flatCollect} method directly on {@code RichIterable}.
 * The lambda body must end with a {@code .stream()} call, which is stripped since {@code flatCollect}
 * expects an {@code Iterable} rather than a {@code Stream}.
 *
 * <p>Note: This recipe does NOT convert toUnmodifiableList() or toUnmodifiableSet() because
 * unmodifiable and immutable are different concepts.
 */
public class ECStreamFlatMapCollectToFlatCollect extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher FLAT_MAP_MATCHER = new MethodMatcher(
		"java.util.stream.Stream flatMap(java.util.function.Function)"
	);

	private static final MethodMatcher TO_LIST_MATCHER = new MethodMatcher("java.util.stream.Collectors toList()");

	private static final MethodMatcher TO_SET_MATCHER = new MethodMatcher("java.util.stream.Collectors toSet()");

	@Override
	public String getDisplayName() {
		return "`stream().flatMap(fn).collect(Collectors.toList())` to `flatCollect(fn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().flatMap(fn).collect(Collectors.toList())` to `collection.flatCollect(fn)` "
			+ "and `collection.stream().flatMap(fn).collect(Collectors.toSet())` to `collection.flatCollect(fn).toSet()`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the flatCollect method directly on RichIterable. "
			+ "The lambda body must end with a .stream() call, which is stripped since "
			+ "flatCollect expects an Iterable rather than a Stream."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COLLECT_MATCHER), new StreamFlatMapCollectToFlatCollectVisitor());
	}

	private static final class StreamFlatMapCollectToFlatCollectVisitor extends JavaIsoVisitor<ExecutionContext> {

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
			if (!(collectSelect instanceof J.MethodInvocation flatMapCall)) {
				return methodInvocation;
			}

			if (!FLAT_MAP_MATCHER.matches(flatMapCall)) {
				return methodInvocation;
			}

			Expression flatMapSelect = flatMapCall.getSelect();
			if (!(flatMapSelect instanceof J.MethodInvocation streamCall)) {
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

			List<Expression> flatMapArguments = flatMapCall.getArguments();
			if (flatMapArguments.isEmpty()) {
				return methodInvocation;
			}

			Expression flatMapArg = flatMapArguments.get(0);
			Expression transformedArg = this.stripStreamFromLambda(flatMapArg);
			if (transformedArg == null) {
				return methodInvocation;
			}

			List<Expression> newArguments = List.of(transformedArg);

			J.Identifier flatCollectMethodName = methodInvocation.getName().withSimpleName("flatCollect");

			if (isToSet) {
				J.MethodInvocation flatCollectCall = flatMapCall
					.withSelect(collectionExpr.withPrefix(Space.EMPTY))
					.withName(flatMapCall.getName().withSimpleName("flatCollect"))
					.withArguments(newArguments);

				J.Identifier toSetMethodName = streamCall.getName().withSimpleName("toSet");
				return streamCall
					.withSelect(flatCollectCall.withPrefix(collectSelect.getPrefix()))
					.withName(toSetMethodName)
					.withPrefix(methodInvocation.getPrefix());
			}

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(flatCollectMethodName)
				.withArguments(newArguments);
		}

		/**
		 * Strips the trailing .stream() call from a lambda body.
		 *
		 * <p>For example, transforms {@code x -> x.items().stream()} to {@code x -> x.items()}.
		 *
		 * @return the transformed lambda, or null if the lambda body does not end with .stream()
		 */
		private Expression stripStreamFromLambda(Expression expression) {
			if (!(expression instanceof J.Lambda lambda)) {
				return null;
			}

			J body = lambda.getBody();
			if (!(body instanceof J.MethodInvocation bodyMethodInvocation)) {
				return null;
			}

			if (!"stream".equals(bodyMethodInvocation.getSimpleName())) {
				return null;
			}

			if (!bodyMethodInvocation.getArguments().isEmpty()) {
				if (bodyMethodInvocation.getArguments().size() != 1) {
					return null;
				}
				if (!(bodyMethodInvocation.getArguments().get(0) instanceof J.Empty)) {
					return null;
				}
			}

			Expression streamSelect = bodyMethodInvocation.getSelect();
			if (streamSelect == null) {
				return null;
			}

			return lambda.withBody(streamSelect.withPrefix(body.getPrefix()));
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
