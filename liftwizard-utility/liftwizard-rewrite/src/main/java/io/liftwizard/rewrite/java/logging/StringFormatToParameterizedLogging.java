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

package io.liftwizard.rewrite.java.logging;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class StringFormatToParameterizedLogging extends Recipe {

	private static final Set<String> LOGGER_METHODS = Set.of("trace", "debug", "info", "warn", "error");
	private static final Pattern SIMPLE_FORMAT_SPECIFIER = Pattern.compile("%[sdxofbc]");
	private static final Pattern COMPLEX_FORMAT_PATTERN = Pattern.compile("%[0-9$+\\-#, (]+[sdxofbc]|%%|%n");

	@Override
	public String getDisplayName() {
		return "String.format() in logging → SLF4J parameterized logging";
	}

	@Override
	public String getDescription() {
		return "Replace String.format() calls in SLF4J logging with parameterized placeholders for improved performance.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new StringFormatLoggingVisitor();
	}

	private static final class StringFormatLoggingVisitor extends JavaVisitor<ExecutionContext> {

		@Override
		public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

			if (!this.isLoggerMethod(m)) {
				return m;
			}

			List<Expression> args = m.getArguments();
			int formatArgIndex = this.findFormatArgumentIndex(args);

			if (formatArgIndex < 0 || formatArgIndex >= args.size()) {
				return m;
			}

			Expression formatArg = args.get(formatArgIndex);
			if (!(formatArg instanceof J.MethodInvocation formatCall)) {
				return m;
			}

			if (!this.isStringFormatCall(formatCall)) {
				return m;
			}

			List<Expression> formatArgs = formatCall.getArguments();
			if (formatArgs.isEmpty() || !(formatArgs.get(0) instanceof J.Literal formatLiteral)) {
				return m;
			}

			if (formatLiteral.getValue() == null || !(formatLiteral.getValue() instanceof String formatString)) {
				return m;
			}

			if (!this.isSimpleFormatString(formatString)) {
				return m;
			}

			int specifierCount = this.countSimpleFormatSpecifiers(formatString);

			if (formatArgs.size() != specifierCount + 1) {
				return m;
			}

			String slf4jTemplate = this.convertToSlf4jTemplate(formatString);

			List<Expression> newArgs = this.buildNewArguments(args, formatArgIndex, slf4jTemplate, formatArgs);

			return m.withArguments(newArgs);
		}

		private boolean isLoggerMethod(J.MethodInvocation m) {
			String methodName = m.getSimpleName();
			return LOGGER_METHODS.contains(methodName) && this.isSlf4jLogger(m.getSelect());
		}

		private boolean isSlf4jLogger(Expression select) {
			if (select == null) {
				return false;
			}
			JavaType.FullyQualified type = TypeUtils.asFullyQualified(select.getType());
			return type != null && "org.slf4j.Logger".equals(type.getFullyQualifiedName());
		}

		private int findFormatArgumentIndex(List<Expression> args) {
			if (args.isEmpty()) {
				return -1;
			}

			if (this.isMarkerType(args.get(0))) {
				return 1;
			}

			return 0;
		}

		private boolean isMarkerType(Expression expr) {
			JavaType.FullyQualified type = TypeUtils.asFullyQualified(expr.getType());
			return type != null && "org.slf4j.Marker".equals(type.getFullyQualifiedName());
		}

		private boolean isStringFormatCall(J.MethodInvocation call) {
			if (!"format".equals(call.getSimpleName())) {
				return false;
			}

			if (!(call.getSelect() instanceof J.Identifier id)) {
				return false;
			}

			return "String".equals(id.getSimpleName());
		}

		private boolean isSimpleFormatString(String format) {
			if (COMPLEX_FORMAT_PATTERN.matcher(format).find()) {
				return false;
			}

			return SIMPLE_FORMAT_SPECIFIER.matcher(format).find();
		}

		private int countSimpleFormatSpecifiers(String format) {
			Matcher matcher = SIMPLE_FORMAT_SPECIFIER.matcher(format);
			int count = 0;
			while (matcher.find()) {
				count++;
			}
			return count;
		}

		private String convertToSlf4jTemplate(String formatString) {
			return formatString.replaceAll("%[sdxofbc]", "{}");
		}

		private List<Expression> buildNewArguments(
			List<Expression> loggerArgs,
			int formatArgIndex,
			String slf4jTemplate,
			List<Expression> formatArgs
		) {
			List<Expression> newArgs = Lists.mutable.empty();

			for (int i = 0; i < formatArgIndex; i++) {
				newArgs.add(loggerArgs.get(i));
			}

			Expression originalFormatArg = loggerArgs.get(formatArgIndex);
			J.Literal templateLiteral = new J.Literal(
				originalFormatArg.getId(),
				originalFormatArg.getPrefix(),
				originalFormatArg.getMarkers(),
				slf4jTemplate,
				"\"" + slf4jTemplate.replace("\\", "\\\\").replace("\"", "\\\"") + "\"",
				null,
				JavaType.Primitive.String
			);
			newArgs.add(templateLiteral);

			for (int i = 1; i < formatArgs.size(); i++) {
				newArgs.add(formatArgs.get(i));
			}

			for (int i = formatArgIndex + 1; i < loggerArgs.size(); i++) {
				newArgs.add(loggerArgs.get(i));
			}

			return newArgs;
		}
	}
}
