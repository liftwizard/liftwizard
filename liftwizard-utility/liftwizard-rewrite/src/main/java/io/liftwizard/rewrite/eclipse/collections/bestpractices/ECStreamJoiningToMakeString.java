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
 * Transforms stream collect+Collectors.joining operations to Eclipse Collections makeString.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().collect(Collectors.joining(", "));
 * list.stream().map(Object::toString).collect(Collectors.joining(", "));
 *
 * // After
 * list.makeString(", ");
 * list.makeString(", ");
 * }</pre>
 *
 * <p>This recipe eliminates unnecessary Stream intermediary operations for Eclipse Collections types,
 * since Eclipse Collections has the {@code makeString} method directly on {@code RichIterable}.
 */
public class ECStreamJoiningToMakeString extends Recipe {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher JOINING_ONE_ARG_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors joining(java.lang.CharSequence)"
	);

	private static final MethodMatcher MAP_MATCHER = new MethodMatcher(
		"java.util.stream.Stream map(java.util.function.Function)"
	);

	@Override
	public String getDisplayName() {
		return "`stream().collect(Collectors.joining())` to `makeString()`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().collect(Collectors.joining(delimiter))` to `collection.makeString(delimiter)`. "
			+ "Also handles `collection.stream().map(Object::toString).collect(Collectors.joining(delimiter))`. "
			+ "This eliminates the unnecessary Stream intermediary since Eclipse Collections has "
			+ "the makeString method directly on RichIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(COLLECT_MATCHER), new StreamJoiningToMakeStringVisitor());
	}

	private static final class StreamJoiningToMakeStringVisitor extends JavaIsoVisitor<ExecutionContext> {

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
			if (!(collectorArg instanceof J.MethodInvocation joiningCall)) {
				return methodInvocation;
			}

			if (!JOINING_ONE_ARG_MATCHER.matches(joiningCall)) {
				return methodInvocation;
			}

			Expression collectSelect = methodInvocation.getSelect();
			if (collectSelect == null) {
				return methodInvocation;
			}

			Expression streamOrMapSelect;
			if (collectSelect instanceof J.MethodInvocation mapCall && MAP_MATCHER.matches(mapCall)) {
				List<Expression> mapArgs = mapCall.getArguments();
				if (mapArgs.size() == 1 && this.isToStringMapper(mapArgs.get(0))) {
					streamOrMapSelect = mapCall.getSelect();
				} else {
					return methodInvocation;
				}
			} else {
				streamOrMapSelect = collectSelect;
			}

			if (!(streamOrMapSelect instanceof J.MethodInvocation streamCall)) {
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

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName("makeString");

			List<Expression> joiningArgs = joiningCall.getArguments();

			return methodInvocation
				.withSelect(collectionExpr.withPrefix(collectSelect.getPrefix()))
				.withName(newMethodName)
				.withArguments(joiningArgs);
		}

		private boolean isToStringMapper(Expression mapFunction) {
			if (mapFunction instanceof J.MemberReference memberRef) {
				String methodName = memberRef.getReference().getSimpleName();
				return "toString".equals(methodName);
			}
			if (mapFunction instanceof J.Lambda lambda) {
				if (lambda.getBody() instanceof J.MethodInvocation lambdaBody) {
					return (
						("toString".equals(lambdaBody.getSimpleName()) && lambdaBody.getArguments().isEmpty())
						|| (lambdaBody.getArguments().size() == 1
							&& lambdaBody.getArguments().get(0) instanceof J.Empty)
					);
				}
			}
			return false;
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
