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

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JLeftPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class ExplicitThis extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add explicit 'this.' prefix to field and method access";
    }

    @Override
    public String getDescription() {
        return "Add explicit 'this.' prefix to field and method access.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ExplicitThisVisitor();
    }

    private static final class ExplicitThisVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext executionContext) {
            J.Assignment a = super.visitAssignment(assignment, executionContext);

            // Handle the left side (variable being assigned)
            Expression variable = a.getVariable();
            if (variable instanceof J.Identifier) {
                J.Identifier identifier = (J.Identifier) variable;
                if (this.shouldQualifyWithThis(identifier)) {
                    J.FieldAccess fieldAccess = this.createFieldAccess(identifier);
                    if (fieldAccess != null) {
                        a = a.withVariable(fieldAccess);
                    }
                }
            }

            // Handle the right side (value being assigned)
            Expression assignmentExpr = a.getAssignment();
            Expression qualifiedAssignment = this.maybeQualifyExpression(assignmentExpr);
            if (qualifiedAssignment != assignmentExpr) {
                a = a.withAssignment(qualifiedAssignment);
            }

            return a;
        }

        @Override
        public J.Binary visitBinary(J.Binary binary, ExecutionContext executionContext) {
            J.Binary b = super.visitBinary(binary, executionContext);

            Expression left = this.maybeQualifyExpression(b.getLeft());
            Expression right = this.maybeQualifyExpression(b.getRight());

            if (left != b.getLeft() || right != b.getRight()) {
                b = b.withLeft(left).withRight(right);
            }

            return b;
        }

        @Override
        public J.Return visitReturn(J.Return returnStatement, ExecutionContext executionContext) {
            J.Return r = super.visitReturn(returnStatement, executionContext);
            if (r.getExpression() != null) {
                Expression qualified = this.maybeQualifyExpression(r.getExpression());
                if (qualified != r.getExpression()) {
                    r = r.withExpression(qualified);
                }
            }
            return r;
        }

        private Expression maybeQualifyExpression(Expression expression) {
            if (expression instanceof J.Identifier) {
                J.Identifier identifier = (J.Identifier) expression;
                if (this.shouldQualifyWithThis(identifier)) {
                    J.FieldAccess fieldAccess = this.createFieldAccess(identifier);
                    if (fieldAccess != null) {
                        return fieldAccess;
                    }
                }
            }
            return expression;
        }

        private boolean shouldQualifyWithThis(J.Identifier identifier) {
            // Don't qualify "this" or "super" themselves
            String name = identifier.getSimpleName();
            if ("this".equals(name) || "super".equals(name)) {
                return false;
            }

            // Check if we're in a static context
            if (this.isInStaticContext()) {
                return false;
            }

            // Check if this identifier is already part of a field access (already qualified)
            if (this.isAlreadyQualified()) {
                return false;
            }

            // Don't qualify variable declarations (e.g., "private String field;")
            if (this.isVariableDeclaration()) {
                return false;
            }

            // Check if this is a field access
            return this.isInstanceFieldAccess(identifier);
        }

        private boolean isAlreadyQualified() {
            // Check if the parent is a FieldAccess and we are its name
            Cursor parent = this.getCursor().getParent();
            if (parent != null && parent.getValue() instanceof J.FieldAccess) {
                J.FieldAccess fieldAccess = (J.FieldAccess) parent.getValue();
                // We are the name part of a field access, so it's already qualified
                Object current = this.getCursor().getValue();
                if (fieldAccess.getName() == current || fieldAccess.getTarget() == current) {
                    return true;
                }
            }
            return false;
        }

        private boolean isVariableDeclaration() {
            // Check if we're in a variable declaration context
            // Go up the tree to see if we're part of a NamedVariable
            Cursor currentCursor = this.getCursor();
            while (currentCursor != null) {
                if (currentCursor.getValue() instanceof J.VariableDeclarations.NamedVariable) {
                    return true;
                }
                currentCursor = currentCursor.getParent();
            }
            return false;
        }

        private J.FieldAccess createFieldAccess(J.Identifier identifier) {
            Object classValue = this.getCursor().dropParentUntil(J.ClassDeclaration.class::isInstance).getValue();
            if (classValue instanceof J.ClassDeclaration) {
                J.ClassDeclaration classDecl = (J.ClassDeclaration) classValue;
                JavaType.FullyQualified classType = classDecl.getType();

                J.Identifier thisIdentifier = new J.Identifier(
                    Tree.randomId(),
                    Space.EMPTY,
                    Markers.EMPTY,
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
            return null;
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            J.MethodInvocation m = super.visitMethodInvocation(method, executionContext);

            // Add "this." to method calls without a select that are instance methods
            if (
                m.getSelect() == null &&
                m.getMethodType() != null &&
                !this.isInStaticContext() &&
                this.isInstanceMethod(m)
            ) {
                // Find the enclosing class to get its type for "this"
                Object classValue = this.getCursor().dropParentUntil(J.ClassDeclaration.class::isInstance).getValue();
                if (classValue instanceof J.ClassDeclaration) {
                    J.ClassDeclaration classDecl = (J.ClassDeclaration) classValue;
                    JavaType.FullyQualified classType = classDecl.getType();

                    return m.withSelect(
                        new J.Identifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "this", classType, null)
                    );
                }
            }

            return m;
        }

        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext executionContext) {
            J.FieldAccess f = super.visitFieldAccess(fieldAccess, executionContext);

            // If field access already has "this" or "super", leave it alone
            if (f.getTarget() instanceof J.Identifier) {
                J.Identifier target = (J.Identifier) f.getTarget();
                if ("this".equals(target.getSimpleName()) || "super".equals(target.getSimpleName())) {
                    return f;
                }
            }

            return f;
        }

        private boolean isInstanceFieldAccess(J.Identifier identifier) {
            // For simplicity, check if this looks like a field by name pattern
            // Real implementation would use proper type information
            String name = identifier.getSimpleName();
            return name != null && !name.equals("this") && !name.equals("super") && identifier.getFieldType() != null;
        }

        private boolean isInstanceMethod(J.MethodInvocation method) {
            if (method.getMethodType() != null) {
                JavaType.Method methodType = method.getMethodType();

                // Check if the method is static by looking at its flags
                // OpenRewrite 8.x provides flag information on JavaType.Method
                Long flags = methodType.getFlagsBitMap();
                if (flags != null) {
                    // Static flag is typically bit 3 (0x0008) in Java's modifier flags
                    // See java.lang.reflect.Modifier.STATIC
                    return (flags & 0x0008L) == 0;
                }

                // Fallback to heuristic approach if flags are not available
                String methodName = method.getSimpleName();

                // Check if this is calling a static method from the same class
                Object classValue = this.getCursor().dropParentUntil(J.ClassDeclaration.class::isInstance).getValue();
                if (classValue instanceof J.ClassDeclaration) {
                    J.ClassDeclaration classDecl = (J.ClassDeclaration) classValue;
                    return !this.isStaticMethodInClass(methodName, classDecl);
                }
            }
            return true;
        }

        private boolean isStaticMethodInClass(String methodName, J.ClassDeclaration classDecl) {
            return classDecl
                .getBody()
                .getStatements()
                .stream()
                .filter(stmt -> stmt instanceof J.MethodDeclaration)
                .map(stmt -> (J.MethodDeclaration) stmt)
                .filter(methodDecl -> methodName.equals(methodDecl.getSimpleName()))
                .anyMatch(
                    methodDecl ->
                        methodDecl.getModifiers().stream().anyMatch(mod -> mod.getType() == J.Modifier.Type.Static)
                );
        }

        private boolean isInStaticContext() {
            // Check if we're in a static method
            try {
                Object methodValue = this.getCursor().dropParentUntil(J.MethodDeclaration.class::isInstance).getValue();
                if (methodValue instanceof J.MethodDeclaration) {
                    J.MethodDeclaration method = (J.MethodDeclaration) methodValue;
                    return method.getModifiers().stream().anyMatch(mod -> mod.getType() == J.Modifier.Type.Static);
                }
            } catch (IllegalStateException e) {
                // No method declaration found - might be in a static block or class initializer
                // Check if we're in a static block by looking for a Block with statik=true
                try {
                    Object blockValue = this.getCursor().dropParentUntil(J.Block.class::isInstance).getValue();
                    if (blockValue instanceof J.Block) {
                        J.Block block = (J.Block) blockValue;
                        return block.isStatic();
                    }
                } catch (IllegalStateException blockException) {
                    // No static context found
                }
            }

            return false;
        }
    }
}
