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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ECAnySatisfyEqualsToContains extends Recipe {

	private static final MethodMatcher ANY_SATISFY_MATCHER = new MethodMatcher(
		"org.eclipse.collections.api.RichIterable anySatisfy(org.eclipse.collections.api.block.predicate.Predicate)"
	);

	@Override
	public String getDisplayName() {
		return "`anySatisfy(value::equals)` to `contains(value)`";
	}

	@Override
	public String getDescription() {
		return (
			"Converts `iterable.anySatisfy(value::equals)` to `iterable.contains(value)` for Eclipse Collections types. "
			+ "The contains() method is simpler and more readable, and may be faster for indexed collections."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(ANY_SATISFY_MATCHER), new AnySatisfyEqualsToContainsVisitor());
	}

	private static final class AnySatisfyEqualsToContainsVisitor extends JavaVisitor<ExecutionContext> {

		@Override
		public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

			if (!ANY_SATISFY_MATCHER.matches(mi)) {
				return mi;
			}

			List<Expression> args = mi.getArguments();
			if (args.size() != 1 || !(args.get(0) instanceof J.MemberReference memberRef)) {
				return mi;
			}

			if (!"equals".equals(memberRef.getReference().getSimpleName())) {
				return mi;
			}

			Expression select = mi.getSelect();
			Expression value = memberRef.getContaining();

			JavaTemplate template = JavaTemplate.builder("#{any()}.contains(#{any()})")
				.javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
				.build();

			return template.apply(getCursor(), mi.getCoordinates().replace(), select, value);
		}
	}
}
