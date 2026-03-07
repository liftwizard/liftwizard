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

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms {@code Map.getOrDefault(key, value)} to Eclipse Collections
 * {@code getIfAbsentValue(key, value)}.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * map.getOrDefault(key, defaultValue)
 *
 * // After
 * map.getIfAbsentValue(key, defaultValue)
 * }</pre>
 *
 * <p>This recipe replaces JDK's {@code Map.getOrDefault()} with Eclipse Collections'
 * {@code MapIterable.getIfAbsentValue()}, which has the same eager evaluation semantics
 * but uses the Eclipse Collections API idiom.
 */
public class ECMapGetOrDefaultToGetIfAbsentValue extends Recipe {

	private static final MethodMatcher GET_OR_DEFAULT_MATCHER = new MethodMatcher(
		"java.util.Map getOrDefault(..)",
		true
	);

	@Override
	public String getDisplayName() {
		return "`map.getOrDefault(key, value)` -> `map.getIfAbsentValue(key, value)`";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `map.getOrDefault(key, value)` to `map.getIfAbsentValue(key, value)` "
			+ "for Eclipse Collections map types. This replaces the JDK Map method with the "
			+ "idiomatic Eclipse Collections equivalent on MapIterable."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(GET_OR_DEFAULT_MATCHER),
			new GetOrDefaultToGetIfAbsentValueVisitor()
		);
	}

	private static final class GetOrDefaultToGetIfAbsentValueVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!GET_OR_DEFAULT_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			List<Expression> arguments = methodInvocation.getArguments();
			if (arguments.size() != 2) {
				return methodInvocation;
			}

			Expression select = methodInvocation.getSelect();
			if (select == null) {
				return methodInvocation;
			}

			if (!this.isEclipseCollectionsMapType(select)) {
				return methodInvocation;
			}

			J.Identifier newMethodName = methodInvocation.getName().withSimpleName("getIfAbsentValue");

			return methodInvocation.withName(newMethodName);
		}

		private boolean isEclipseCollectionsMapType(Expression expression) {
			return TypeUtils.isAssignableTo("org.eclipse.collections.api.map.MapIterable", expression.getType());
		}
	}
}
