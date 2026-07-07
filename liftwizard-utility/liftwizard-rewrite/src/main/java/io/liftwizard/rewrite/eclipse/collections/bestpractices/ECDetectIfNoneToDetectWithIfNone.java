/*
 * Copyright 2026 Craig Motlin
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

import io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsTemplateStubs;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

/**
 * Converts {@code richIterable.detectIfNone(x -> x.foo(captured), defaultFn)} to
 * {@code richIterable.detectWithIfNone(Type::foo, captured, defaultFn)}.
 *
 * <p>The source method takes two arguments (predicate + {@code Function0}); the target takes three
 * (predicate2 + captured + {@code Function0}). The captured value is spliced between the method reference
 * and the {@code Function0}.
 *
 * <p>See {@link GarbageFreeLambdaRecipe} for the lambda shape rules.
 */
public class ECDetectIfNoneToDetectWithIfNone extends Recipe {

	private static final String[] STUBS = EclipseCollectionsTemplateStubs.RICH_ITERABLE;

	private static final MethodMatcher DETECT_IF_NONE_MATCHER = new MethodMatcher(
		"org.eclipse.collections.api.RichIterable detectIfNone(org.eclipse.collections.api.block.predicate.Predicate, org.eclipse.collections.api.block.function.Function0)",
		true
	);

	@Override
	public String getDisplayName() {
		return "`detectIfNone(x -> x.foo(captured), defaultFn)` → `detectWithIfNone(Type::foo, captured, defaultFn)`";
	}

	@Override
	public String getDescription() {
		return (
			"Converts `richIterable.detectIfNone(x -> x.foo(captured), defaultFn)` to "
			+ "`richIterable.detectWithIfNone(Type::foo, captured, defaultFn)` for Eclipse Collections types. "
			+ "The captured value must not reference the lambda parameter."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(DETECT_IF_NONE_MATCHER),
			new DetectIfNoneToDetectWithIfNoneVisitor()
		);
	}

	private static final class DetectIfNoneToDetectWithIfNoneVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

			if (!DETECT_IF_NONE_MATCHER.matches(mi)) {
				return mi;
			}
			if (mi.getArguments().size() != 2) {
				return mi;
			}
			if (mi.getSelect() == null) {
				return mi;
			}

			GarbageFreeLambdaVisitor.Result result = GarbageFreeLambdaVisitor.detect(mi.getArguments().get(0));
			if (result == null) {
				return mi;
			}

			Expression defaultFunction = mi.getArguments().get(1);

			String selectPlaceholder = "#{any(org.eclipse.collections.api.RichIterable<" + result.typeFqn() + ">)}";
			String templateSource =
				selectPlaceholder
				+ ".detectWithIfNone("
				+ result.simpleName()
				+ "::"
				+ result.methodName()
				+ ", #{any()}, #{any()})";

			JavaTemplate template = JavaTemplate.builder(templateSource)
				.imports(result.typeFqn())
				.contextSensitive()
				.javaParser(JavaParser.fromJavaVersion().dependsOn(STUBS))
				.build();

			if (!result.typeFqn().startsWith("java.lang.")) {
				this.doAfterVisit(new AddImport<>(result.typeFqn(), null, false));
			}

			J.MethodInvocation replacement = template.apply(
				this.getCursor(),
				mi.getCoordinates().replace(),
				mi.getSelect(),
				GarbageFreeLambdaVisitor.spaceBefore(result.capturedExpression()),
				defaultFunction
			);
			replacement = result.withTypedMemberReferences(replacement);
			if (mi.getMethodType() == null) {
				return replacement;
			}
			JavaType.Method methodType = mi.getMethodType().withName("detectWithIfNone");
			return replacement.withMethodType(methodType).withName(replacement.getName().withType(methodType));
		}
	}
}
