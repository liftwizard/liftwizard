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
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

/**
 * Precondition recipe that matches files which do <em>not</em> use Log4j 1.x
 * {@code log(Priority, ..)} logging.
 *
 * <p>This is the negation of {@link UsesLog4j1LogWithPriority}. It is intended to be used as a
 * YAML {@code preconditions} entry so that a composite migration recipe (e.g., Log4j 1 to SLF4J)
 * skips files that call the generic {@code log(Priority, ..)} method, which SLF4J cannot express
 * and the upstream migration would otherwise leave as uncompilable code.
 */
public final class DoesNotUseLog4j1LogWithPriority extends Recipe {

	@Override
	public String getDisplayName() {
		return "Does not use Log4j 1.x log(Priority, ..)";
	}

	@Override
	public String getDescription() {
		return (
			"Precondition that matches source files which do not call the Log4j 1.x generic "
			+ "`log(Priority, ..)` method. Files that use it (e.g., `LOGGER.log(Level.INFO, message)`) "
			+ "are excluded, because SLF4J has no generic `log(level, ..)` method and migrating them "
			+ "would produce uncompilable code."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.not(UsesLog4j1LogWithPriority.logWithPriorityUsage());
	}
}
