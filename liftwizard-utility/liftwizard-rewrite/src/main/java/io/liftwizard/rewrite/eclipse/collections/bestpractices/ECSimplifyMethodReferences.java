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

import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Simplifies redundant functional method references by removing unnecessary method calls.
 *
 * <p>When a variable is already the correct functional type and you pass it as a method reference
 * that calls its single abstract method, this is redundant and can be simplified.
 *
 * <p>Examples:
 * <pre>{@code
 * // Before
 * list.select(predicate::accept)
 * list.select(predicate::test)
 * list.collect(function::valueOf)
 * list.collect(function::apply)
 * list.forEach(procedure::value)
 * list.forEach(consumer::accept)
 *
 * // After
 * list.select(predicate)
 * list.select(predicate)
 * list.collect(function)
 * list.collect(function)
 * list.forEach(procedure)
 * list.forEach(consumer)
 * }</pre>
 *
 * <p>This applies to both Eclipse Collections functional types (Predicate, Function, Procedure)
 * and JDK functional types (java.util.function.Predicate, Function, Consumer).
 */
public class ECSimplifyMethodReferences extends Recipe {

	private static final Set<String> PREDICATE_METHODS = Sets.fixedSize.with("accept", "test");
	private static final Set<String> FUNCTION_METHODS = Sets.fixedSize.with("valueOf", "apply");
	private static final Set<String> PROCEDURE_METHODS = Sets.fixedSize.with("value", "accept");

	private static final String EC_PREDICATE = "org.eclipse.collections.api.block.predicate.Predicate";
	private static final String EC_FUNCTION = "org.eclipse.collections.api.block.function.Function";
	private static final String EC_PROCEDURE = "org.eclipse.collections.api.block.procedure.Procedure";

	private static final String JDK_PREDICATE = "java.util.function.Predicate";
	private static final String JDK_FUNCTION = "java.util.function.Function";
	private static final String JDK_CONSUMER = "java.util.function.Consumer";

	@Override
	public String getDisplayName() {
		return "Simplify redundant functional method references";
	}

	@Override
	public String getDescription() {
		return (
			"Simplifies `predicate::accept` to `predicate`, `function::valueOf` to `function`, "
			+ "and similar patterns where a functional interface variable is unnecessarily "
			+ "dereferenced with its single abstract method."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new SimplifyMethodReferencesVisitor();
	}

	private static final class SimplifyMethodReferencesVisitor extends JavaVisitor<ExecutionContext> {

		@Override
		public J visitMemberReference(J.MemberReference memberRef, ExecutionContext ctx) {
			J result = super.visitMemberReference(memberRef, ctx);

			if (!(result instanceof J.MemberReference visitedMemberRef)) {
				return result;
			}

			Expression containing = visitedMemberRef.getContaining();
			if (!(containing instanceof J.Identifier identifier)) {
				return result;
			}

			String methodName = visitedMemberRef.getReference().getSimpleName();
			JavaType containingType = identifier.getType();

			if (containingType == null) {
				return result;
			}

			if (this.isRedundantMethodReference(containingType, methodName)) {
				JavaType targetType = this.getExpectedTargetType(visitedMemberRef);
				if (targetType != null && !this.areTypesCompatible(containingType, targetType)) {
					return result;
				}

				return identifier.withPrefix(visitedMemberRef.getPrefix());
			}

			return result;
		}

		private boolean isRedundantMethodReference(JavaType type, String methodName) {
			return (
				this.isTypeCompatibleWithMethods(type, EC_PREDICATE, PREDICATE_METHODS, methodName)
				|| this.isTypeCompatibleWithMethods(type, JDK_PREDICATE, PREDICATE_METHODS, methodName)
				|| this.isTypeCompatibleWithMethods(type, EC_FUNCTION, FUNCTION_METHODS, methodName)
				|| this.isTypeCompatibleWithMethods(type, JDK_FUNCTION, FUNCTION_METHODS, methodName)
				|| this.isTypeCompatibleWithMethods(type, EC_PROCEDURE, PROCEDURE_METHODS, methodName)
				|| this.isTypeCompatibleWithMethods(type, JDK_CONSUMER, PROCEDURE_METHODS, methodName)
			);
		}

		private boolean isTypeCompatibleWithMethods(
			JavaType type,
			String expectedTypeFqn,
			Set<String> validMethods,
			String methodName
		) {
			if (!validMethods.contains(methodName)) {
				return false;
			}
			return TypeUtils.isAssignableTo(expectedTypeFqn, type);
		}

		private JavaType getExpectedTargetType(J.MemberReference memberRef) {
			var cursor = this.getCursor().getParent();
			while (cursor != null) {
				Object value = cursor.getValue();

				if (value instanceof J.VariableDeclarations varDecls) {
					return varDecls.getTypeAsFullyQualified();
				}

				if (value instanceof J.MethodInvocation methodInv) {
					JavaType paramType = this.getMethodParameterType(memberRef, methodInv);
					if (paramType != null) {
						return paramType;
					}
				}

				if (value instanceof J.Assignment assignment) {
					return assignment.getVariable().getType();
				}

				cursor = cursor.getParent();
			}

			return null;
		}

		private JavaType getMethodParameterType(J.MemberReference memberRef, J.MethodInvocation methodInv) {
			java.util.List<Expression> arguments = methodInv.getArguments();
			int argIndex = -1;
			for (int i = 0; i < arguments.size(); i++) {
				if (arguments.get(i) == memberRef) {
					argIndex = i;
					break;
				}
			}

			if (argIndex < 0) {
				return null;
			}

			JavaType methodType = methodInv.getMethodType();
			if (methodType instanceof JavaType.Method method) {
				java.util.List<JavaType> paramTypes = method.getParameterTypes();
				if (argIndex < paramTypes.size()) {
					return paramTypes.get(argIndex);
				}
			}

			return null;
		}

		private boolean areTypesCompatible(JavaType sourceType, JavaType targetType) {
			if (sourceType == null || targetType == null) {
				return true;
			}

			if (TypeUtils.isAssignableTo(targetType, sourceType)) {
				return true;
			}

			// Fallback: compare raw fully-qualified names to handle generic variance
			FullyQualified sourceFqn = TypeUtils.asFullyQualified(sourceType);
			FullyQualified targetFqn = TypeUtils.asFullyQualified(targetType);

			if (sourceFqn == null || targetFqn == null) {
				return true;
			}

			return sourceFqn.getFullyQualifiedName().equals(targetFqn.getFullyQualifiedName());
		}
	}
}
