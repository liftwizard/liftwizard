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
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms {@code stream().gather(Gatherers.windowFixed(n)).collect(Collectors.toList())}
 * to Eclipse Collections {@code chunk(n)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().gather(Gatherers.windowFixed(3)).collect(Collectors.toList());
 *
 * // After
 * list.chunk(3);
 * }</pre>
 *
 * <p>This recipe eliminates the unnecessary Stream intermediary since Eclipse Collections has
 * the {@code chunk} method directly on {@code RichIterable}. The {@code chunk} method returns
 * a {@code LazyIterable<RichIterable<T>>} which provides a rich collection API.
 *
 * <p>Note: This recipe targets Java 24+ code that uses the Gatherers API (JEP 485).
 * Since {@code Gatherers} and {@code Stream.gather()} are Java 24+ APIs, this recipe uses
 * name-based matching for the {@code gather()}, {@code collect()}, and {@code windowFixed()}
 * methods rather than type-based matching with {@link MethodMatcher}. The type-based matching
 * with {@link MethodMatcher} requires the receiver type to be resolved, but the return type of
 * the unresolved {@code gather()} call breaks the chain.
 */
public class ECStreamGatherWindowFixedToChunk extends Recipe {

	private static final MethodMatcher TO_LIST_MATCHER = new MethodMatcher("java.util.stream.Collectors toList()");

	@Override
	public String getDisplayName() {
		return "`stream().gather(Gatherers.windowFixed(n)).collect(toList())` to `chunk(n)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().gather(Gatherers.windowFixed(n)).collect(Collectors.toList())` "
			+ "to `collection.chunk(n)`. This eliminates the unnecessary Stream intermediary since "
			+ "Eclipse Collections has the chunk method directly on RichIterable. "
			+ "Targets Java 24+ code using the Gatherers API (JEP 485)."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new StreamGatherWindowFixedToChunkVisitor();
	}

	private static final class StreamGatherWindowFixedToChunkVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			// Match: .collect(Collectors.toList()) by name because the receiver
			// is the return type of gather() which is unresolved on Java < 24
			if (!"collect".equals(methodInvocation.getSimpleName())) {
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

			if (!TO_LIST_MATCHER.matches(collectorCall)) {
				return methodInvocation;
			}

			// Match: .gather(Gatherers.windowFixed(n)) by name since it's a Java 24+ API
			Expression collectSelect = methodInvocation.getSelect();
			if (!(collectSelect instanceof J.MethodInvocation gatherCall)) {
				return methodInvocation;
			}

			if (!"gather".equals(gatherCall.getSimpleName())) {
				return methodInvocation;
			}

			List<Expression> gatherArguments = gatherCall.getArguments();
			if (gatherArguments.size() != 1) {
				return methodInvocation;
			}

			Expression gathererArg = gatherArguments.get(0);
			if (!(gathererArg instanceof J.MethodInvocation windowFixedCall)) {
				return methodInvocation;
			}

			if (!"windowFixed".equals(windowFixedCall.getSimpleName())) {
				return methodInvocation;
			}

			if (!this.isGatherersClass(windowFixedCall)) {
				return methodInvocation;
			}

			List<Expression> windowFixedArguments = windowFixedCall.getArguments();
			if (windowFixedArguments.size() != 1) {
				return methodInvocation;
			}

			// Match: .stream()
			Expression gatherSelect = gatherCall.getSelect();
			if (!(gatherSelect instanceof J.MethodInvocation streamCall)) {
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

			// Transform: collection.chunk(n)
			Expression sizeArg = windowFixedArguments.get(0);
			// Use streamCall as the base to preserve the dot formatting between
			// the collection expression and the method name
			J.Identifier chunkMethodName = streamCall.getName().withSimpleName("chunk");

			this.maybeRemoveImport("java.util.stream.Collectors");
			this.maybeRemoveImport("java.util.stream.Gatherers");

			return streamCall
				.withSelect(collectionExpr)
				.withName(chunkMethodName)
				.withArguments(List.of(sizeArg))
				.withPrefix(methodInvocation.getPrefix());
		}

		private boolean isGatherersClass(J.MethodInvocation method) {
			Expression select = method.getSelect();
			if (select instanceof J.Identifier identifier) {
				return "Gatherers".equals(identifier.getSimpleName());
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
