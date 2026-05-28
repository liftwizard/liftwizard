/*
 * Copyright 2026 Craig Motlin
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;

/**
 * Converts a single-arg Eclipse Collections method like {@code select(Predicate)} to its {@code *With}
 * method-reference variant like {@code selectWith(Predicate2, P)}.
 *
 * <p>Parameterized via {@code matcherPattern} (full {@link MethodMatcher} pattern of the source method)
 * and {@code targetMethodName} (the {@code *With} method to rewrite to). Intended to be instantiated from
 * a YAML composite — see {@code META-INF/rewrite/eclipse-collections/bestpractices/garbage-free-lambdas.yml}.
 */
public class GarbageFreeLambdaRecipe extends Recipe {

	@Option(
		displayName = "Matcher pattern",
		description = "The MethodMatcher pattern identifying the source method to rewrite.",
		example = "org.eclipse.collections.api.RichIterable select(org.eclipse.collections.api.block.predicate.Predicate)"
	)
	private final String matcherPattern;

	@Option(
		displayName = "Target method name",
		description = "The name of the *With method-reference variant to rewrite to.",
		example = "selectWith"
	)
	private final String targetMethodName;

	@JsonCreator
	public GarbageFreeLambdaRecipe(
		@JsonProperty("matcherPattern") String matcherPattern,
		@JsonProperty("targetMethodName") String targetMethodName
	) {
		this.matcherPattern = matcherPattern;
		this.targetMethodName = targetMethodName;
	}

	@Override
	public String getDisplayName() {
		return "Convert Eclipse Collections capturing lambda to `*With` method reference";
	}

	@Override
	public String getDescription() {
		return (
			"Rewrites a single-arg Eclipse Collections call like `richIterable.select(x -> x.foo(captured))` "
			+ "to its non-capturing `*With` form `richIterable.selectWith(Type::foo, captured)`. "
			+ "The captured value must not reference the lambda parameter."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		MethodMatcher matcher = new MethodMatcher(this.matcherPattern, true);
		return Preconditions.check(
			new UsesMethod<>(matcher),
			new GarbageFreeLambdaVisitor(matcher, this.targetMethodName)
		);
	}
}
