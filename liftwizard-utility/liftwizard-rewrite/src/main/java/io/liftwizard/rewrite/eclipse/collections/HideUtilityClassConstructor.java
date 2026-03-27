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

package io.liftwizard.rewrite.eclipse.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.ChangeMethodAccessLevelVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.service.AnnotationService;
import org.openrewrite.java.style.Checkstyle;
import org.openrewrite.java.style.HideUtilityClassConstructorStyle;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.style.Style;

/**
 * Fork of {@code org.openrewrite.staticanalysis.HideUtilityClassConstructor} that
 * generates a private constructor whose body throws {@link AssertionError} to
 * enforce noninstantiability, matching the Liftwizard codebase convention.
 *
 * <p>The generated constructor body is:
 * <pre>{@code
 * throw new AssertionError("Suppress default constructor for noninstantiability");
 * }</pre>
 */
public class HideUtilityClassConstructor extends Recipe {

	@Override
	public String getDisplayName() {
		return "Hide utility class constructor with AssertionError";
	}

	@Override
	public String getDescription() {
		return (
			"Ensures utility classes (classes containing only static methods or fields in their API) "
			+ "do not have a public constructor. Adds a private constructor that throws AssertionError "
			+ "to enforce noninstantiability."
		);
	}

	@Override
	public Set<String> getTags() {
		return Collections.singleton("RSPEC-S1118");
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new HideUtilityClassConstructorVisitor();
	}

	private static final class HideUtilityClassConstructorVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final Set<J.ClassDeclaration.Kind.Type> EXCLUDE_CLASS_TYPES = EnumSet.of(
			J.ClassDeclaration.Kind.Type.Interface,
			J.ClassDeclaration.Kind.Type.Record
		);

		@SuppressWarnings("NotNullFieldNotInitialized")
		private UtilityClassMatcher utilityClassMatcher;

		@SuppressWarnings("NotNullFieldNotInitialized")
		private HideUtilityClassConstructorStyle style;

		@Override
		public @Nullable J visit(@Nullable Tree tree, ExecutionContext ctx) {
			if (style == null && tree instanceof SourceFile) {
				style = Style.from(
					HideUtilityClassConstructorStyle.class,
					(SourceFile) tree,
					Checkstyle::hideUtilityClassConstructorStyle
				);
				utilityClassMatcher = new UtilityClassMatcher(style.getIgnoreIfAnnotatedBy());
			}
			return super.visit(tree, ctx);
		}

		@Override
		@SuppressWarnings("ConstantConditions")
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
			J.ClassDeclaration c = super.visitClassDeclaration(classDecl, ctx);
			if (
				!EXCLUDE_CLASS_TYPES.contains(c.getKind())
				&& !c.hasModifier(J.Modifier.Type.Abstract)
				&& utilityClassMatcher.isRefactorableUtilityClass(getCursor())
			) {
				c = (J.ClassDeclaration) new AddPrivateConstructorVisitor().visit(
					c,
					ctx,
					getCursor().getParentOrThrow()
				);
				c = (J.ClassDeclaration) new ChangeExposedConstructorVisitor(c).visit(
					c,
					ctx,
					getCursor().getParentOrThrow()
				);
				c = (J.ClassDeclaration) new AddAssertionErrorToConstructorVisitor(c).visit(
					c,
					ctx,
					getCursor().getParentOrThrow()
				);
			}
			return c;
		}

		/**
		 * Adds a private constructor with AssertionError body when the class
		 * has no explicit constructors (implicit default constructor).
		 */
		private final class AddPrivateConstructorVisitor extends JavaIsoVisitor<ExecutionContext> {

			@Override
			public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
				if (
					utilityClassMatcher.hasImplicitDefaultConstructor(classDecl)
					&& J.ClassDeclaration.Kind.Type.Enum != classDecl.getKind()
				) {
					return JavaTemplate.builder(
						"private #{}() {\n"
						+ "    throw new AssertionError(\"Suppress default constructor for noninstantiability\");\n"
						+ "}"
					)
						.contextSensitive()
						.build()
						.apply(
							getCursor(),
							classDecl.getBody().getCoordinates().lastStatement(),
							classDecl.getSimpleName()
						);
				}
				return classDecl;
			}
		}

		/**
		 * Changes public/package-private constructors to private.
		 */
		private static final class ChangeExposedConstructorVisitor extends JavaIsoVisitor<ExecutionContext> {

			private final J.ClassDeclaration utilityClass;

			private ChangeExposedConstructorVisitor(J.ClassDeclaration utilityClass) {
				this.utilityClass = utilityClass;
			}

			@Override
			public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
				return classDecl == utilityClass ? super.visitClassDeclaration(classDecl, ctx) : classDecl;
			}

			@Override
			public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
				J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
				if (
					md.getMethodType() == null
					|| !md.isConstructor()
					|| md.hasModifier(J.Modifier.Type.Private)
					|| md.hasModifier(J.Modifier.Type.Protected)
					|| md.getMethodType().getDeclaringType().getKind() == JavaType.Class.Kind.Enum
				) {
					return md;
				}

				ChangeMethodAccessLevelVisitor<ExecutionContext> changeAccess = new ChangeMethodAccessLevelVisitor<>(
					new MethodMatcher(method),
					J.Modifier.Type.Private
				);
				md = (J.MethodDeclaration) changeAccess.visit(md, ctx, getCursor().getParentOrThrow());
				return Objects.requireNonNull(md);
			}
		}

		/**
		 * Adds {@code throw new AssertionError(...)} to constructor bodies that
		 * don't already contain it.
		 */
		private static final class AddAssertionErrorToConstructorVisitor extends JavaIsoVisitor<ExecutionContext> {

			private final J.ClassDeclaration utilityClass;

			private AddAssertionErrorToConstructorVisitor(J.ClassDeclaration utilityClass) {
				this.utilityClass = utilityClass;
			}

			@Override
			public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
				return classDecl == utilityClass ? super.visitClassDeclaration(classDecl, ctx) : classDecl;
			}

			@Override
			public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
				J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
				if (!md.isConstructor() || md.getBody() == null || !md.hasModifier(J.Modifier.Type.Private)) {
					return md;
				}
				if (hasAssertionErrorThrow(md.getBody())) {
					return md;
				}
				return JavaTemplate.builder(
					"throw new AssertionError(\"Suppress default constructor for noninstantiability\");"
				)
					.contextSensitive()
					.build()
					.apply(getCursor(), md.getBody().getCoordinates().lastStatement());
			}

			private static boolean hasAssertionErrorThrow(J.Block body) {
				for (Statement statement : body.getStatements()) {
					if (statement instanceof J.Throw throwStatement) {
						if (throwStatement.getException() instanceof J.NewClass newClass) {
							if (
								newClass.getClazz() != null && newClass.getClazz().toString().equals("AssertionError")
							) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}

		private final class UtilityClassMatcher {

			private final Collection<AnnotationMatcher> ignorableAnnotations;

			private UtilityClassMatcher(Collection<String> ignorableAnnotations) {
				this.ignorableAnnotations = new ArrayList<>(ignorableAnnotations.size());
				for (String ignorableAnnotation : ignorableAnnotations) {
					this.ignorableAnnotations.add(new AnnotationMatcher(ignorableAnnotation));
				}
			}

			boolean hasIgnorableAnnotation(Cursor cursor) {
				AnnotationService service = service(AnnotationService.class);
				for (AnnotationMatcher ignorableAnn : ignorableAnnotations) {
					if (service.matches(cursor, ignorableAnn)) {
						return true;
					}
				}
				return false;
			}

			boolean hasMainMethod(J.ClassDeclaration c) {
				if (c.getType() == null) {
					return false;
				}
				for (Statement statement : c.getBody().getStatements()) {
					if (statement instanceof J.MethodDeclaration md) {
						if (
							!md.isConstructor()
							&& md.hasModifier(J.Modifier.Type.Public)
							&& md.hasModifier(J.Modifier.Type.Static)
							&& md.getReturnTypeExpression() != null
							&& JavaType.Primitive.Void == md.getReturnTypeExpression().getType()
							&& new MethodMatcher(c.getType().getFullyQualifiedName() + " main(String[])").matches(md, c)
						) {
							return true;
						}
					}
				}
				return false;
			}

			boolean hasImplicitDefaultConstructor(J.ClassDeclaration c) {
				for (Statement statement : c.getBody().getStatements()) {
					if (statement instanceof J.MethodDeclaration md) {
						if (md.isConstructor()) {
							return false;
						}
					}
				}
				return true;
			}

			boolean isRefactorableUtilityClass(Cursor cursor) {
				J.ClassDeclaration c = cursor.getValue();
				return isUtilityClass(c) && !hasIgnorableAnnotation(cursor) && !hasMainMethod(c);
			}

			boolean isUtilityClass(J.ClassDeclaration c) {
				if (c.getImplements() != null || c.getExtends() != null) {
					return false;
				}

				int staticMethodCount = countStaticMethods(c);
				if (staticMethodCount < 0) {
					return false;
				}

				int staticFieldCount = countStaticFields(c);
				if (staticFieldCount < 0) {
					return false;
				}

				return staticMethodCount != 0 || staticFieldCount != 0;
			}

			private int countStaticFields(J.ClassDeclaration classDeclaration) {
				int count = 0;
				for (Statement statement : classDeclaration.getBody().getStatements()) {
					if (!(statement instanceof J.VariableDeclarations field)) {
						continue;
					}
					if (!field.hasModifier(J.Modifier.Type.Static)) {
						return -1;
					}
					if (field.hasModifier(J.Modifier.Type.Private)) {
						continue;
					}
					count++;
				}
				return count;
			}

			private int countStaticMethods(J.ClassDeclaration classDeclaration) {
				int count = 0;
				for (Statement statement : classDeclaration.getBody().getStatements()) {
					if (!(statement instanceof J.MethodDeclaration method)) {
						continue;
					}
					if (method.isConstructor()) {
						continue;
					}
					if (!method.hasModifier(J.Modifier.Type.Static)) {
						return -1;
					}
					if (method.hasModifier(J.Modifier.Type.Private)) {
						continue;
					}
					count++;
				}
				return count;
			}
		}
	}
}
