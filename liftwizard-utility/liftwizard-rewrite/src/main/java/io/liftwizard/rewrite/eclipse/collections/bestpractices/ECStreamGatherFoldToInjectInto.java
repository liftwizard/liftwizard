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
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms {@code stream().gather(Gatherers.fold(initializer, folder)).findFirst().orElseThrow()}
 * to Eclipse Collections {@code injectInto(initial, folder)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * list.stream().gather(Gatherers.fold(() -> 0, Integer::sum)).findFirst().orElseThrow();
 *
 * // After
 * list.injectInto(0, Integer::sum);
 * }</pre>
 *
 * <p>This recipe eliminates the unnecessary Stream intermediary since Eclipse Collections has
 * the {@code injectInto} method directly on {@code RichIterable}. The {@code injectInto} method
 * is Eclipse Collections' native fold operation.
 *
 * <p>{@code Gatherers.fold()} returns a single-element stream containing the folded result,
 * so {@code findFirst().orElseThrow()} (or {@code .get()}) is always used to extract the value.
 *
 * <p>The initializer argument to {@code Gatherers.fold()} is a {@code Supplier}, so this recipe
 * extracts the body of a zero-argument lambda expression (e.g., {@code () -> 0}) to use as
 * the identity value for {@code injectInto()}.
 *
 * <p>Note: This recipe targets Java 24+ code that uses the Gatherers API (JEP 485).
 * Since {@code Gatherers} and {@code Stream.gather()} are Java 24+ APIs, this recipe uses
 * name-based matching for the {@code gather()}, {@code fold()}, and {@code findFirst()} methods
 * rather than type-based matching with {@link MethodMatcher}. The type-based matching
 * with {@link MethodMatcher} requires the receiver type to be resolved, but the return type of
 * the unresolved {@code gather()} call breaks the chain.
 */
public class ECStreamGatherFoldToInjectInto extends Recipe {

	@Override
	public String getDisplayName() {
		return "`stream().gather(Gatherers.fold(init, folder)).findFirst().orElseThrow()` to `injectInto(init, folder)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `collection.stream().gather(Gatherers.fold(() -> init, folder)).findFirst().orElseThrow()` "
			+ "to `collection.injectInto(init, folder)`. This eliminates the unnecessary Stream intermediary since "
			+ "Eclipse Collections has the injectInto method directly on RichIterable. "
			+ "Targets Java 24+ code using the Gatherers API (JEP 485)."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new StreamGatherFoldToInjectIntoVisitor();
	}

	private static final class StreamGatherFoldToInjectIntoVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			// Match: .orElseThrow() or .get() — the unwrap at the end of the chain
			if (
				!"orElseThrow".equals(methodInvocation.getSimpleName())
				&& !"get".equals(methodInvocation.getSimpleName())
			) {
				return methodInvocation;
			}

			if (!this.hasNoArguments(methodInvocation)) {
				return methodInvocation;
			}

			// Match: .findFirst()
			Expression orElseThrowSelect = methodInvocation.getSelect();
			if (!(orElseThrowSelect instanceof J.MethodInvocation findFirstCall)) {
				return methodInvocation;
			}

			if (!"findFirst".equals(findFirstCall.getSimpleName())) {
				return methodInvocation;
			}

			if (!this.hasNoArguments(findFirstCall)) {
				return methodInvocation;
			}

			// Match: .gather(Gatherers.fold(initializer, folder))
			Expression findFirstSelect = findFirstCall.getSelect();
			if (!(findFirstSelect instanceof J.MethodInvocation gatherCall)) {
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
			if (!(gathererArg instanceof J.MethodInvocation foldCall)) {
				return methodInvocation;
			}

			if (!"fold".equals(foldCall.getSimpleName())) {
				return methodInvocation;
			}

			if (!this.isGatherersClass(foldCall)) {
				return methodInvocation;
			}

			List<Expression> foldArguments = foldCall.getArguments();
			if (foldArguments.size() != 2) {
				return methodInvocation;
			}

			Expression supplierArg = foldArguments.get(0);

			// Extract the body of the supplier lambda: () -> value
			Expression initialValueRaw = this.extractSupplierLambdaBody(supplierArg);
			if (initialValueRaw == null) {
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

			// Strip any whitespace prefix inherited from the lambda body
			Expression initialValue = initialValueRaw.withPrefix(Space.EMPTY);
			Expression folderArg = foldArguments.get(1);

			// Transform: collection.injectInto(initialValue, folder)
			J.Identifier injectIntoMethodName = streamCall.getName().withSimpleName("injectInto");

			this.maybeRemoveImport("java.util.stream.Gatherers");

			return streamCall
				.withSelect(collectionExpr)
				.withName(injectIntoMethodName)
				.withArguments(List.of(initialValue, folderArg))
				.withPrefix(methodInvocation.getPrefix());
		}

		private boolean hasNoArguments(J.MethodInvocation method) {
			if (method.getArguments().isEmpty()) {
				return true;
			}
			return method.getArguments().size() == 1 && method.getArguments().get(0) instanceof J.Empty;
		}

		private Expression extractSupplierLambdaBody(Expression expression) {
			if (!(expression instanceof J.Lambda lambda)) {
				return null;
			}
			// Must be a zero-argument lambda: () -> value
			if (!lambda.getParameters().getParameters().isEmpty()) {
				J firstParam = lambda.getParameters().getParameters().get(0);
				if (!(firstParam instanceof J.Empty)) {
					return null;
				}
			}
			J body = lambda.getBody();
			if (body instanceof Expression bodyExpr) {
				return bodyExpr;
			}
			return null;
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
