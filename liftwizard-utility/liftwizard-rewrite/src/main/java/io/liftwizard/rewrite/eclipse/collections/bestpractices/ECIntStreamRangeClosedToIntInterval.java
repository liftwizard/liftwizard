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
import org.openrewrite.java.tree.J;

/**
 * Transforms {@code IntStream.rangeClosed(from, to)} to Eclipse Collections
 * {@code IntInterval.fromTo(from, to)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * IntStream.rangeClosed(1, 10)
 *
 * // After
 * IntInterval.fromTo(1, 10)
 * }</pre>
 *
 * <p>Eclipse Collections' {@code IntInterval} is a memory-efficient, first-class collection
 * that supports operations like {@code select}, {@code collect}, {@code sum}, etc.
 * without boxing to {@code Integer}.
 */
public class ECIntStreamRangeClosedToIntInterval extends Recipe {

	private static final MethodMatcher RANGE_CLOSED_MATCHER = new MethodMatcher(
		"java.util.stream.IntStream rangeClosed(int, int)"
	);

	@Override
	public String getDisplayName() {
		return "`IntStream.rangeClosed(from, to)` -> `IntInterval.fromTo(from, to)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `IntStream.rangeClosed(from, to)` to `IntInterval.fromTo(from, to)`. "
			+ "Eclipse Collections' `IntInterval` is a memory-efficient, first-class collection "
			+ "that supports `select`, `collect`, `sum`, and other operations without boxing."
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
		return Preconditions.check(new UsesMethod<>(RANGE_CLOSED_MATCHER), new IntStreamRangeClosedVisitor());
	}

	private static final class IntStreamRangeClosedVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final JavaTemplate INT_INTERVAL_FROM_TO = JavaTemplate.builder(
			"IntInterval.fromTo(#{any(int)}, #{any(int)})"
		)
			.imports("org.eclipse.collections.impl.list.primitive.IntInterval")
			.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
			.build();

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

			if (!RANGE_CLOSED_MATCHER.matches(mi)) {
				return mi;
			}

			if (mi.getArguments().size() != 2) {
				return mi;
			}

			this.maybeRemoveImport("java.util.stream.IntStream");
			this.maybeAddImport("org.eclipse.collections.impl.list.primitive.IntInterval");

			return INT_INTERVAL_FROM_TO.apply(
				this.getCursor(),
				mi.getCoordinates().replace(),
				mi.getArguments().get(0),
				mi.getArguments().get(1)
			);
		}
	}
}
