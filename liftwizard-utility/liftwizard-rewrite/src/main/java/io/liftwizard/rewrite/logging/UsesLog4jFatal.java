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

package io.liftwizard.rewrite.logging;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

/**
 * Search recipe that finds Log4j 1.x {@code fatal} logging calls.
 *
 * <p>Log4j 1's {@code Category.fatal(Object)} logs at the {@code FATAL} level, which has no
 * equivalent in SLF4J (whose highest level is {@code ERROR}). Migrating such calls to SLF4J
 * would either break compilation or silently downgrade them to {@code error}, losing the
 * {@code FATAL} severity.
 *
 * <p>This recipe is used as the basis for the {@link DoesNotUseLog4jFatal} precondition, which
 * prevents the Log4j 1 to SLF4J migration from running on files that use this pattern.
 */
public final class UsesLog4jFatal extends Recipe {

	private static final List<MethodMatcher> LOG_MATCHERS = List.of(
		new MethodMatcher("org.apache.log4j.Logger fatal(..)", true),
		new MethodMatcher("org.apache.log4j.Category fatal(..)", true)
	);

	@Override
	public String getDisplayName() {
		return "Find Log4j 1.x fatal logging calls";
	}

	@Override
	public String getDescription() {
		return (
			"Finds Log4j 1.x logging calls that use the `fatal` level (e.g., `LOGGER.fatal(message)`). "
			+ "SLF4J has no `fatal` level, so these calls cannot be migrated automatically without "
			+ "losing the FATAL severity."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new Log4jFatalVisitor();
	}

	static final class Log4jFatalVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
			for (MethodMatcher matcher : LOG_MATCHERS) {
				if (matcher.matches(m)) {
					return SearchResult.found(m);
				}
			}
			return m;
		}
	}
}
