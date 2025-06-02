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

public class ECCollectionRemoveProcedureToFactory extends Recipe {

    private static final String COLLECTION_REMOVE_PROCEDURE =
        "org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure";

    @Override
    public String getDisplayName() {
        return "`new CollectionRemoveProcedure<>()` → `CollectionRemoveProcedure.on()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new CollectionRemoveProcedure<>()` constructor calls with `CollectionRemoveProcedure.on()` static factory method.";
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
        return new JavaVisitor<ExecutionContext>() {
            private final JavaTemplate collectionRemoveProcedureOnTemplate = JavaTemplate.builder(
                "CollectionRemoveProcedure.on(#{any()})"
            )
                .imports(COLLECTION_REMOVE_PROCEDURE)
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                // Check if this is a CollectionRemoveProcedure constructor
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
                if (type == null || !COLLECTION_REMOVE_PROCEDURE.equals(type.getFullyQualifiedName())) {
                    return nc;
                }

                // Check if constructor has exactly one argument
                if (nc.getArguments() == null || nc.getArguments().size() != 1) {
                    return nc;
                }

                // Replace with CollectionRemoveProcedure.on() static factory method
                return collectionRemoveProcedureOnTemplate.apply(
                    getCursor(),
                    nc.getCoordinates().replace(),
                    nc.getArguments().get(0)
                );
            }
        };
    }
}
