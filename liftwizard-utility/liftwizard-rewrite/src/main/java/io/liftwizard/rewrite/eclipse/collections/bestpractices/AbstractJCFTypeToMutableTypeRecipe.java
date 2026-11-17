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

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.RemoveUnusedImports;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Cannot be reimplemented as Refaster templates because:
 * 1. Refaster only matches expression patterns, not type declarations
 * 2. This recipe analyzes the type of initializers and conditionally replaces variable type expressions
 * 3. Refaster cannot perform type compatibility analysis (checking if initializer type is assignable to target type)
 * 4. Refaster cannot selectively replace just the type portion of a variable declaration
 *
 * This transformation requires semantic analysis of the type system via JavaIsoVisitor,
 * not structural pattern matching.
 */
public abstract class AbstractJCFTypeToMutableTypeRecipe
	extends Recipe
{
	private final String jcfInterface;
	private final String ecPackage;
	private final String ecInterface;

	protected AbstractJCFTypeToMutableTypeRecipe(String jcfInterface, String ecPackage, String ecInterface)
	{
		this.jcfInterface = Objects.requireNonNull(jcfInterface);
		this.ecPackage = Objects.requireNonNull(ecPackage);
		this.ecInterface = Objects.requireNonNull(ecInterface);
	}

	@Override
	public final String getDisplayName()
	{
		String typeParams = this.jcfInterface.equals("Map") ? "<K, V>" : "<T>";
		return '`' + this.jcfInterface + typeParams + "` → `" + this.ecInterface + typeParams + '`';
	}

	@Override
	public final String getDescription()
	{
		String typeParams = this.jcfInterface.equals("Map") ? "<K, V>" : "<T>";
		return MessageFormat.format(
			"Replace `java.util.{0}{1}` with `org.eclipse.collections.api.{2}.{3}{1}` when the variable is initialized with a {3}, unless the variable is passed to a method with an overload that would accept the {3} but not the `{0}`, such as AssertJ `assertThat`.",
			this.jcfInterface,
			typeParams,
			this.ecPackage,
			this.ecInterface
		);
	}

	@Override
	public final Set<String> getTags()
	{
		return Sets.fixedSize.with("eclipse-collections");
	}

	@Override
	public final Duration getEstimatedEffortPerOccurrence()
	{
		return Duration.ofSeconds(10);
	}

	@Override
	public final TreeVisitor<?, ExecutionContext> getVisitor()
	{
		return new JCFTypeToMutableTypeVisitor(this.jcfInterface, this.ecPackage, this.ecInterface);
	}

	private static final class JCFTypeToMutableTypeVisitor
		extends JavaIsoVisitor<ExecutionContext>
	{
		private final String jcfInterface;
		private final String ecPackage;
		private final String ecInterface;

		private JCFTypeToMutableTypeVisitor(String jcfInterface, String ecPackage, String ecInterface)
		{
			this.jcfInterface = Objects.requireNonNull(jcfInterface);
			this.ecPackage = Objects.requireNonNull(ecPackage);
			this.ecInterface = Objects.requireNonNull(ecInterface);
		}

		@Override
		public J.VariableDeclarations visitVariableDeclarations(
			J.VariableDeclarations multiVariable,
			ExecutionContext ctx
		)
		{
			boolean isField = multiVariable
				.getVariables()
				.stream()
				.anyMatch((namedVariable) -> namedVariable.isField(this.getCursor()));
			boolean isFinal = multiVariable.hasModifier(J.Modifier.Type.Final);

			J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

			if (vd.getTypeExpression() == null || !this.isJavaUtilType(vd.getTypeExpression()))
			{
				return vd;
			}

			if (isField && !isFinal)
			{
				return vd;
			}

			String fullyQualifiedName = this.ecPackage + '.' + this.ecInterface;
			boolean shouldTransform = vd
				.getVariables()
				.stream()
				.filter((variable) -> variable.getInitializer() != null)
				.map((variable) -> variable.getInitializer().getType())
				.filter(Objects::nonNull)
				.anyMatch((initializerType) -> TypeUtils.isAssignableTo(fullyQualifiedName, initializerType));

			if (!shouldTransform)
			{
				return vd;
			}

			JavaType.FullyQualified ecType = vd
				.getVariables()
				.stream()
				.map(J.VariableDeclarations.NamedVariable::getInitializer)
				.filter(Objects::nonNull)
				.map((initializer) -> findSupertype(initializer.getType(), fullyQualifiedName))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow();
			if (this.isPassedToNewlyApplicableOverload(vd, vd.getTypeExpression().getType(), ecType))
			{
				return vd;
			}

			TypeTree typeExpr = vd.getTypeExpression();
			TypeTree newTypeExpr = this.getNewTypeExpr(typeExpr, fullyQualifiedName);

			if (newTypeExpr == null)
			{
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			this.maybeAddImport(fullyQualifiedName);
			this.doAfterVisit(new RemoveUnusedImports().getVisitor());
			return vd.withTypeExpression(newTypeExpr);
		}

		/**
		 * Changing the declared type to the Eclipse Collections interface also changes overload resolution wherever the
		 * variable is passed as an argument. MutableMap is both a Map and an Iterable, and MutableSortedSet is both a
		 * SortedSet and a Comparable, so AssertJ's assertThat(Map) and assertThat(Iterable) both apply and the call no
		 * longer compiles. Skip the declaration when any argument use has an overload that accepts the new type but not
		 * the old one.
		 */
		private boolean isPassedToNewlyApplicableOverload(
			J.VariableDeclarations vd,
			JavaType oldType,
			JavaType.FullyQualified newType
		)
		{
			Set<JavaType.Variable> variableTypes = vd
				.getVariables()
				.stream()
				.map(J.VariableDeclarations.NamedVariable::getVariableType)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			J.CompilationUnit compilationUnit = this.getCursor().firstEnclosing(J.CompilationUnit.class);
			if (variableTypes.isEmpty() || compilationUnit == null)
			{
				return false;
			}

			AtomicBoolean found = new AtomicBoolean();
			new JavaIsoVisitor<AtomicBoolean>()
			{
				@Override
				public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, AtomicBoolean result)
				{
					if (this.passesVariableToNewOverload(method.getMethodType(), method.getArguments()))
					{
						result.set(true);
						return method;
					}
					return super.visitMethodInvocation(method, result);
				}

				@Override
				public J.NewClass visitNewClass(J.NewClass newClass, AtomicBoolean result)
				{
					if (this.passesVariableToNewOverload(newClass.getMethodType(), newClass.getArguments()))
					{
						result.set(true);
						return newClass;
					}
					return super.visitNewClass(newClass, result);
				}

				private boolean passesVariableToNewOverload(JavaType.Method methodType, List<Expression> arguments)
				{
					if (methodType == null)
					{
						return false;
					}
					for (int index = 0; index < arguments.size(); index++)
					{
						if (
							refersTo(arguments.get(index), variableTypes) &&
							hasNewlyApplicableOverload(methodType, arguments.size(), index, oldType, newType)
						)
						{
							return true;
						}
					}
					return false;
				}
			}.visit(compilationUnit, found);
			return found.get();
		}

		private static boolean refersTo(Expression argument, Set<JavaType.Variable> variableTypes)
		{
			Expression unwrapped = argument.unwrap();
			if (unwrapped instanceof J.FieldAccess fieldAccess)
			{
				unwrapped = fieldAccess.getName();
			}
			return unwrapped instanceof J.Identifier identifier && variableTypes.contains(identifier.getFieldType());
		}

		private static boolean hasNewlyApplicableOverload(
			JavaType.Method methodType,
			int argumentCount,
			int argumentIndex,
			JavaType oldType,
			JavaType.FullyQualified newType
		)
		{
			Set<JavaType.Method> overloads = Sets.mutable.empty();
			collectMethods(methodType.getDeclaringType(), methodType.getName(), overloads, Sets.mutable.empty());
			return overloads
				.stream()
				.filter((overload) -> overload.getParameterTypes().size() == argumentCount)
				.map((overload) -> erasedParameterName(overload.getParameterTypes().get(argumentIndex)))
				.filter(Objects::nonNull)
				.anyMatch(
					(parameterName) ->
						TypeUtils.isAssignableTo(parameterName, newType) &&
						!TypeUtils.isAssignableTo(parameterName, oldType)
				);
		}

		private static void collectMethods(
			JavaType.FullyQualified type,
			String methodName,
			Set<JavaType.Method> result,
			Set<String> visited
		)
		{
			if (type == null || !visited.add(type.getFullyQualifiedName()))
			{
				return;
			}
			type.getMethods()
				.stream()
				.filter((method) -> method.getName().equals(methodName))
				.forEach(result::add);
			collectMethods(type.getSupertype(), methodName, result, visited);
			for (JavaType.FullyQualified anInterface : type.getInterfaces())
			{
				collectMethods(anInterface, methodName, result, visited);
			}
		}

		private static String erasedParameterName(JavaType parameterType)
		{
			if (parameterType instanceof JavaType.GenericTypeVariable typeVariable)
			{
				return typeVariable.getBounds().isEmpty() ? null : erasedParameterName(typeVariable.getBounds().get(0));
			}
			JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(parameterType);
			return fullyQualified == null ? null : fullyQualified.getFullyQualifiedName();
		}

		private static JavaType.FullyQualified findSupertype(JavaType type, String fullyQualifiedName)
		{
			JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type);
			if (fullyQualified == null)
			{
				return null;
			}
			if (fullyQualified.getFullyQualifiedName().equals(fullyQualifiedName))
			{
				return fullyQualified;
			}
			JavaType.FullyQualified fromSupertype = findSupertype(fullyQualified.getSupertype(), fullyQualifiedName);
			if (fromSupertype != null)
			{
				return fromSupertype;
			}
			for (JavaType.FullyQualified anInterface : fullyQualified.getInterfaces())
			{
				JavaType.FullyQualified fromInterface = findSupertype(anInterface, fullyQualifiedName);
				if (fromInterface != null)
				{
					return fromInterface;
				}
			}
			return null;
		}

		private TypeTree getNewTypeExpr(TypeTree typeExpr, String fullyQualifiedName)
		{
			if (typeExpr instanceof J.Identifier)
			{
				return ((J.Identifier) typeExpr)
					.withSimpleName(this.ecInterface)
					.withType(JavaType.buildType(fullyQualifiedName));
			}

			if (typeExpr instanceof J.FieldAccess)
			{
				return new J.Identifier(
					Tree.randomId(),
					typeExpr.getPrefix(),
					typeExpr.getMarkers(),
					Lists.fixedSize.empty(),
					this.ecInterface,
					JavaType.buildType(fullyQualifiedName),
					null
				);
			}

			if (!(typeExpr instanceof J.ParameterizedType paramType))
			{
				throw new AssertionError("Unexpected type expression: " + typeExpr.getClass().getSimpleName());
			}

			J clazz = paramType.getClazz();

			if (clazz instanceof J.Identifier)
			{
				J.Identifier newClazz = ((J.Identifier) clazz)
					.withSimpleName(this.ecInterface)
					.withType(JavaType.buildType(fullyQualifiedName));
				return paramType.withClazz(newClazz);
			}

			if (clazz instanceof J.FieldAccess)
			{
				J.Identifier mutableTypeIdent = new J.Identifier(
					Tree.randomId(),
					clazz.getPrefix(),
					clazz.getMarkers(),
					Lists.fixedSize.empty(),
					this.ecInterface,
					JavaType.buildType(fullyQualifiedName),
					null
				);
				return paramType.withClazz(mutableTypeIdent);
			}

			throw new AssertionError("Unexpected parameterized type class: " + clazz.getClass().getSimpleName());
		}

		private boolean isJavaUtilType(J typeExpression)
		{
			J currentExpression = typeExpression;
			while (true)
			{
				String javaUtilTypeName = this.jcfInterface;
				if (currentExpression instanceof J.Identifier identifier)
				{
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
					return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
				}
				if (currentExpression instanceof J.ParameterizedType paramType)
				{
					currentExpression = paramType.getClazz();
					continue;
				}
				if (currentExpression instanceof J.FieldAccess fieldAccess)
				{
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
					return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
				}

				return false;
			}
		}
	}
}
