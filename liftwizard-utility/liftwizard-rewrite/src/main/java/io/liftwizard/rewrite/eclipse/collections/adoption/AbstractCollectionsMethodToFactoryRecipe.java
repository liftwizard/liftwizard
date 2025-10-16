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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public abstract class AbstractCollectionsMethodToFactoryRecipe extends Recipe {

    private final String collectionType;
    private final String sourceMethodName;
    private final int arity;

    protected AbstractCollectionsMethodToFactoryRecipe(String collectionType, String sourceMethodName, int arity) {
        this.collectionType = Objects.requireNonNull(collectionType);
        this.sourceMethodName = Objects.requireNonNull(sourceMethodName);
        this.arity = arity;
    }

    @Override
    public final String getDisplayName() {
        return "`Collections.%s()` → `%ss.fixedSize.%s()`".formatted(
            this.sourceMethodName,
            this.collectionType,
            this.getTargetMethod()
        );
    }

    @Override
    public final String getDescription() {
        return "Replace `Collections.%s()` with `%ss.fixedSize.%s()`.".formatted(
            this.sourceMethodName,
            this.collectionType,
            this.getTargetMethod()
        );
    }

    private String getTargetMethod() {
        return this.arity == 0 ? "empty" : "of";
    }

    @Override
    public final Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public final Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(10);
    }

    @Override
    public final TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CollectionsMethodVisitor(this.collectionType, this.sourceMethodName, this.arity);
    }

    private static final class CollectionsMethodVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher;
        private final String collectionType;
        private final int arity;

        private CollectionsMethodVisitor(String collectionType, String sourceMethodName, int arity) {
            this.methodMatcher = new MethodMatcher("java.util.Collections " + sourceMethodName + "(..)");
            this.collectionType = Objects.requireNonNull(collectionType);
            this.arity = arity;
        }

        private String getTypeParameters() {
            return switch (this.collectionType) {
                case "List", "Set" -> "<T>";
                case "Map" -> "<K, V>";
                default -> throw new IllegalArgumentException("Unsupported collection type: " + this.collectionType);
            };
        }

        private String getMethodParameters() {
            if (this.arity == 0) {
                return "";
            }

            if ((this.collectionType.equals("List") || this.collectionType.equals("Set")) && this.arity == 1) {
                return "T element";
            }

            if (this.collectionType.equals("Map") && this.arity == 2) {
                return "K key, V value";
            }

            throw new IllegalArgumentException(
                "Unsupported arity " + this.arity + " for collection type: " + this.collectionType
            );
        }

        private String getTargetMethod() {
            return this.arity == 0 ? "empty" : "of";
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

            if (!this.methodMatcher.matches(mi)) {
                return mi;
            }

            if (this.arity > 0 && mi.getArguments().size() != this.arity) {
                return mi;
            }

            String targetFactorySimpleName = this.collectionType + 's';
            String targetImport = "org.eclipse.collections.api.factory." + targetFactorySimpleName;
            this.maybeAddImport(targetImport);
            this.maybeRemoveImport("java.util.Collections");

            JavaTemplate template = this.getTemplate();

            if (this.arity == 0) {
                return template.apply(this.getCursor(), mi.getCoordinates().replace());
            }
            if (this.arity == 1) {
                return template.apply(this.getCursor(), mi.getCoordinates().replace(), mi.getArguments().get(0));
            }
            if (this.arity == 2) {
                return template.apply(
                    this.getCursor(),
                    mi.getCoordinates().replace(),
                    mi.getArguments().get(0),
                    mi.getArguments().get(1)
                );
            }
            return mi;
        }

        private JavaTemplate getTemplate() {
            String methodName = this.getTargetMethod();
            String factoryTemplateCode = """
                package org.eclipse.collections.api.factory;
                public final class {0}s '{'
                    public static final FixedSize{0}Factory fixedSize = null;
                    public static final class FixedSize{0}Factory '{'
                        public {1} java.util.{0}{1} {2}({3}) '{ return null; }'
                    '}'
                '}'""";
            String factoryStubCode = java.text.MessageFormat.format(
                factoryTemplateCode,
                this.collectionType,
                this.getTypeParameters(),
                methodName,
                this.getMethodParameters()
            );

            JavaParser.Builder<? extends JavaParser, ?> javaParser = JavaParser.fromJavaVersion()
                .classpath("eclipse-collections-api")
                .dependsOn(factoryStubCode);

            String templatePattern = this.collectionType + "s.fixedSize." + methodName + this.buildTemplateArgs();
            return JavaTemplate.builder(templatePattern)
                .imports("org.eclipse.collections.api.factory." + this.collectionType + 's')
                .contextSensitive()
                .javaParser(javaParser)
                .build();
        }

        private String buildTemplateArgs() {
            return switch (this.arity) {
                case 0 -> "()";
                case 1 -> "(#{any()})";
                case 2 -> "(#{any()}, #{any()})";
                default -> throw new IllegalArgumentException("Unsupported arity: " + this.arity);
            };
        }
    }
}
