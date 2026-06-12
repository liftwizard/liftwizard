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
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Shared support for recognizing Log4j 1.x logging usage and locating the logged <em>message</em>.
 *
 * <p>This deliberately separates <strong>mechanism</strong> — "is this node a Log4j 1 logging call or
 * reference, and where is its message?" — from any <strong>policy</strong> a recipe layers on top of
 * the message (e.g. {@link UsesLog4j1ObjectLogging} asks "is the message a non-String object?").
 * Keeping the mechanism in one place means recipes that care about Log4j 1 logging share a single,
 * tested definition of what a logging call is rather than each re-deriving it.
 *
 * <p>Covers the forms a message can reach Log4j 1:
 * <ul>
 *     <li>level method invocations ({@code LOGGER.info(message)}) — message is the first argument;</li>
 *     <li>the generic {@code log(Priority, Object, ..)} methods — message follows the {@code Priority};</li>
 *     <li>level method references ({@code collection.forEach(LOGGER::info)}) — message type comes from
 *         the functional interface.</li>
 * </ul>
 */
final class Log4j1LoggingSupport {

	private static final String LOG4J_PRIORITY = "org.apache.log4j.Priority";

	/** Cheap guard so the matchers only run for invocations/references with a plausible logging name. */
	private static final Set<String> METHOD_NAMES = Set.of("trace", "debug", "info", "warn", "error", "fatal", "log");

	/**
	 * Level methods whose message is the first argument. Both the {@code Category}- and
	 * {@code Logger}-declared variants are matched so detection survives when the
	 * {@code Logger extends Category} inheritance is not resolvable, in which case a call resolves to
	 * {@code Logger.<level>} rather than the inherited {@code Category.<level>}.
	 */
	private static final List<MethodMatcher> LEVEL_MATCHERS = List.of(
		new MethodMatcher("org.apache.log4j.Category debug(..)", true),
		new MethodMatcher("org.apache.log4j.Category info(..)", true),
		new MethodMatcher("org.apache.log4j.Category warn(..)", true),
		new MethodMatcher("org.apache.log4j.Category error(..)", true),
		new MethodMatcher("org.apache.log4j.Category fatal(..)", true),
		new MethodMatcher("org.apache.log4j.Logger trace(..)", true),
		new MethodMatcher("org.apache.log4j.Logger debug(..)", true),
		new MethodMatcher("org.apache.log4j.Logger info(..)", true),
		new MethodMatcher("org.apache.log4j.Logger warn(..)", true),
		new MethodMatcher("org.apache.log4j.Logger error(..)", true),
		new MethodMatcher("org.apache.log4j.Logger fatal(..)", true)
	);

	/** Generic log methods whose message is the argument following the {@code Priority}. */
	private static final List<MethodMatcher> LOG_MATCHERS = List.of(
		new MethodMatcher("org.apache.log4j.Category log(..)", true),
		new MethodMatcher("org.apache.log4j.Logger log(..)", true)
	);

	private Log4j1LoggingSupport() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * The message argument of a Log4j 1 logging invocation, or {@code null} if {@code m} is not a Log4j 1
	 * logging call. A non-null result with an unresolved {@link Expression#getType()} is a logging call
	 * whose message type could not be determined — callers decide how to treat that.
	 */
	static @Nullable Expression messageArgument(J.MethodInvocation m) {
		if (!METHOD_NAMES.contains(m.getSimpleName())) {
			return null;
		}
		if (m.getArguments().isEmpty() || m.getArguments().get(0) instanceof J.Empty) {
			return null;
		}
		if (matchesAny(LEVEL_MATCHERS, m)) {
			return m.getArguments().get(0);
		}
		if (matchesAny(LOG_MATCHERS, m)) {
			return logMessageArgument(m);
		}
		return null;
	}

	/** Whether {@code mr} is a reference to a Log4j 1 logging method (e.g. {@code LOGGER::info}). */
	static boolean isLoggingReference(J.MemberReference mr) {
		return (
			METHOD_NAMES.contains(mr.getReference().getSimpleName())
			&& (matchesAny(LEVEL_MATCHERS, mr) || matchesAny(LOG_MATCHERS, mr))
		);
	}

	/**
	 * The message type consumed by a Log4j 1 logging method reference, taken from the functional
	 * interface it implements. Only a <em>single</em>-type-parameter interface (a {@code Consumer<T>}-shaped
	 * SAM — the common {@code forEach}/{@code ifPresent} case) lets us identify the consumed type
	 * unambiguously as {@code T}. Raw types, multi-parameter SAMs (e.g. an unbound {@code Logger::info}
	 * bound to {@code BiConsumer<Logger, Object>}, where the first type argument is the receiver, not the
	 * message), and unresolved types all return {@code null} so callers can treat them conservatively
	 * rather than trust a guessed position.
	 */
	static @Nullable JavaType consumedMessageType(J.MemberReference mr) {
		if (
			mr.getType() instanceof JavaType.Parameterized functionalInterface
			&& functionalInterface.getTypeParameters().size() == 1
		) {
			return functionalInterface.getTypeParameters().get(0);
		}
		return null;
	}

	/**
	 * For {@code log(Priority, Object, ..)} overloads the message is the argument immediately following
	 * the {@code Priority}. Covers {@code log(Priority, Object)}, {@code log(Priority, Object, Throwable)},
	 * and {@code log(String, Priority, Object, Throwable)}.
	 */
	private static @Nullable Expression logMessageArgument(J.MethodInvocation m) {
		List<Expression> arguments = m.getArguments();
		for (int i = 0; i < arguments.size() - 1; i++) {
			if (TypeUtils.isAssignableTo(LOG4J_PRIORITY, arguments.get(i).getType())) {
				return arguments.get(i + 1);
			}
		}
		return null;
	}

	private static boolean matchesAny(List<MethodMatcher> matchers, Expression expression) {
		for (MethodMatcher matcher : matchers) {
			if (matcher.matches(expression)) {
				return true;
			}
		}
		return false;
	}
}
