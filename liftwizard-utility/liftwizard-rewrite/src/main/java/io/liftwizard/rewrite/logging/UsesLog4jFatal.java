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

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;

/**
 * Search recipe that finds Log4j 1.x {@code fatal} logging usage.
 *
 * <p>Log4j 1's {@code Category.fatal(Object)} logs at the {@code FATAL} level, which has no
 * equivalent in SLF4J (whose highest level is {@code ERROR}). Migrating such usage to SLF4J
 * would either break compilation or silently downgrade it to {@code error}, losing the
 * {@code FATAL} severity.
 *
 * <p>Detection covers all the ways FATAL reaches Log4j 1:
 * <ul>
 *     <li>{@code fatal(..)} method <em>invocations</em> ({@code LOGGER.fatal(message)});</li>
 *     <li>{@code fatal} method <em>references</em> ({@code LOGGER::fatal});</li>
 *     <li>the {@code Level.FATAL} / {@code Priority.FATAL} constant (e.g.
 *         {@code LOGGER.log(Level.FATAL, message)} or any bare reference to the constant).</li>
 * </ul>
 *
 * <p>Method invocations and references are found with {@link UsesMethod}, which matches every call
 * form via the compilation unit's resolved method types. The {@code FATAL} constant is a field, not
 * a method, so it is found with a dedicated field-access visitor.
 *
 * <p>This recipe is the basis for the {@link DoesNotUseLog4jFatal} precondition, which prevents the
 * Log4j 1 to SLF4J migration from running on files that use this pattern.
 */
public final class UsesLog4jFatal extends Recipe {

	private static final String LOG4J_PRIORITY = "org.apache.log4j.Priority";
	private static final String FATAL_FIELD_NAME = "FATAL";

	@Override
	public String getDisplayName() {
		return "Find Log4j 1.x fatal logging usage";
	}

	@Override
	public String getDescription() {
		return (
			"Finds Log4j 1.x logging that uses the `fatal` level, including `fatal(..)` method "
			+ "invocations (e.g., `LOGGER.fatal(message)`), `fatal` method references "
			+ "(e.g., `LOGGER::fatal`), and the `Level.FATAL`/`Priority.FATAL` constant "
			+ "(e.g., `LOGGER.log(Level.FATAL, message)`). SLF4J has no `fatal` level, so these "
			+ "cannot be migrated automatically without losing the FATAL severity."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return fatalUsage();
	}

	/**
	 * Builds the visitor that flags any Log4j 1.x FATAL usage. Shared with {@link DoesNotUseLog4jFatal}
	 * so the precondition stays in sync with this recipe's detection.
	 */
	static TreeVisitor<?, ExecutionContext> fatalUsage() {
		// Logger extends Category and inherits fatal(..), so matching the declaring type covers both.
		return Preconditions.or(
			new UsesMethod<>("org.apache.log4j.Category fatal(..)", true),
			new LevelFatalConstantVisitor()
		);
	}

	/**
	 * Flags references to the {@code FATAL} constant declared on {@code org.apache.log4j.Priority}
	 * (and its subclass {@code Level}), whether qualified ({@code Level.FATAL}) or statically imported
	 * (bare {@code FATAL}).
	 */
	static final class LevelFatalConstantVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
			J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
			if (isFatalConstant(fa.getSimpleName(), fa.getTarget().getType())) {
				return SearchResult.found(fa);
			}
			return fa;
		}

		@Override
		public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
			J.Identifier id = super.visitIdentifier(identifier, ctx);
			// Cheap name check first: visitIdentifier runs for every identifier in the file.
			if (!FATAL_FIELD_NAME.equals(id.getSimpleName()) || isFieldAccessName(this.getCursor())) {
				return id;
			}
			JavaType.Variable fieldType = id.getFieldType();
			if (fieldType != null && TypeUtils.isAssignableTo(LOG4J_PRIORITY, fieldType.getOwner())) {
				return SearchResult.found(id);
			}
			return id;
		}

		private static boolean isFatalConstant(String simpleName, JavaType ownerType) {
			return FATAL_FIELD_NAME.equals(simpleName) && TypeUtils.isAssignableTo(LOG4J_PRIORITY, ownerType);
		}

		private static boolean isFieldAccessName(Cursor cursor) {
			return cursor.getParentTreeCursor().getValue() instanceof J.FieldAccess;
		}
	}
}
