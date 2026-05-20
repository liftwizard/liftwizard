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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

/**
 * Helpers that decide whether a recipe should skip substituting a method reference
 * (or any expression whose static type would change to a method reference) at the
 * cursor position. The checks address three categories of breakage observed when
 * the Eclipse Collections removal recipes ran over the eclipse-collections repo.
 */
final class MethodReferenceContextChecks {

	private MethodReferenceContextChecks() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * True when the expression's use-site expects the concrete implementing class
	 * (e.g. {@code CollectionAddProcedure<T>}) rather than the functional-interface
	 * type (e.g. {@code Procedure<T>}). A method reference cannot be the value of a
	 * binding whose declared type is the concrete class.
	 *
	 * <p>The check looks at the expected type at the cursor's parent (variable
	 * declaration, return statement, method/constructor argument, assignment). If
	 * the expected type's fully-qualified name equals {@code concreteFqn} (or any
	 * other non-functional-interface type) the result is true.
	 */
	static boolean targetIsConcreteClass(Cursor cursor, String concreteFqn, String functionalInterfaceFqn) {
		JavaType expectedType = findExpectedType(cursor);
		if (expectedType == null) {
			return false;
		}
		String expectedFqn = fullyQualifiedName(expectedType);
		if (expectedFqn == null) {
			return false;
		}
		if (expectedFqn.equals(functionalInterfaceFqn)) {
			return false;
		}
		return expectedFqn.equals(concreteFqn);
	}

	/**
	 * True when the use-site expected type is {@code java.lang.Object}. Method
	 * references require a functional-interface target type; assignment to Object
	 * loses that and the compiler rejects the substitution.
	 */
	static boolean targetIsObject(Cursor cursor) {
		JavaType expectedType = findExpectedType(cursor);
		if (expectedType == null) {
			return false;
		}
		return "java.lang.Object".equals(fullyQualifiedName(expectedType));
	}

	/** True when the expression is a {@code null} literal. */
	static boolean isNullLiteral(@Nullable Expression expression) {
		return expression instanceof J.Literal literal && literal.getValue() == null;
	}

	/**
	 * True when the expression sits at a method-invocation argument position whose
	 * method has two or more accessible overloads where this argument position
	 * accepts a functional-interface type with the same arity as the substituted
	 * method reference. In that case the substituted method reference would be
	 * ambiguous and the compiler would reject the rewrite.
	 */
	static boolean wouldBeAmbiguousAtCallSite(Cursor cursor, int methodReferenceArity) {
		Cursor parentCursor = nextTreeCursor(cursor.getParent());
		if (parentCursor == null) {
			return false;
		}
		Object parentValue = parentCursor.getValue();
		if (!(parentValue instanceof J.MethodInvocation parentCall)) {
			return false;
		}

		List<Expression> arguments = parentCall.getArguments();
		int argIndex = arguments.indexOf(cursor.getValue());
		if (argIndex == -1) {
			return false;
		}

		JavaType.Method methodType = parentCall.getMethodType();
		if (methodType == null) {
			return false;
		}
		JavaType.FullyQualified declaringType = methodType.getDeclaringType();

		String methodName = methodType.getName();
		int parameterCount = methodType.getParameterTypes().size();

		int matchingOverloads = 0;
		for (JavaType.Method candidate : allMethodsIncludingSupertypes(declaringType)) {
			if (!methodName.equals(candidate.getName())) {
				continue;
			}
			List<JavaType> candidateParams = candidate.getParameterTypes();
			if (candidateParams.size() != parameterCount) {
				continue;
			}
			if (argIndex >= candidateParams.size()) {
				continue;
			}
			JavaType candidateParamType = candidateParams.get(argIndex);
			if (couldBindMethodReference(candidateParamType, methodReferenceArity)) {
				matchingOverloads++;
				if (matchingOverloads >= 2) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean couldBindMethodReference(@Nullable JavaType paramType, int methodReferenceArity) {
		JavaType raw = paramType;
		if (raw instanceof JavaType.Parameterized parameterized) {
			raw = parameterized.getType();
		}
		if (raw instanceof JavaType.GenericTypeVariable) {
			return true;
		}
		if (!(raw instanceof JavaType.FullyQualified fq)) {
			return false;
		}
		int arity = functionalInterfaceArity(fq);
		if (arity == methodReferenceArity) {
			return true;
		}
		// Could not determine arity (e.g. type details are not on the parser
		// classpath). Be conservative and treat any non-Object reference type
		// as a possible target — better to skip a rewrite than to introduce
		// ambiguous code at the call site.
		if (arity == -1) {
			String fqn = fq.getFullyQualifiedName();
			return !"java.lang.Object".equals(fqn) && !"java.lang.String".equals(fqn);
		}
		return false;
	}

	private static Iterable<JavaType.Method> allMethodsIncludingSupertypes(JavaType.FullyQualified start) {
		return () ->
			new Iterator<JavaType.Method>() {
				private final Deque<JavaType.FullyQualified> toVisit = new ArrayDeque<>(List.of(start));
				private final Set<String> visited = new HashSet<>();
				private Iterator<JavaType.Method> current = Collections.emptyIterator();

				@Override
				public boolean hasNext() {
					while (!current.hasNext() && !toVisit.isEmpty()) {
						JavaType.FullyQualified next = toVisit.poll();
						if (next == null || !visited.add(next.getFullyQualifiedName())) {
							continue;
						}
						current = next.getMethods().iterator();
						JavaType.FullyQualified supertype = next.getSupertype();
						if (supertype != null) {
							toVisit.add(supertype);
						}
						for (JavaType.FullyQualified iface : next.getInterfaces()) {
							toVisit.add(iface);
						}
					}
					return current.hasNext();
				}

				@Override
				public JavaType.Method next() {
					return current.next();
				}
			};
	}

	/**
	 * If {@code type} is a functional interface (exactly one abstract method),
	 * returns that method's parameter count. Otherwise returns -1.
	 *
	 * <p>An interface method is treated as abstract when it has neither the
	 * {@code default} nor {@code static} modifier — covering the case where the
	 * parsed JavaType does not carry an explicit {@code Abstract} flag.
	 */
	private static int functionalInterfaceArity(@Nullable JavaType type) {
		if (!(type instanceof JavaType.FullyQualified fq)) {
			if (type instanceof JavaType.Parameterized parameterized) {
				return functionalInterfaceArity(parameterized.getType());
			}
			return -1;
		}
		boolean isInterface = fq instanceof JavaType.Class clazz && clazz.getKind() == JavaType.Class.Kind.Interface;
		Set<String> overriddenSignatures = new HashSet<>();
		Map<String, JavaType.Method> abstractMethods = new LinkedHashMap<>();
		for (JavaType.Method m : allMethodsIncludingSupertypes(fq)) {
			if (m.getName().equals("<constructor>")) {
				continue;
			}
			if (m.hasFlags(Flag.Static)) {
				continue;
			}
			if (isObjectMethod(m)) {
				continue;
			}
			String signature = methodSignature(m);
			if (m.hasFlags(Flag.Default)) {
				overriddenSignatures.add(signature);
				continue;
			}
			boolean abstractMethod = m.hasFlags(Flag.Abstract) || isInterface;
			if (!abstractMethod) {
				continue;
			}
			abstractMethods.putIfAbsent(signature, m);
		}
		abstractMethods.keySet().removeAll(overriddenSignatures);
		if (abstractMethods.size() != 1) {
			return -1;
		}
		return abstractMethods.values().iterator().next().getParameterTypes().size();
	}

	private static String methodSignature(JavaType.Method method) {
		StringBuilder sb = new StringBuilder(method.getName()).append('(');
		boolean first = true;
		for (JavaType paramType : method.getParameterTypes()) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			String fqn = fullyQualifiedName(paramType);
			sb.append(fqn == null ? paramType.toString() : fqn);
		}
		sb.append(')');
		return sb.toString();
	}

	private static boolean isObjectMethod(JavaType.Method method) {
		JavaType.FullyQualified declaring = method.getDeclaringType();
		return "java.lang.Object".equals(declaring.getFullyQualifiedName());
	}

	/**
	 * Walks up the cursor to find the type expected at the position of the cursor's
	 * value. Handles variable declarations, return statements, assignments,
	 * method-invocation arguments, new-class arguments and type casts. Skips
	 * non-tree cursor entries such as {@code JLeftPadded}, {@code JRightPadded},
	 * and {@code JContainer}.
	 */
	private static @Nullable JavaType findExpectedType(Cursor cursor) {
		Object value = cursor.getValue();
		Cursor parentCursor = nextTreeCursor(cursor.getParent());
		while (parentCursor != null) {
			Object parent = parentCursor.getValue();
			if (
				parent instanceof J.VariableDeclarations.NamedVariable namedVariable
				&& namedVariable.getInitializer() == value
			) {
				return namedVariable.getType();
			}
			if (parent instanceof J.Assignment assignment && assignment.getAssignment() == value) {
				return assignment.getType();
			}
			if (parent instanceof J.Return retStatement && retStatement.getExpression() == value) {
				return findEnclosingReturnType(parentCursor);
			}
			if (parent instanceof J.TypeCast typeCast && typeCast.getExpression() == value) {
				return typeCast.getType();
			}
			if (parent instanceof J.MethodInvocation call && call.getArguments().contains(value)) {
				return parameterTypeAt(call.getMethodType(), call.getArguments(), value);
			}
			if (
				parent instanceof J.NewClass newClass
				&& newClass.getArguments() != null
				&& newClass.getArguments().contains(value)
			) {
				return parameterTypeAt(newClass.getConstructorType(), newClass.getArguments(), value);
			}
			if (parent instanceof J.Parentheses<?> parens && parens.getTree() == value) {
				value = parent;
				parentCursor = nextTreeCursor(parentCursor.getParent());
				continue;
			}
			if (parent instanceof J.ControlParentheses<?> parens && parens.getTree() == value) {
				value = parent;
				parentCursor = nextTreeCursor(parentCursor.getParent());
				continue;
			}
			return null;
		}
		return null;
	}

	private static @Nullable Cursor nextTreeCursor(@Nullable Cursor cursor) {
		Cursor current = cursor;
		while (current != null && !(current.getValue() instanceof Tree)) {
			current = current.getParent();
		}
		return current;
	}

	private static @Nullable JavaType parameterTypeAt(
		JavaType.@Nullable Method methodType,
		@Nullable List<Expression> arguments,
		Object target
	) {
		if (methodType == null || arguments == null) {
			return null;
		}
		List<JavaType> paramTypes = methodType.getParameterTypes();
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i) == target) {
				if (methodType.hasFlags(Flag.Varargs) && i >= paramTypes.size() - 1 && !paramTypes.isEmpty()) {
					JavaType last = paramTypes.get(paramTypes.size() - 1);
					if (last instanceof JavaType.Array array) {
						return array.getElemType();
					}
					return last;
				}
				if (i < paramTypes.size()) {
					return paramTypes.get(i);
				}
				return null;
			}
		}
		return null;
	}

	private static @Nullable JavaType findEnclosingReturnType(Cursor returnCursor) {
		Cursor parent = returnCursor.getParent();
		while (parent != null) {
			Object value = parent.getValue();
			if (value instanceof J.MethodDeclaration methodDeclaration) {
				JavaType.Method methodType = methodDeclaration.getMethodType();
				return methodType == null ? null : methodType.getReturnType();
			}
			if (value instanceof J.Lambda lambda) {
				return lambda.getType();
			}
			parent = parent.getParent();
		}
		return null;
	}

	private static @Nullable String fullyQualifiedName(@Nullable JavaType type) {
		if (type instanceof JavaType.Parameterized parameterized) {
			return fullyQualifiedName(parameterized.getType());
		}
		if (type instanceof JavaType.FullyQualified fq) {
			return fq.getFullyQualifiedName();
		}
		return null;
	}
}
