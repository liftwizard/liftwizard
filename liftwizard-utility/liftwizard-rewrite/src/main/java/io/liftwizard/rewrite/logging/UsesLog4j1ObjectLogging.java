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

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;

/**
 * Search recipe that finds Log4j 1.x logging usage where the message argument is not a String.
 *
 * <p>Log4j 1's {@code Category.info(Object)} accepts any Object, enabling structured logging
 * where appenders can inspect the argument's type via {@code instanceof}. Migrating such usage
 * to SLF4J (which only has {@code info(String)}) would either break compilation or silently
 * destroy structured logging if transformed to {@code info("{}", object)}.
 *
 * <p>{@code Throwable} message arguments are excluded: calls like {@code LOGGER.error(exception)}
 * are not structured object logging and migrate safely to SLF4J, which has a native
 * {@code error(String, Throwable)} overload.
 *
 * <p>Recognizing logging usage (invocations, {@code log(Priority, Object)}, and method references)
 * is delegated to {@link Log4j1LoggingSupport}; this recipe contributes only the policy of which
 * <em>message types</em> are unsafe to migrate. An unresolved message type is treated as an object,
 * so the recipe (and the {@link DoesNotUseLog4j1ObjectLogging} precondition built on it) errs toward
 * <em>not</em> migrating.
 */
public final class UsesLog4j1ObjectLogging extends Recipe {

	@Override
	public String getDisplayName() {
		return "Find Log4j 1.x object logging calls";
	}

	@Override
	public String getDescription() {
		return (
			"Finds Log4j 1.x logging where the message argument is not a CharSequence. "
			+ "These pass an Object directly (e.g., `LOGGER.info(myObject)`, "
			+ "`LOGGER.log(Level.INFO, myObject)`, or `collection.forEach(LOGGER::info)`) "
			+ "which allows appenders to inspect the object's type for structured logging. "
			+ "CharSequence message arguments (e.g., String, StringBuilder) and Throwable "
			+ "message arguments (e.g., `LOGGER.error(exception)`) are excluded "
			+ "because they migrate safely to SLF4J."
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
			Expression message = Log4j1LoggingSupport.messageArgument(m);
			if (message != null && isObjectMessage(message.getType())) {
				return SearchResult.found(m);
			}
			return m;
		}

		@Override
		public J.MemberReference visitMemberReference(J.MemberReference memberReference, ExecutionContext ctx) {
			J.MemberReference mr = super.visitMemberReference(memberReference, ctx);
			if (
				Log4j1LoggingSupport.isLoggingReference(mr)
				&& isObjectMessage(Log4j1LoggingSupport.consumedMessageType(mr))
			) {
				return SearchResult.found(mr);
			}
			return mr;
		}

		/**
		 * Whether the message type is a non-String, non-Throwable object — i.e. structured object logging
		 * that does not migrate safely to SLF4J. An unresolved ({@code null}) type is treated as an object
		 * so the precondition errs toward not migrating.
		 */
		private static boolean isObjectMessage(@Nullable JavaType type) {
			return !isCharSequence(type) && !TypeUtils.isAssignableTo("java.lang.Throwable", type);
		}

		/**
		 * Returns whether the type is a CharSequence, and thus a string-like message that migrates
		 * safely to SLF4J. Both checks are required: a string literal carries
		 * {@link JavaType.Primitive#String}, which {@code isAssignableTo("java.lang.CharSequence", ..)}
		 * does not match, while {@code StringBuilder} and {@code StringBuffer} are matched only by the
		 * assignability check.
		 */
		private static boolean isCharSequence(@Nullable JavaType type) {
			return TypeUtils.isString(type) || TypeUtils.isAssignableTo("java.lang.CharSequence", type);
		}
	}
}
