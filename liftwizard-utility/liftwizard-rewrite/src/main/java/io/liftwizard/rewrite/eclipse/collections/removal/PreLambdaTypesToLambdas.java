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

package io.liftwizard.rewrite.eclipse.collections.removal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Replaces pre-lambda Eclipse Collections utility classes with equivalent Java
 * lambdas and method references.
 *
 * <p>Method-reference substitutions are guarded by {@link MethodReferenceContextChecks}
 * to avoid producing uncompilable code: a method reference cannot bind to a concrete
 * implementing class, to {@code java.lang.Object}, to a {@code null} receiver, or to
 * a call site with overload ambiguity. Lambda substitutions carry their own
 * functional-interface target type and need no such checks.
 */
public class PreLambdaTypesToLambdas extends Recipe {

	private static final String ADD_FUNCTION_FQN = "org.eclipse.collections.impl.block.function.AddFunction";
	private static final String MULTIPLY_FUNCTION_FQN = "org.eclipse.collections.impl.block.function.MultiplyFunction";
	private static final String SUBTRACT_FUNCTION_FQN = "org.eclipse.collections.impl.block.function.SubtractFunction";
	private static final String FUNCTIONS_FQN = "org.eclipse.collections.impl.block.factory.Functions";
	private static final String PASS_THRU_FUNCTION_0_FQN =
		"org.eclipse.collections.impl.block.function.PassThruFunction0";
	private static final String MAP_PUT_PROCEDURE_FQN = "org.eclipse.collections.impl.block.procedure.MapPutProcedure";
	private static final String COLLECTION_ADD_PROCEDURE_FQN =
		"org.eclipse.collections.impl.block.procedure.CollectionAddProcedure";
	private static final String COLLECTION_REMOVE_PROCEDURE_FQN =
		"org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure";
	private static final String PROCEDURE_FQN = "org.eclipse.collections.api.block.procedure.Procedure";
	private static final String PROCEDURE_2_FQN = "org.eclipse.collections.api.block.procedure.Procedure2";
	private static final String FUNCTION_0_FQN = "org.eclipse.collections.api.block.function.Function0";

	/** Static-field substitutions (e.g. {@code AddFunction.INTEGER} → {@code Integer::sum}). */
	private static final List<FieldSubstitution> FIELD_SUBSTITUTIONS = List.of(
		FieldSubstitution.methodReference(ADD_FUNCTION_FQN, "INTEGER", "Integer::sum", 2),
		FieldSubstitution.methodReference(ADD_FUNCTION_FQN, "LONG", "Long::sum", 2),
		FieldSubstitution.methodReference(ADD_FUNCTION_FQN, "DOUBLE", "Double::sum", 2),
		FieldSubstitution.methodReference(ADD_FUNCTION_FQN, "FLOAT", "Float::sum", 2),
		FieldSubstitution.lambda(MULTIPLY_FUNCTION_FQN, "INTEGER", "(Integer a, Integer b) -> a * b"),
		FieldSubstitution.lambda(MULTIPLY_FUNCTION_FQN, "LONG", "(Long a, Long b) -> a * b"),
		FieldSubstitution.lambda(MULTIPLY_FUNCTION_FQN, "DOUBLE", "(Double a, Double b) -> a * b"),
		FieldSubstitution.lambda(SUBTRACT_FUNCTION_FQN, "INTEGER", "(Integer a, Integer b) -> a - b"),
		FieldSubstitution.lambda(SUBTRACT_FUNCTION_FQN, "LONG", "(Long a, Long b) -> a - b"),
		FieldSubstitution.lambda(SUBTRACT_FUNCTION_FQN, "DOUBLE", "(Double a, Double b) -> a - b")
	);

	/** Static no-arg method substitutions (e.g. {@code Functions.getStringTrim()} → {@code String::trim}). */
	private static final List<MethodSubstitution> METHOD_SUBSTITUTIONS = List.of(
		new MethodSubstitution(FUNCTIONS_FQN, "getStringToInteger()", "Integer::valueOf", 1),
		new MethodSubstitution(FUNCTIONS_FQN, "getStringTrim()", "String::trim", 1),
		new MethodSubstitution(FUNCTIONS_FQN, "getToClass()", "Object::getClass", 1),
		new MethodSubstitution(FUNCTIONS_FQN, "getToString()", "Object::toString", 1)
	);

	/**
	 * {@code SomeType.on(arg)} style static factory substitutions. The argument is
	 * threaded through into the replacement template.
	 */
	private static final List<FactoryMethodSubstitution> FACTORY_METHOD_SUBSTITUTIONS = List.of(
		new FactoryMethodSubstitution(
			COLLECTION_ADD_PROCEDURE_FQN,
			"on(java.util.Collection)",
			COLLECTION_ADD_PROCEDURE_FQN,
			PROCEDURE_FQN,
			"#{any(java.util.Collection)}::add",
			1
		),
		new FactoryMethodSubstitution(
			COLLECTION_REMOVE_PROCEDURE_FQN,
			"on(java.util.Collection)",
			COLLECTION_REMOVE_PROCEDURE_FQN,
			PROCEDURE_FQN,
			"#{any(java.util.Collection)}::remove",
			1
		)
	);

	/**
	 * {@code new SomeType<>(arg)} constructor substitutions. The first argument
	 * is threaded through into the replacement template (the placeholder type is
	 * the {@code argType}).
	 */
	private static final List<ConstructorSubstitution> CONSTRUCTOR_SUBSTITUTIONS = List.of(
		new ConstructorSubstitution(
			COLLECTION_ADD_PROCEDURE_FQN,
			PROCEDURE_FQN,
			"java.util.Collection",
			"#{any(java.util.Collection)}::add",
			1
		),
		new ConstructorSubstitution(
			COLLECTION_REMOVE_PROCEDURE_FQN,
			PROCEDURE_FQN,
			"java.util.Collection",
			"#{any(java.util.Collection)}::remove",
			1
		),
		new ConstructorSubstitution(
			MAP_PUT_PROCEDURE_FQN,
			PROCEDURE_2_FQN,
			"java.util.Map",
			"#{any(java.util.Map)}::put",
			2
		),
		new ConstructorSubstitution(PASS_THRU_FUNCTION_0_FQN, FUNCTION_0_FQN, "java.lang.Object", "() -> #{any()}", 0)
	);

	@Override
	public String getDisplayName() {
		return "Replace pre-lambda Eclipse Collections types with lambdas and method references";
	}

	@Override
	public String getDescription() {
		return (
			"Replace Eclipse Collections' pre-lambda utility classes and constants with equivalent Java lambdas"
			+ " and method references (e.g. `AddFunction.INTEGER` → `Integer::sum`,"
			+ " `new CollectionAddProcedure<>(coll)` → `coll::add`, `MultiplyFunction.INTEGER` → `(a, b) -> a * b`)."
			+ " Method-reference substitutions are skipped when the use-site target type is the concrete"
			+ " implementing class or `java.lang.Object`, when the receiver is a `null` literal, or when the"
			+ " substituted method reference would create an ambiguous overload at the call site."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		TreeVisitor<?, ExecutionContext> precondition = Preconditions.or(
			new UsesType<>(ADD_FUNCTION_FQN, false),
			new UsesType<>(MULTIPLY_FUNCTION_FQN, false),
			new UsesType<>(SUBTRACT_FUNCTION_FQN, false),
			new UsesType<>(FUNCTIONS_FQN, false),
			new UsesType<>(PASS_THRU_FUNCTION_0_FQN, false),
			new UsesType<>(MAP_PUT_PROCEDURE_FQN, false),
			new UsesType<>(COLLECTION_ADD_PROCEDURE_FQN, false),
			new UsesType<>(COLLECTION_REMOVE_PROCEDURE_FQN, false)
		);
		return Preconditions.check(precondition, new Visitor());
	}

	/** Replacement of a static field reference. */
	private record FieldSubstitution(
		String declaringTypeFqn,
		String fieldName,
		String replacement,
		int methodReferenceArity
	) {
		static FieldSubstitution methodReference(String typeFqn, String fieldName, String replacement, int arity) {
			return new FieldSubstitution(typeFqn, fieldName, replacement, arity);
		}

		static FieldSubstitution lambda(String typeFqn, String fieldName, String replacement) {
			return new FieldSubstitution(typeFqn, fieldName, replacement, 0);
		}

		boolean needsContextChecks() {
			return methodReferenceArity > 0;
		}
	}

	/** Replacement of a zero-argument static method invocation. */
	private record MethodSubstitution(
		String declaringTypeFqn,
		String methodSignature,
		String replacement,
		int methodReferenceArity
	) {}

	/**
	 * Replacement of a {@code SomeType.factory(arg)} static method call. The
	 * argument is substituted into {@code replacement}.
	 */
	private record FactoryMethodSubstitution(
		String declaringTypeFqn,
		String methodSignature,
		String concreteFqn,
		String functionalInterfaceFqn,
		String replacement,
		int methodReferenceArity
	) {}

	/**
	 * Replacement of a {@code new SomeType<>(arg)} constructor call. The first
	 * argument is substituted into {@code replacement}; the placeholder type
	 * declared in the template must be {@code argType}.
	 */
	private record ConstructorSubstitution(
		String concreteFqn,
		String functionalInterfaceFqn,
		String argType,
		String replacement,
		int methodReferenceArity
	) {
		boolean needsContextChecks() {
			return methodReferenceArity > 0;
		}
	}

	private static final class Visitor extends JavaVisitor<ExecutionContext> {

		private final Map<String, MethodMatcher> methodMatchers = buildMatchers();
		private final Map<String, MethodMatcher> factoryMatchers = buildFactoryMatchers();

		private static Map<String, MethodMatcher> buildMatchers() {
			Map<String, MethodMatcher> result = new LinkedHashMap<>();
			for (MethodSubstitution sub : METHOD_SUBSTITUTIONS) {
				result.put(
					sub.declaringTypeFqn() + " " + sub.methodSignature(),
					new MethodMatcher(sub.declaringTypeFqn() + " " + sub.methodSignature())
				);
			}
			return result;
		}

		private static Map<String, MethodMatcher> buildFactoryMatchers() {
			Map<String, MethodMatcher> result = new LinkedHashMap<>();
			for (FactoryMethodSubstitution sub : FACTORY_METHOD_SUBSTITUTIONS) {
				result.put(
					sub.declaringTypeFqn() + " " + sub.methodSignature(),
					new MethodMatcher(sub.declaringTypeFqn() + " " + sub.methodSignature())
				);
			}
			return result;
		}

		@Override
		public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
			J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);
			for (ConstructorSubstitution sub : CONSTRUCTOR_SUBSTITUTIONS) {
				if (!TypeUtils.isOfClassType(nc.getType(), sub.concreteFqn())) {
					continue;
				}
				if (nc.getArguments() == null || nc.getArguments().isEmpty()) {
					return nc;
				}
				Expression arg = nc.getArguments().get(0);
				if (sub.needsContextChecks() && !passesContextChecks(arg, sub)) {
					return nc;
				}
				JavaTemplate template = JavaTemplate.builder(sub.replacement()).build();
				J replaced = template.apply(this.getCursor(), nc.getCoordinates().replace(), arg);
				this.maybeRemoveImport(sub.concreteFqn());
				return replaced;
			}
			return nc;
		}

		@Override
		public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation invocation = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

			for (FactoryMethodSubstitution sub : FACTORY_METHOD_SUBSTITUTIONS) {
				MethodMatcher matcher = factoryMatchers.get(sub.declaringTypeFqn() + " " + sub.methodSignature());
				if (matcher == null || !matcher.matches(invocation)) {
					continue;
				}
				Expression arg = invocation.getArguments().get(0);
				if (!passesContextChecks(arg, sub)) {
					return invocation;
				}
				JavaTemplate template = JavaTemplate.builder(sub.replacement()).build();
				J replaced = template.apply(this.getCursor(), invocation.getCoordinates().replace(), arg);
				this.maybeRemoveImport(sub.declaringTypeFqn());
				return replaced;
			}

			for (MethodSubstitution sub : METHOD_SUBSTITUTIONS) {
				MethodMatcher matcher = methodMatchers.get(sub.declaringTypeFqn() + " " + sub.methodSignature());
				if (matcher == null || !matcher.matches(invocation)) {
					continue;
				}
				if (!passesMethodReferenceChecks(sub.methodReferenceArity())) {
					return invocation;
				}
				JavaTemplate template = JavaTemplate.builder(sub.replacement()).build();
				J replaced = template.apply(this.getCursor(), invocation.getCoordinates().replace());
				this.maybeRemoveImport(sub.declaringTypeFqn());
				return replaced;
			}

			return invocation;
		}

		@Override
		public J visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
			J.FieldAccess fa = (J.FieldAccess) super.visitFieldAccess(fieldAccess, ctx);
			FieldSubstitution sub = lookupFieldSubstitution(fa.getTarget().getType(), fa.getSimpleName());
			if (sub == null) {
				return fa;
			}
			if (sub.needsContextChecks() && !passesMethodReferenceChecks(sub.methodReferenceArity())) {
				return fa;
			}
			JavaTemplate template = JavaTemplate.builder(sub.replacement()).build();
			J replaced = template.apply(this.getCursor(), fa.getCoordinates().replace());
			this.maybeRemoveImport(sub.declaringTypeFqn());
			return replaced;
		}

		@Override
		public J visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
			J.Identifier id = (J.Identifier) super.visitIdentifier(identifier, ctx);
			if (isFieldAccessName(this.getCursor())) {
				return id;
			}
			JavaType.Variable fieldType = id.getFieldType();
			if (fieldType == null) {
				return id;
			}
			FieldSubstitution sub = lookupFieldSubstitution(fieldType.getOwner(), id.getSimpleName());
			if (sub == null) {
				return id;
			}
			if (sub.needsContextChecks() && !passesMethodReferenceChecks(sub.methodReferenceArity())) {
				return id;
			}
			JavaTemplate template = JavaTemplate.builder(sub.replacement()).build();
			J replaced = template.apply(this.getCursor(), id.getCoordinates().replace());
			this.maybeRemoveImport(sub.declaringTypeFqn());
			return replaced;
		}

		private static FieldSubstitution lookupFieldSubstitution(JavaType owner, String fieldName) {
			for (FieldSubstitution sub : FIELD_SUBSTITUTIONS) {
				if (sub.fieldName().equals(fieldName) && TypeUtils.isOfClassType(owner, sub.declaringTypeFqn())) {
					return sub;
				}
			}
			return null;
		}

		private boolean passesContextChecks(Expression arg, ConstructorSubstitution sub) {
			if (MethodReferenceContextChecks.isNullLiteral(arg)) {
				return false;
			}
			if (
				MethodReferenceContextChecks.targetIsConcreteClass(
					this.getCursor(),
					sub.concreteFqn(),
					sub.functionalInterfaceFqn()
				)
			) {
				return false;
			}
			return passesMethodReferenceChecks(sub.methodReferenceArity());
		}

		private boolean passesContextChecks(Expression arg, FactoryMethodSubstitution sub) {
			if (MethodReferenceContextChecks.isNullLiteral(arg)) {
				return false;
			}
			if (
				MethodReferenceContextChecks.targetIsConcreteClass(
					this.getCursor(),
					sub.concreteFqn(),
					sub.functionalInterfaceFqn()
				)
			) {
				return false;
			}
			return passesMethodReferenceChecks(sub.methodReferenceArity());
		}

		private boolean passesMethodReferenceChecks(int arity) {
			if (MethodReferenceContextChecks.targetIsObject(this.getCursor())) {
				return false;
			}
			if (MethodReferenceContextChecks.wouldBeAmbiguousAtCallSite(this.getCursor(), arity)) {
				return false;
			}
			return true;
		}

		private static boolean isFieldAccessName(org.openrewrite.Cursor cursor) {
			org.openrewrite.Cursor parent = cursor.getParent();
			while (parent != null && !(parent.getValue() instanceof org.openrewrite.Tree)) {
				parent = parent.getParent();
			}
			if (parent != null && parent.getValue() instanceof J.FieldAccess fa) {
				return fa.getName() == cursor.getValue();
			}
			return false;
		}
	}
}
