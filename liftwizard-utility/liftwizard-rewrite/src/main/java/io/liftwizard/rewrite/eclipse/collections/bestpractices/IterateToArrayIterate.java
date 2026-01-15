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
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class IterateToArrayIterate extends Recipe {

	private static final MethodMatcher ITERATE_MATCHER = new MethodMatcher(
		"org.eclipse.collections.impl.utility.Iterate *(..)",
		true
	);
	private static final MethodMatcher ARRAYS_AS_LIST = new MethodMatcher("java.util.Arrays asList(..)");

	@Override
	public String getDisplayName() {
		return "`Iterate.method(Arrays.asList(array))` → `ArrayIterate.method(array)`";
	}

	@Override
	public String getDescription() {
		return "Replace `Iterate.method(Arrays.asList(array), ...)` with `ArrayIterate.method(array, ...)` for better performance when working with arrays.";
	}

	@Override
	public Set<String> getTags() {
		return Sets.fixedSize.with("eclipse-collections");
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(10);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			@Override
			public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
				J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

				if (!ITERATE_MATCHER.matches(mi)) {
					return mi;
				}

				if (mi.getArguments().isEmpty()) {
					return mi;
				}

				Expression firstArgument = mi.getArguments().get(0);
				if (!(firstArgument instanceof J.MethodInvocation arraysAsListCall)) {
					return mi;
				}

				if (!ARRAYS_AS_LIST.matches(arraysAsListCall)) {
					return mi;
				}

				if (arraysAsListCall.getArguments().size() != 1) {
					return mi;
				}

				final Expression arrayArgument = arraysAsListCall.getArguments().get(0);
				this.maybeRemoveImport("org.eclipse.collections.impl.utility.Iterate");
				this.maybeAddImport("org.eclipse.collections.impl.utility.ArrayIterate");
				this.maybeRemoveImport("java.util.Arrays");

				String methodName = mi.getSimpleName();
				List<Expression> remainingArguments = mi.getArguments().subList(1, mi.getArguments().size());
				String templatePattern = this.buildTemplatePattern(methodName, remainingArguments.size());
				JavaTemplate template = JavaTemplate.builder(templatePattern)
					.imports("org.eclipse.collections.impl.utility.ArrayIterate")
					.contextSensitive()
					.javaParser(
						JavaParser.fromJavaVersion()
							.classpath("eclipse-collections")
							.dependsOn(
								"""
								package org.eclipse.collections.impl.utility;
								import org.eclipse.collections.api.block.predicate.Predicate;
								import org.eclipse.collections.api.block.function.Function;
								import org.eclipse.collections.api.block.procedure.Procedure;
								public final class ArrayIterate {
								    public static <T> boolean anySatisfy(T[] array, Predicate<? super T> predicate) { return false; }
								    public static <T> boolean allSatisfy(T[] array, Predicate<? super T> predicate) { return false; }
								    public static <T> boolean noneSatisfy(T[] array, Predicate<? super T> predicate) { return false; }
								    public static <T> T detect(T[] array, Predicate<? super T> predicate) { return null; }
								    public static <T> int count(T[] array, Predicate<? super T> predicate) { return 0; }
								    public static <T, V> java.util.Collection<V> collect(T[] array, Function<? super T, ? extends V> function) { return null; }
								    public static <T> void forEach(T[] array, Procedure<? super T> procedure) {}
								    public static <T> T getFirst(T[] array) { return null; }
								    public static <T> T getLast(T[] array) { return null; }
								}"""
							)
					)
					.build();

				Object[] templateArguments = new Object[remainingArguments.size() + 1];
				templateArguments[0] = arrayArgument;
				for (int i = 0; i < remainingArguments.size(); i++) {
					templateArguments[i + 1] = remainingArguments.get(i);
				}

				return template.apply(this.getCursor(), mi.getCoordinates().replace(), templateArguments);
			}

			private String buildTemplatePattern(String methodName, int additionalArgCount) {
				StringBuilder pattern = new StringBuilder("ArrayIterate.").append(methodName).append("(#{any()}");
				for (int i = 0; i < additionalArgCount; i++) {
					pattern.append(", #{any()}");
				}
				pattern.append(")");
				return pattern.toString();
			}
		};
	}
}
