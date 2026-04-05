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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Forked from {@code org.openrewrite.java.logging.ParameterizedLogging} to exclude the
 * transformation that converts simple object logging calls like {@code LOGGER.info(myObject)}
 * to {@code LOGGER.info("{}", myObject)}. That transformation is incorrect because
 * {@code LOGGER.info(Object)} already calls {@code toString()} on the argument, and the
 * parameterized form changes the method signature unnecessarily.
 *
 * <p>All other transformations are preserved, including string concatenation to parameterized logging.
 */
public final class ParameterizedLogging extends Recipe {

	private final String methodPattern;
	private final @Nullable Boolean removeToString;

	public ParameterizedLogging(String methodPattern, @Nullable Boolean removeToString) {
		this.methodPattern = methodPattern;
		this.removeToString = removeToString;
	}

	@Override
	public String getDisplayName() {
		return "Parameterize logging statements (excluding simple object arguments)";
	}

	@Override
	public String getDescription() {
		return (
			"Transform logging statements using concatenation for messages and variables into a parameterized format. "
			+ "For example, `logger.info(\"hi \" + userName)` becomes `logger.info(\"hi {}\", userName)`. "
			+ "Unlike the upstream recipe, this version does not transform simple object arguments like "
			+ "`LOGGER.info(myObject)` to `LOGGER.info(\"{}\", myObject)`."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesMethod<>(this.methodPattern, true),
			new ParameterizedLoggingVisitor(this.methodPattern, this.removeToString)
		);
	}

	private static MessageAndArguments concatenationToLiteral(Expression message, MessageAndArguments result) {
		if (!(message instanceof J.Binary)) {
			result.arguments.add(message);
			return result;
		}

		var concat = (J.Binary) message;
		if (
			concat.getLeft() instanceof J.Binary
			&& ((J.Binary) concat.getLeft()).getOperator() == J.Binary.Type.Addition
		) {
			concatenationToLiteral(concat.getLeft(), result);
		} else if (concat.getLeft() instanceof J.Literal left) {
			result.message = getLiteralValue(left) + result.message;
			result.previousMessageWasStringLiteral = left.getType() == JavaType.Primitive.String;
		} else {
			result.message = "{}" + result.message;
			result.arguments.add(concat.getLeft());
			result.previousMessageWasStringLiteral = false;
		}

		if (
			concat.getRight() instanceof J.Binary
			&& ((J.Binary) concat.getRight()).getOperator() == J.Binary.Type.Addition
		) {
			concatenationToLiteral(concat.getRight(), result);
		} else if (concat.getRight() instanceof J.Literal right) {
			boolean rightIsStringLiteral = right.getType() == JavaType.Primitive.String;
			if (result.previousMessageWasStringLiteral && rightIsStringLiteral) {
				result.message += "\" +" + right.getPrefix().getWhitespace() + "\"" + getLiteralValue(right);
			} else {
				result.message += getLiteralValue(right);
			}
			result.previousMessageWasStringLiteral = rightIsStringLiteral;
		} else {
			// Prevent inadvertently appending {} to # to create #{}, which creates an additional JavaTemplate argument
			if (result.message.endsWith("#")) {
				result.message += "\\";
			}
			result.message += "{}";
			result.arguments.add(concat.getRight());
			result.previousMessageWasStringLiteral = false;
		}

		return result;
	}

	private static @Nullable Object getLiteralValue(J.Literal literal) {
		if (literal.getValueSource() == null || literal.getType() != JavaType.Primitive.String) {
			return literal.getValue();
		}
		return literal.getValueSource().substring(1, literal.getValueSource().length() - 1).replace("\\", "\\\\");
	}

	private static String escapeDollarSign(String value) {
		return value.replaceAll("\\$", "\\\\\\$");
	}

	private static final class ParameterizedLoggingVisitor extends JavaIsoVisitor<ExecutionContext> {

		private final MethodMatcher matcher;
		private final @Nullable Boolean removeToString;
		private final RemoveToStringVisitor removeToStringVisitor = new RemoveToStringVisitor();

		private ParameterizedLoggingVisitor(String methodPattern, @Nullable Boolean removeToString) {
			this.matcher = new MethodMatcher(methodPattern, true);
			this.removeToString = removeToString;
		}

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
			if (!this.matcher.matches(m) || m.getArguments().isEmpty() || m.getArguments().get(0) instanceof J.Empty) {
				return m;
			}

			int logMsgIndex = isMarker(m.getArguments().get(0)) ? 1 : 0;

			// Only process if we have at most 2 arguments after accounting for marker
			if (m.getArguments().size() - logMsgIndex > 2) {
				return m;
			}

			Expression logMsg = m.getArguments().get(logMsgIndex);
			if (logMsg instanceof J.Binary) {
				var messageBuilder = new StringBuilder();
				List<Expression> concatenationArgs = new ArrayList<>();
				List<Expression> regularArgs = new ArrayList<>();
				Expression possibleThrowable = null;

				// Process all arguments
				for (int index = 0; index < m.getArguments().size(); index++) {
					Expression arg = m.getArguments().get(index);
					if (index == logMsgIndex && arg instanceof J.Binary) {
						MessageAndArguments literalAndArgs = concatenationToLiteral(
							arg,
							new MessageAndArguments("", new ArrayList<>())
						);
						concatenationArgs.addAll(literalAndArgs.arguments);
					} else if (
						index == m.getArguments().size() - 1
						&& TypeUtils.isAssignableTo("java.lang.Throwable", arg.getType())
					) {
						possibleThrowable = arg;
					} else {
						regularArgs.add(arg);
					}
				}

				// Skip parameterization when throwables are concatenated
				boolean hasThrowableInConcatenation = concatenationArgs
					.stream()
					.anyMatch((arg) -> TypeUtils.isAssignableTo("java.lang.Throwable", arg.getType()));
				if (hasThrowableInConcatenation) {
					return m;
				}

				// Build the message template
				ListUtils.map(m.getArguments(), (index, message) -> {
					if (index > 0) {
						messageBuilder.append(", ");
					}
					if (index == logMsgIndex && message instanceof J.Binary) {
						messageBuilder.append("\"");
						MessageAndArguments literalAndArgs = concatenationToLiteral(
							message,
							new MessageAndArguments("", new ArrayList<>())
						);
						messageBuilder.append(literalAndArgs.message);
						messageBuilder.append("\"");
						literalAndArgs.arguments.forEach((arg) -> messageBuilder.append(", #{any()}"));
					} else {
						messageBuilder.append("#{any()}");
					}
					return message;
				});

				// Assemble arguments in correct order: regular args, concatenation args, throwable
				List<Expression> newArgList = new ArrayList<>(regularArgs);
				newArgList.addAll(concatenationArgs);
				if (possibleThrowable != null) {
					newArgList.add(possibleThrowable);
				}

				m = JavaTemplate.apply(
					escapeDollarSign(messageBuilder.toString()),
					new Cursor(this.getCursor().getParent(), m),
					m.getCoordinates().replaceArguments(),
					newArgList.toArray()
				);
			}
			// NOTE: The upstream recipe has an else-if block here that transforms simple object
			// arguments (e.g., LOGGER.info(myObject)) to parameterized form (LOGGER.info("{}", myObject)).
			// This transformation is intentionally excluded because it silently destroys structured
			// logging: Log4j 1's info(Object) allows appenders to inspect the argument via instanceof,
			// while info("{}", myObject) reduces it to a formatted string.
			// See DoesNotUseLog4j1ObjectLogging for the precondition that skips files with this pattern.

			if (Boolean.TRUE.equals(this.removeToString)) {
				m = m.withArguments(
					ListUtils.map(m.getArguments(), (arg) ->
						(Expression) this.removeToStringVisitor.visitNonNull(arg, ctx, this.getCursor())
					)
				);
			}

			// Avoid changing reference if the templating didn't actually change the contents
			if (
				m != method
				&& m
					.print(this.getCursor().getParentTreeCursor())
					.equals(method.print(this.getCursor().getParentTreeCursor()))
			) {
				return method;
			}
			return m;
		}

		private static boolean isMarker(Expression expression) {
			JavaType expressionType = expression.getType();
			return (
				TypeUtils.isAssignableTo("org.slf4j.Marker", expressionType)
				|| TypeUtils.isAssignableTo("org.apache.logging.log4j.Marker", expressionType)
				|| TypeUtils.isAssignableTo("java.lang.System.Logger.Level", expressionType)
				|| TypeUtils.isAssignableTo("java.util.logging.Level", expressionType)
			);
		}
	}

	private static final class RemoveToStringVisitor extends JavaVisitor<ExecutionContext> {

		private static final MethodMatcher TO_STRING = new MethodMatcher("*..* toString()");

		@Override
		public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			if (this.getCursor().getNearestMessage("DO_NOT_REMOVE", Boolean.FALSE)) {
				return method;
			}
			if (TO_STRING.matches(method.getSelect())) {
				this.getCursor().putMessage("DO_NOT_REMOVE", Boolean.TRUE);
			} else if (TO_STRING.matches(method) && method.getSelect() != null) {
				return method.getSelect().withPrefix(method.getPrefix());
			}
			return super.visitMethodInvocation(method, ctx);
		}
	}

	private static final class MessageAndArguments {

		private final List<Expression> arguments;
		private String message;
		private boolean previousMessageWasStringLiteral;

		private MessageAndArguments(String message, List<Expression> arguments) {
			this.message = message;
			this.arguments = arguments;
		}
	}
}
