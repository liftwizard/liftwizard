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

package io.liftwizard.rewrite.java.style;

import org.eclipse.collections.api.factory.Lists;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.FieldAccess;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.JLeftPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.Method;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class ExplicitThis extends Recipe {

	@Override
	public String getDisplayName() {
		return "`field` → `this.field`";
	}

	@Override
	public String getDescription() {
		return "Add explicit 'this.' prefix to field and method access.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ExplicitThisVisitor();
	}

	private static final class ExplicitThisVisitor extends JavaVisitor<ExecutionContext> {

		private boolean isStatic;
		private boolean isInsideFieldAccess;

		@Override
		public J visitFieldAccess(FieldAccess fieldAccess, ExecutionContext ctx) {
			boolean previousIsInsideFieldAccess = this.isInsideFieldAccess;
			this.isInsideFieldAccess = true;

			J result = super.visitFieldAccess(fieldAccess, ctx);

			this.isInsideFieldAccess = previousIsInsideFieldAccess;
			return result;
		}

		@Override
		public J visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
			J.Identifier id = (J.Identifier) super.visitIdentifier(identifier, ctx);

			// In static context, no "this." allowed
			if (this.isStatic) {
				return id;
			}

			// Skip if already qualified
			if (this.isInsideFieldAccess) {
				return id;
			}

			JavaType.Variable fieldType = id.getFieldType();
			if (fieldType == null) {
				return id;
			}

			// Check if this is actually a field of the class (not a parameter or local variable)
			if (fieldType.getOwner() == null || !(fieldType.getOwner() instanceof JavaType.Class)) {
				return id;
			}

			// Skip static fields
			// 0x0008 is the static flag in Java
			if ((fieldType.getFlagsBitMap() & 0x0008L) != 0) {
				return id;
			}

			// Skip keywords
			String name = id.getSimpleName();
			if ("this".equals(name) || "super".equals(name)) {
				return id;
			}

			// Skip declarations
			if (this.isPartOfDeclaration()) {
				return id;
			}

			J.FieldAccess fieldAccess = this.createFieldAccess(id);
			return fieldAccess != null ? fieldAccess : id;
		}

		@Override
		public J visitBlock(J.Block block, ExecutionContext ctx) {
			if (!block.isStatic()) {
				return super.visitBlock(block, ctx);
			}

			boolean previousStatic = this.isStatic;
			this.isStatic = true;

			J.Block result = (J.Block) super.visitBlock(block, ctx);

			this.isStatic = previousStatic;
			return result;
		}

		@Override
		public J visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
			boolean previousStatic = this.isStatic;

			// Check if method is static using flag bits (0x0008 is the static flag)
			JavaType.Method methodType = method.getMethodType();
			if (methodType != null) {
				this.isStatic = (methodType.getFlagsBitMap() & 0x0008L) != 0;
			}

			J.MethodDeclaration result = (J.MethodDeclaration) super.visitMethodDeclaration(method, ctx);

			// Restore previous state
			this.isStatic = previousStatic;

			return result;
		}

		@Override
		public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

			// Fast path: in static context, no "this." allowed
			if (this.isStatic) {
				return m;
			}

			// Skip constructor invocations (super() or this())
			if (m.getName().getSimpleName().equals("super") || m.getName().getSimpleName().equals("this")) {
				return m;
			}

			Method methodType = m.getMethodType();
			if (
				m.getSelect() != null
				|| methodType == null
				// Check if method is static using flag bits (0x0008 is the static flag)
				|| (methodType.getFlagsBitMap() & 0x0008L) != 0
			) {
				return m;
			}

			Cursor classDeclarationCursor = this.getCursor().dropParentUntil(
				(p) -> p instanceof J.ClassDeclaration || p == Cursor.ROOT_VALUE
			);
			if (!(classDeclarationCursor.getValue() instanceof J.ClassDeclaration)) {
				return m;
			}

			J.ClassDeclaration classDeclaration = classDeclarationCursor.getValue();
			JavaType.FullyQualified classType = classDeclaration.getType();

			Identifier identifier = new Identifier(
				Tree.randomId(),
				Space.EMPTY,
				Markers.EMPTY,
				Lists.fixedSize.empty(),
				"this",
				classType,
				null
			);
			return m.withSelect(identifier);
		}

		private boolean isPartOfDeclaration() {
			Cursor parent = this.getCursor().getParent();
			if (parent == null || !(parent.getValue() instanceof J.VariableDeclarations.NamedVariable)) {
				return false;
			}
			J.VariableDeclarations.NamedVariable namedVar = (J.VariableDeclarations.NamedVariable) parent.getValue();
			return namedVar.getName() == this.getCursor().getValue();
		}

		private J.FieldAccess createFieldAccess(J.Identifier identifier) {
			Cursor classDeclarationCursor = this.getCursor().dropParentUntil(
				(p) -> p instanceof J.ClassDeclaration || p == Cursor.ROOT_VALUE
			);
			if (!(classDeclarationCursor.getValue() instanceof J.ClassDeclaration)) {
				return null;
			}

			J.ClassDeclaration classDeclaration = classDeclarationCursor.getValue();
			JavaType.FullyQualified classType = classDeclaration.getType();

			J.Identifier thisIdentifier = new J.Identifier(
				Tree.randomId(),
				Space.EMPTY,
				Markers.EMPTY,
				Lists.fixedSize.empty(),
				"this",
				classType,
				null
			);

			return new J.FieldAccess(
				Tree.randomId(),
				identifier.getPrefix(),
				Markers.EMPTY,
				thisIdentifier,
				JLeftPadded.build(identifier.withPrefix(Space.EMPTY)),
				identifier.getFieldType()
			);
		}
	}
}
