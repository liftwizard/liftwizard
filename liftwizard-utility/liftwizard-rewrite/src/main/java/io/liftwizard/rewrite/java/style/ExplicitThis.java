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

import java.util.Collections;

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
        public J visitFieldAccess(FieldAccess fieldAccess, ExecutionContext executionContext) {
            boolean previousIsInsideFieldAccess = this.isInsideFieldAccess;
            this.isInsideFieldAccess = true;

            J result = super.visitFieldAccess(fieldAccess, executionContext);

            this.isInsideFieldAccess = previousIsInsideFieldAccess;
            return result;
        }

        @Override
        public J visitIdentifier(J.Identifier identifier, ExecutionContext executionContext) {
            J.Identifier id = (J.Identifier) super.visitIdentifier(identifier, executionContext);

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

            return this.createFieldAccess(id);
        }

        @Override
        public J visitBlock(J.Block block, ExecutionContext executionContext) {
            if (!block.isStatic()) {
                return super.visitBlock(block, executionContext);
            }

            boolean previousStatic = this.isStatic;
            this.isStatic = true;

            J.Block result = (J.Block) super.visitBlock(block, executionContext);

            this.isStatic = previousStatic;
            return result;
        }

        @Override
        public J visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
            boolean previousStatic = this.isStatic;

            // Check if method is static using flag bits (0x0008 is the static flag)
            JavaType.Method methodType = method.getMethodType();
            if (methodType != null) {
                this.isStatic = (methodType.getFlagsBitMap() & 0x0008L) != 0;
            }

            J.MethodDeclaration result = (J.MethodDeclaration) super.visitMethodDeclaration(method, executionContext);

            // Restore previous state
            this.isStatic = previousStatic;

            return result;
        }

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, executionContext);

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
                m.getSelect() != null ||
                methodType == null ||
                // Check if method is static using flag bits (0x0008 is the static flag)
                (methodType.getFlagsBitMap() & 0x0008L) !=
                0
            ) {
                return m;
            }
            J.ClassDeclaration classDeclaration = this.getCursor()
                .dropParentUntil(J.ClassDeclaration.class::isInstance)
                .getValue();
            JavaType.FullyQualified classType = classDeclaration.getType();

            Identifier identifier = new Identifier(
                Tree.randomId(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
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
            J.ClassDeclaration classDeclaration = this.getCursor()
                .dropParentUntil(J.ClassDeclaration.class::isInstance)
                .getValue();

            JavaType.FullyQualified classType = classDeclaration.getType();

            J.Identifier thisIdentifier = new J.Identifier(
                Tree.randomId(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
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
