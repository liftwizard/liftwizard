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

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECCollectionAddProcedureToFactory extends Recipe {

    private static final String COLLECTION_ADD_PROCEDURE =
        "org.eclipse.collections.impl.block.procedure.CollectionAddProcedure";

    @Override
    public String getDisplayName() {
        return "`new CollectionAddProcedure<>()` → `CollectionAddProcedure.on()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new CollectionAddProcedure<>()` constructor calls with `CollectionAddProcedure.on()` static factory method.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(20);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ECCollectionAddProcedureToFactoryVisitor();
    }

    private static final class ECCollectionAddProcedureToFactoryVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            JavaTemplate collectionAddProcedureOnTemplate = JavaTemplate.builder("CollectionAddProcedure.on(#{any()})")
                .imports(COLLECTION_ADD_PROCEDURE)
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
                .build();

            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (type == null || !COLLECTION_ADD_PROCEDURE.equals(type.getFullyQualifiedName())) {
                return nc;
            }

            if (nc.getArguments() == null || nc.getArguments().size() != 1) {
                return nc;
            }

            return collectionAddProcedureOnTemplate.apply(
                this.getCursor(),
                nc.getCoordinates().replace(),
                nc.getArguments().get(0)
            );
        }
    }
}
