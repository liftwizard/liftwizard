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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.search.UsesMethod;

/**
 * Search recipe that finds Log4j 1.x generic {@code log(Priority, ..)} logging usage.
 *
 * <p>Log4j 1's {@code Category.log(Priority, Object, ..)} dispatches at a level supplied as a
 * {@code Priority} argument. SLF4J has no generic {@code log(level, ..)} method — only the fixed
 * {@code trace}/{@code debug}/{@code info}/{@code warn}/{@code error} family — so the level dispatch
 * has no equivalent. The upstream Log4j 1 to SLF4J migration does not lower these calls; it rewrites
 * the logger type to {@code org.slf4j.Logger} but leaves the {@code log(..)} invocation in place,
 * producing code that no longer compiles. This is true regardless of the message type, so unlike
 * {@link UsesLog4j1ObjectLogging} this recipe flags every {@code log(Priority, ..)} call rather than
 * only those with an Object message.
 *
 * <p>Detection uses {@link UsesMethod}, which matches every call form — direct invocations
 * ({@code LOGGER.log(Level.INFO, message)}) and method references ({@code LOGGER::log}) — via the
 * compilation unit's resolved method types.
 *
 * <p>This recipe is the basis for the {@link DoesNotUseLog4j1LogWithPriority} precondition, which
 * prevents the Log4j 1 to SLF4J migration from running on files that use this pattern.
 */
public final class UsesLog4j1LogWithPriority extends Recipe {

	@Override
	public String getDisplayName() {
		return "Find Log4j 1.x log(Priority, ..) usage";
	}

	@Override
	public String getDescription() {
		return (
			"Finds Log4j 1.x calls to the generic `log(Priority, ..)` method "
			+ "(e.g., `LOGGER.log(Level.INFO, message)` or `LOGGER::log`). SLF4J has no generic "
			+ "`log(level, ..)` method, so these cannot be migrated automatically and the upstream "
			+ "migration leaves them as uncompilable calls on an `org.slf4j.Logger`."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return logWithPriorityUsage();
	}

	/**
	 * Builds the visitor that flags any Log4j 1.x {@code log(Priority, ..)} usage. Shared with
	 * {@link DoesNotUseLog4j1LogWithPriority} so the precondition stays in sync with this recipe's
	 * detection. {@code Logger extends Category} and inherits {@code log(..)}, so matching the
	 * declaring type with overrides covers both.
	 */
	static TreeVisitor<?, ExecutionContext> logWithPriorityUsage() {
		return new UsesMethod<>("org.apache.log4j.Category log(..)", true);
	}
}
