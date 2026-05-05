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

package io.liftwizard.rewrite.dropwizard.migration;

import java.util.Collections;
import java.util.List;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

public class Dropwizard3LoadRoleInfoSignature extends Recipe {

	private static final String USER_PRINCIPAL = "org.eclipse.jetty.security.UserPrincipal";
	private static final String ROLE_PRINCIPAL = "org.eclipse.jetty.security.RolePrincipal";
	private static final String LIST = "java.util.List";
	private static final String ABSTRACT_LOGIN_SERVICE = "org.eclipse.jetty.security.AbstractLoginService";
	private static final String JETTY_SECURITY_STUB = """
		package org.eclipse.jetty.security;

		public class UserPrincipal {
		    public UserPrincipal(String name, Object credential) {}
		    public String getName() { return null; }
		}
		""";
	private static final String ROLE_PRINCIPAL_STUB = """
		package org.eclipse.jetty.security;

		public class RolePrincipal {
		    public RolePrincipal(String name) {}
		}
		""";

	@Override
	public String getDisplayName() {
		return "Migrate AbstractLoginService.loadRoleInfo override to List<RolePrincipal>";
	}

	@Override
	public String getDescription() {
		return (
			"In Jetty 10, AbstractLoginService.loadRoleInfo's return type changed from "
			+ "String[] to List<RolePrincipal>. This recipe rewrites overrides accordingly: "
			+ "the method's return type is updated, returns of `new String[] { \"x\", ... }` "
			+ "become `List.of(new RolePrincipal(\"x\"), ...)`, and returns of `new String[0]` "
			+ "become `List.of()`."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesType<>(ABSTRACT_LOGIN_SERVICE, false), new LoadRoleInfoVisitor());
	}

	private static boolean isLoadRoleInfoOverride(J.MethodDeclaration method) {
		if (!"loadRoleInfo".equals(method.getSimpleName())) {
			return false;
		}
		if (method.getParameters().size() != 1) {
			return false;
		}
		J firstParam = method.getParameters().get(0);
		if (!(firstParam instanceof J.VariableDeclarations vd)) {
			return false;
		}
		if (!isUserPrincipalType(vd.getTypeExpression())) {
			return false;
		}
		J returnTypeExpression = method.getReturnTypeExpression();
		if (!(returnTypeExpression instanceof J.ArrayType arrayType)) {
			return false;
		}
		return TypeUtils.isOfClassType(arrayType.getElementType().getType(), "java.lang.String");
	}

	/**
	 * Match UserPrincipal both when fully resolved (Jetty 10 top-level type or Jetty 9 nested type)
	 * and when only the simple name is available. The bare-name fallback is needed because Jetty 9
	 * source often referenced UserPrincipal via inherited nested-class scope without an explicit
	 * import; under Jetty 10's classpath that reference is unresolved until imports are added.
	 */
	private static boolean isUserPrincipalType(J typeExpression) {
		if (!(typeExpression instanceof TypeTree typeTree)) {
			return false;
		}
		if (TypeUtils.isOfClassType(typeTree.getType(), USER_PRINCIPAL)) {
			return true;
		}
		return typeExpression instanceof J.Identifier id && "UserPrincipal".equals(id.getSimpleName());
	}

	private static final class LoadRoleInfoVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
			J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
			if (!isLoadRoleInfoOverride(md)) {
				return md;
			}

			this.maybeAddImport(LIST, false);
			this.maybeAddImport(ROLE_PRINCIPAL, false);
			this.maybeAddImport(USER_PRINCIPAL, false);

			TypeTree existingReturnType = md.getReturnTypeExpression();
			if (existingReturnType == null) {
				return md;
			}
			TypeTree newReturnType = TypeTree.build("List<RolePrincipal>").withPrefix(existingReturnType.getPrefix());
			return md.withReturnTypeExpression(newReturnType);
		}

		@Override
		public J.Return visitReturn(J.Return retn, ExecutionContext ctx) {
			J.Return processed = super.visitReturn(retn, ctx);
			if (!this.isInsideLoadRoleInfoOverride()) {
				return processed;
			}
			Expression expr = processed.getExpression();
			if (!(expr instanceof J.NewArray newArray) || !isStringArrayCreation(newArray)) {
				return processed;
			}
			return this.rewriteAsListOfRolePrincipal(processed, newArray, ctx);
		}

		private boolean isInsideLoadRoleInfoOverride() {
			Cursor parent = this.getCursor().dropParentUntil(
				(value) -> value instanceof J.MethodDeclaration || value instanceof J.CompilationUnit
			);
			Object parentValue = parent.getValue();
			return parentValue instanceof J.MethodDeclaration enclosing && isLoadRoleInfoOverride(enclosing);
		}

		private J.Return rewriteAsListOfRolePrincipal(J.Return retn, J.NewArray newArray, ExecutionContext ctx) {
			List<Expression> elements = newArray.getInitializer() == null ? List.of() : newArray.getInitializer();

			String placeholders = String.join(
				", ",
				Collections.nCopies(elements.size(), "new RolePrincipal(#{any(java.lang.String)})")
			);
			String code = "return List.of(" + placeholders + ");";

			return JavaTemplate.builder(code)
				.imports(LIST, ROLE_PRINCIPAL)
				.javaParser(JavaParser.fromJavaVersion().dependsOn(ROLE_PRINCIPAL_STUB, JETTY_SECURITY_STUB))
				.build()
				.apply(this.updateCursor(retn), retn.getCoordinates().replace(), elements.toArray());
		}

		private static boolean isStringArrayCreation(J.NewArray newArray) {
			JavaType elementType = newArray.getType();
			if (elementType instanceof JavaType.Array array) {
				return TypeUtils.isOfClassType(array.getElemType(), "java.lang.String");
			}
			return false;
		}
	}
}
