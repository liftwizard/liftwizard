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

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Rewrites a matched single-arg Eclipse Collections call like {@code select(x -> x.foo(captured))} to its
 * {@code *With} method-reference variant {@code selectWith(Type::foo, captured)}.
 *
 * <p>Two lambda shapes are accepted:
 * <ul>
 *   <li>Shape A — instance method on the lambda parameter: {@code x -> x.foo(captured)} becomes
 *       {@code Type::foo, captured}.</li>
 *   <li>Shape B — equality on the lambda parameter: {@code x -> x.equals(captured)} becomes
 *       {@code Object::equals, captured}.</li>
 * </ul>
 *
 * <p>The static {@link #detect} method is reused by {@link ECDetectIfNoneToDetectWithIfNone} for the
 * structurally different 3-arg {@code detectIfNone} overload.
 */
final class GarbageFreeLambdaVisitor extends JavaIsoVisitor<ExecutionContext> {

	private final MethodMatcher matcher;
	private final String targetMethodName;

	GarbageFreeLambdaVisitor(MethodMatcher matcher, String targetMethodName) {
		this.matcher = matcher;
		this.targetMethodName = targetMethodName;
	}

	@Override
	public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
		J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

		if (!this.matcher.matches(mi)) {
			return mi;
		}
		if (mi.getArguments().size() != 1) {
			return mi;
		}
		if (mi.getSelect() == null) {
			return mi;
		}

		Result result = detect(mi.getArguments().get(0));
		if (result == null) {
			return mi;
		}

		String templateSource =
			"#{any()}."
			+ this.targetMethodName
			+ "("
			+ result.simpleName()
			+ "::"
			+ result.methodName()
			+ ", #{any()})";

		JavaTemplate template = JavaTemplate.builder(templateSource)
			.imports(result.typeFqn())
			.contextSensitive()
			.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
			.build();

		if (!result.typeFqn().startsWith("java.lang.")) {
			this.doAfterVisit(new AddImport<>(result.typeFqn(), null, false));
		}

		return template.apply(
			this.getCursor(),
			mi.getCoordinates().replace(),
			mi.getSelect(),
			result.capturedExpression()
		);
	}

	static Result detect(Expression argument) {
		if (!(argument instanceof J.Lambda lambda)) {
			return null;
		}

		if (lambda.getParameters().getParameters().size() != 1) {
			return null;
		}

		J firstParameter = lambda.getParameters().getParameters().get(0);
		if (!(firstParameter instanceof J.VariableDeclarations varDecls)) {
			return null;
		}
		if (varDecls.getVariables().size() != 1) {
			return null;
		}
		J.VariableDeclarations.NamedVariable namedVariable = varDecls.getVariables().get(0);
		JavaType paramType = namedVariable.getType();
		if (paramType == null) {
			return null;
		}

		J body = lambda.getBody();
		if (body instanceof J.Block block) {
			if (block.getStatements().size() != 1) {
				return null;
			}
			Statement onlyStatement = block.getStatements().get(0);
			if (!(onlyStatement instanceof J.Return returnStatement)) {
				return null;
			}
			Expression returned = returnStatement.getExpression();
			if (returned == null) {
				return null;
			}
			body = returned;
		}

		if (!(body instanceof J.MethodInvocation innerCall)) {
			return null;
		}

		Expression select = innerCall.getSelect();
		if (!(select instanceof J.Identifier selectIdentifier)) {
			return null;
		}
		String paramName = namedVariable.getSimpleName();
		if (!selectIdentifier.getSimpleName().equals(paramName)) {
			return null;
		}

		if (innerCall.getArguments().size() != 1) {
			return null;
		}
		Expression capturedExpression = innerCall.getArguments().get(0);
		if (capturedExpression instanceof J.Empty) {
			return null;
		}

		if (referencesParameter(capturedExpression, paramName)) {
			return null;
		}

		FullyQualified paramFqn = TypeUtils.asFullyQualified(paramType);
		if (paramFqn == null) {
			return null;
		}

		String methodName = innerCall.getSimpleName();

		if (methodName.equals("equals")) {
			return new Result("java.lang.Object", "Object", "equals", capturedExpression);
		}

		String className = paramFqn.getClassName();
		String leafName = className.substring(className.lastIndexOf('.') + 1);
		return new Result(paramFqn.getFullyQualifiedName(), leafName, methodName, capturedExpression);
	}

	private static boolean referencesParameter(Expression expression, String paramName) {
		ReferenceChecker checker = new ReferenceChecker(paramName);
		checker.visit(expression, null);
		return checker.found;
	}

	private static final class ReferenceChecker extends JavaIsoVisitor<Object> {

		private final String paramName;
		private boolean found;

		ReferenceChecker(String paramName) {
			this.paramName = paramName;
		}

		@Override
		public J.Identifier visitIdentifier(J.Identifier identifier, Object ignored) {
			if (!this.found && identifier.getSimpleName().equals(this.paramName)) {
				this.found = true;
			}
			return identifier;
		}
	}

	record Result(String typeFqn, String simpleName, String methodName, Expression capturedExpression) {}
}
