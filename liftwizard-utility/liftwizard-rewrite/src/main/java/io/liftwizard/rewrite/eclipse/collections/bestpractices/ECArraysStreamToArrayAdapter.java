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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

/**
 * Transforms {@code Arrays.stream(array)} chains to Eclipse Collections
 * {@code ArrayAdapter.adapt(array)} chains.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * Arrays.stream(values).skip(1).toArray()
 * Arrays.stream(values).filter(each -> !each.isEmpty()).anyMatch(each -> each.length() > 3)
 *
 * // After
 * ArrayAdapter.adapt(values).drop(1).toArray()
 * ArrayAdapter.adapt(values).select(each -> !each.isEmpty()).anySatisfy(each -> each.length() > 3)
 * }</pre>
 *
 * <p>Eclipse Collections' {@code ArrayAdapter} wraps an array as a {@code RichIterable},
 * providing the full Eclipse Collections API ({@code select}, {@code collect},
 * {@code groupBy}, etc.) without copying the array.
 *
 * <p>Chain walking, the operation translation tables, and the shared guards live in
 * {@link AbstractECStreamChainVisitor}. This recipe adds the array-specific root handling:
 * only the single-argument {@code Arrays.stream(T[])} overload matches — not the primitive
 * overloads ({@code int[]}, {@code long[]}, {@code double[]}) or the range overload — and the
 * root is replaced with {@code ArrayAdapter.adapt(array)}.
 */
public class ECArraysStreamToArrayAdapter extends Recipe {

	private static final MethodMatcher ARRAYS_STREAM_MATCHER = new MethodMatcher("java.util.Arrays stream(..)");

	private static final String[] STUBS = {
		"""
			package org.eclipse.collections.impl.list.fixed;
			public final class ArrayAdapter<T> {
			    public static <T> ArrayAdapter<T> adapt(T... array) { return null; }
			}
			""",
	};

	@Override
	public String getDisplayName() {
		return "`Arrays.stream(array)` -> `ArrayAdapter.adapt(array)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `Arrays.stream(array)` chains to `ArrayAdapter.adapt(array)` chains, "
			+ "renaming intermediate operations to their Eclipse Collections equivalents "
			+ "(`skip` to `drop`, `limit` to `take`, `filter` to `select`, `map` to `collect`, "
			+ "`anyMatch` to `anySatisfy`, ...). Eclipse Collections' `ArrayAdapter` wraps an array "
			+ "as a `RichIterable` without copying the array."
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

	private static final class ECArraysStreamToArrayAdapterVisitor extends AbstractECStreamChainVisitor {

		private static final JavaTemplate ARRAY_ADAPTER_ADAPT = JavaTemplate.builder("ArrayAdapter.adapt(#{any()})")
			.imports("org.eclipse.collections.impl.list.fixed.ArrayAdapter")
			.javaParser(JavaParser.fromJavaVersion().dependsOn(STUBS))
			.build();

		@Override
		protected boolean isChainRoot(J.MethodInvocation invocation) {
			return ARRAYS_STREAM_MATCHER.matches(invocation);
		}

		@Override
		protected boolean isRootTranslatable(J.MethodInvocation root) {
			if (root.getArguments().size() != 1) {
				return false;
			}

			// Only match Object[] arrays, not primitive arrays (int[], long[], double[])
			JavaType argType = root.getArguments().get(0).getType();
			if (argType instanceof JavaType.Array arrayType && arrayType.getElemType() instanceof JavaType.Primitive) {
				return false;
			}
			return true;
		}

		@Override
		protected J.MethodInvocation visitTranslatableRoot(J.MethodInvocation root) {
			this.maybeRemoveImport("java.util.Arrays");
			this.maybeAddImport("org.eclipse.collections.impl.list.fixed.ArrayAdapter");
			return ARRAY_ADAPTER_ADAPT.apply(
				this.getCursor(),
				root.getCoordinates().replace(),
				root.getArguments().get(0)
			);
		}
	}
}
