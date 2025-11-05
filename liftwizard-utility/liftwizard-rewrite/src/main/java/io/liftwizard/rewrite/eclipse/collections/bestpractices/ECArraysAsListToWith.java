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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ECArraysAsListToWith extends Recipe {

    @Override
    public String getDisplayName() {
        return "`FastList.newList(Arrays.asList())` → `Lists.mutable.with()`";
    }

    @Override
    public String getDescription() {
        return (
            "Replace `FastList.newList(Arrays.asList())`, `UnifiedSet.newSet(Arrays.asList())`, " +
            "`HashBag.newBag(Arrays.asList())`, `TreeSortedSet.newSet(Arrays.asList())`, and " +
            "`TreeBag.newBag(Arrays.asList())` with Eclipse Collections factory methods using varargs. " +
            "This recipe properly handles varargs of any arity. " +
            "Note: This could be refactored to use Refaster once OpenRewrite issue #4397 is resolved, " +
            "which will add support for the @Repeated annotation for varargs matching."
        );
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(15);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ArraysAsListToWithVisitor();
    }

    private static final class ArraysAsListToWithVisitor extends JavaIsoVisitor<ExecutionContext> {

        private static final MethodMatcher FAST_LIST_NEW_LIST = new MethodMatcher(
            "org.eclipse.collections.impl.list.mutable.FastList newList(java.lang.Iterable)"
        );
        private static final MethodMatcher UNIFIED_SET_NEW_SET = new MethodMatcher(
            "org.eclipse.collections.impl.set.mutable.UnifiedSet newSet(java.lang.Iterable)"
        );
        private static final MethodMatcher HASH_BAG_NEW_BAG = new MethodMatcher(
            "org.eclipse.collections.impl.bag.mutable.HashBag newBag(java.lang.Iterable)"
        );
        private static final MethodMatcher TREE_SORTED_SET_NEW_SET = new MethodMatcher(
            "org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet newSet(java.lang.Iterable)"
        );
        private static final MethodMatcher TREE_SORTED_SET_NEW_SET_WITH_COMPARATOR = new MethodMatcher(
            "org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet newSet(java.util.Comparator, java.lang.Iterable)"
        );
        private static final MethodMatcher TREE_BAG_NEW_BAG = new MethodMatcher(
            "org.eclipse.collections.impl.bag.sorted.mutable.TreeBag newBag(java.lang.Iterable)"
        );
        private static final MethodMatcher TREE_BAG_NEW_BAG_WITH_COMPARATOR = new MethodMatcher(
            "org.eclipse.collections.impl.bag.sorted.mutable.TreeBag newBag(java.util.Comparator, java.lang.Iterable)"
        );
        private static final MethodMatcher ARRAYS_AS_LIST = new MethodMatcher("java.util.Arrays asList(..)");

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

            String factoryClass = null;
            String factoryMethod = null;
            boolean hasComparator = false;

            if (FAST_LIST_NEW_LIST.matches(mi)) {
                factoryClass = "Lists";
                factoryMethod = "mutable";
            } else if (UNIFIED_SET_NEW_SET.matches(mi)) {
                factoryClass = "Sets";
                factoryMethod = "mutable";
            } else if (HASH_BAG_NEW_BAG.matches(mi)) {
                factoryClass = "Bags";
                factoryMethod = "mutable";
            } else if (TREE_SORTED_SET_NEW_SET.matches(mi)) {
                factoryClass = "SortedSets";
                factoryMethod = "mutable";
            } else if (TREE_SORTED_SET_NEW_SET_WITH_COMPARATOR.matches(mi)) {
                factoryClass = "SortedSets";
                factoryMethod = "mutable";
                hasComparator = true;
            } else if (TREE_BAG_NEW_BAG.matches(mi)) {
                factoryClass = "SortedBags";
                factoryMethod = "mutable";
            } else if (TREE_BAG_NEW_BAG_WITH_COMPARATOR.matches(mi)) {
                factoryClass = "SortedBags";
                factoryMethod = "mutable";
                hasComparator = true;
            }

            if (factoryClass == null) {
                return mi;
            }

            Expression argument;
            Expression comparatorArg = null;

            if (hasComparator) {
                if (mi.getArguments().size() != 2) {
                    return mi;
                }
                comparatorArg = mi.getArguments().get(0);
                argument = mi.getArguments().get(1);
            } else {
                if (mi.getArguments().size() != 1) {
                    return mi;
                }
                argument = mi.getArguments().get(0);
            }

            if (!(argument instanceof J.MethodInvocation arraysAsListCall)) {
                return mi;
            }

            if (!ARRAYS_AS_LIST.matches(arraysAsListCall)) {
                return mi;
            }

            List<Expression> varargsElements = arraysAsListCall.getArguments();

            String factoryImport = "org.eclipse.collections.api.factory." + factoryClass;
            this.maybeAddImport(factoryImport);
            this.maybeRemoveImport("java.util.Arrays");

            String templateSource;
            if (hasComparator) {
                String varargsPlaceholder = this.buildVarargsPlaceholder(varargsElements);
                templateSource =
                    factoryClass +
                    "." +
                    factoryMethod +
                    ".with(#{any(java.util.Comparator)}" +
                    (varargsPlaceholder.isEmpty() ? "" : ", " + varargsPlaceholder) +
                    ")";
            } else {
                String varargsPlaceholder = this.buildVarargsPlaceholder(varargsElements);
                templateSource = factoryClass + "." + factoryMethod + ".with(" + varargsPlaceholder + ")";
            }

            JavaTemplate template = JavaTemplate.builder(templateSource)
                .imports(factoryImport)
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
                .build();

            Object[] templateArguments;
            if (hasComparator) {
                templateArguments = new Object[varargsElements.size() + 1];
                templateArguments[0] = comparatorArg;
                for (int i = 0; i < varargsElements.size(); i++) {
                    templateArguments[i + 1] = varargsElements.get(i);
                }
            } else {
                templateArguments = varargsElements.toArray();
            }

            return template.apply(this.getCursor(), mi.getCoordinates().replace(), templateArguments);
        }

        private String buildVarargsPlaceholder(List<Expression> varargsElements) {
            if (varargsElements.isEmpty()) {
                return "";
            }
            return varargsElements
                .stream()
                .map(expr -> "#{any()}")
                .collect(Collectors.joining(", "));
        }
    }
}
