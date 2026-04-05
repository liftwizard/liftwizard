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
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;

/**
 * Search recipe that finds Log4j 1.x logging calls where the message argument is not a String.
 *
 * <p>Log4j 1's {@code Category.info(Object)} accepts any Object, enabling structured logging
 * where appenders can inspect the argument's type via {@code instanceof}. Migrating such calls
 * to SLF4J (which only has {@code info(String)}) would either break compilation or silently
 * destroy structured logging if transformed to {@code info("{}", object)}.
 *
 * <p>This recipe is used as the basis for the {@link DoesNotUseLog4j1ObjectLogging} precondition,
 * which prevents the Log4j 1 to SLF4J migration from running on files that use this pattern.
 */
public final class UsesLog4j1ObjectLogging extends Recipe {

	private static final List<MethodMatcher> LOG_MATCHERS = List.of(
		new MethodMatcher("org.apache.log4j.Category debug(..)", true),
		new MethodMatcher("org.apache.log4j.Category info(..)", true),
		new MethodMatcher("org.apache.log4j.Category warn(..)", true),
		new MethodMatcher("org.apache.log4j.Category error(..)", true),
		new MethodMatcher("org.apache.log4j.Category fatal(..)", true)
	);

	@Override
	public String getDisplayName() {
		return "Find Log4j 1.x object logging calls";
	}

	@Override
	public String getDescription() {
		return (
			"Finds Log4j 1.x logging calls where the message argument is not a String. "
			+ "These calls pass an Object directly (e.g., `LOGGER.info(myObject)`) "
			+ "which allows appenders to inspect the object's type for structured logging."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new Log4j1ObjectLoggingVisitor();
	}

	static final class Log4j1ObjectLoggingVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
			if (m.getArguments().isEmpty() || m.getArguments().get(0) instanceof J.Empty) {
				return m;
			}
			for (MethodMatcher matcher : LOG_MATCHERS) {
				if (matcher.matches(m)) {
					Expression firstArg = m.getArguments().get(0);
					if (!TypeUtils.isString(firstArg.getType())) {
						return SearchResult.found(m);
					}
					break;
				}
			}
			return m;
		}
	}
}
