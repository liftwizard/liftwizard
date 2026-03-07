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

package io.liftwizard.rewrite.java.style;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;

/**
 * Enforces consistent line wrapping in method call arguments.
 *
 * <p>When a method call has multiple arguments and ANY argument is on a different
 * line than the opening parenthesis, this recipe ensures ALL arguments are
 * line-wrapped (one per line) for consistency.
 *
 * <p>Single-line method calls are left unchanged. Calls with only one argument
 * are also left unchanged.
 */
public class EnforceConsistentArgumentLineWrapping extends Recipe {

	@Override
	public String getDisplayName() {
		return "Enforce consistent argument line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When a method call has multiple arguments and any argument is line-wrapped, "
			+ "enforce that all arguments use line wrapping for consistency."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new EnforceConsistentArgumentLineWrappingVisitor();
	}

	private static final class EnforceConsistentArgumentLineWrappingVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

			JContainer<Expression> argsContainer = m.getPadding().getArguments();
			List<JRightPadded<Expression>> args = argsContainer.getPadding().getElements();

			// Need at least 2 arguments to enforce consistency
			if (args.size() < 2) {
				return m;
			}

			boolean anyWrapped = false;
			boolean allWrapped = true;

			for (JRightPadded<Expression> arg : args) {
				if (containsNewline(arg.getElement().getPrefix())) {
					anyWrapped = true;
				} else {
					allWrapped = false;
				}
			}

			// If no arguments are wrapped, or all are already wrapped, nothing to do
			if (!anyWrapped || allWrapped) {
				return m;
			}

			// Find the indentation to use: use the indentation from the first wrapped argument
			String wrappedIndent = null;
			for (JRightPadded<Expression> arg : args) {
				Space prefix = arg.getElement().getPrefix();
				if (containsNewline(prefix)) {
					wrappedIndent = extractIndentAfterLastNewline(prefix.getWhitespace());
					break;
				}
			}

			if (wrappedIndent == null) {
				return m;
			}

			// Apply wrapping to all arguments
			String newlineAndIndent = "\n" + wrappedIndent;
			List<JRightPadded<Expression>> newArgs = new java.util.ArrayList<>(args.size());

			for (JRightPadded<Expression> arg : args) {
				Expression element = arg.getElement();
				Space prefix = element.getPrefix();

				if (!containsNewline(prefix)) {
					// Preserve any comments from the original prefix
					Space newPrefix = prefix.withWhitespace(newlineAndIndent);
					newArgs.add(arg.withElement(element.withPrefix(newPrefix)));
				} else {
					newArgs.add(arg);
				}
			}

			JContainer<Expression> newContainer = argsContainer.getPadding().withElements(newArgs);
			return m.getPadding().withArguments(newContainer);
		}

		private static boolean containsNewline(Space space) {
			return space.getWhitespace().contains("\n");
		}

		private static String extractIndentAfterLastNewline(String whitespace) {
			int lastNewline = whitespace.lastIndexOf('\n');
			if (lastNewline < 0) {
				return whitespace;
			}
			return whitespace.substring(lastNewline + 1);
		}
	}
}
