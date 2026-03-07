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
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

/**
 * Transforms {@code Arrays.stream(array)} to Eclipse Collections
 * {@code ArrayAdapter.adapt(array)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * Arrays.stream(values)
 *
 * // After
 * ArrayAdapter.adapt(values)
 * }</pre>
 *
 * <p>Eclipse Collections' {@code ArrayAdapter} wraps an array as a {@code RichIterable},
 * providing the full Eclipse Collections API ({@code select}, {@code collect},
 * {@code groupBy}, etc.) without copying the array.
 *
 * <p>Note: This recipe only matches the single-argument {@code Arrays.stream(T[])} overload.
 * It does not match the primitive overloads ({@code int[]}, {@code long[]}, {@code double[]})
 * or the range overload ({@code Arrays.stream(T[], int, int)}).
 */
public class ECArraysStreamToArrayAdapter extends Recipe {

	private static final MethodMatcher ARRAYS_STREAM_MATCHER = new MethodMatcher("java.util.Arrays stream(..)");

	@Override
	public String getDisplayName() {
		return "`Arrays.stream(array)` -> `ArrayAdapter.adapt(array)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `Arrays.stream(array)` to `ArrayAdapter.adapt(array)`. "
			+ "Eclipse Collections' `ArrayAdapter` wraps an array as a `RichIterable`, "
			+ "providing `select`, `collect`, `groupBy`, and other operations "
			+ "without copying the array."
		);
	}

	@Override
	public Set<String> getTags() {
		return Sets.fixedSize.with("eclipse-collections");
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(15);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(ARRAYS_STREAM_MATCHER), new ECArraysStreamToArrayAdapterVisitor());
	}

	private static final class ECArraysStreamToArrayAdapterVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final JavaTemplate ARRAY_ADAPTER_ADAPT = JavaTemplate.builder("ArrayAdapter.adapt(#{any()})")
			.imports("org.eclipse.collections.impl.list.fixed.ArrayAdapter")
			.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
			.build();

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

			if (!ARRAYS_STREAM_MATCHER.matches(mi)) {
				return mi;
			}

			if (mi.getArguments().size() != 1) {
				return mi;
			}

			Expression argument = mi.getArguments().get(0);
			JavaType argType = argument.getType();

			// Only match Object[] arrays, not primitive arrays (int[], long[], double[])
			if (argType instanceof JavaType.Array arrayType) {
				JavaType elemType = arrayType.getElemType();
				if (elemType instanceof JavaType.Primitive) {
					return mi;
				}
			}

			this.maybeRemoveImport("java.util.Arrays");
			this.maybeAddImport("org.eclipse.collections.impl.list.fixed.ArrayAdapter");

			return ARRAY_ADAPTER_ADAPT.apply(this.getCursor(), mi.getCoordinates().replace(), argument);
		}
	}
}
