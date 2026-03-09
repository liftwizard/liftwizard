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
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

/**
 * Enforces consistent line wrapping in method declaration parameters.
 *
 * <p>When a method declaration has multiple parameters and ANY parameter is on a different
 * line than the opening parenthesis, this recipe ensures ALL parameters are
 * line-wrapped (one per line) for consistency.
 *
 * <p>Single-line method declarations are left unchanged. Declarations with only one parameter
 * are also left unchanged.
 */
public class EnforceConsistentParameterLineWrapping extends Recipe {

	@Override
	public String getDisplayName() {
		return "Enforce consistent parameter line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When a method declaration has multiple parameters and any parameter is line-wrapped, "
			+ "enforce that all parameters use line wrapping for consistency."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new EnforceConsistentParameterLineWrappingVisitor();
	}

	private static final class EnforceConsistentParameterLineWrappingVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
			J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

			JContainer<Statement> paramsContainer = m.getPadding().getParameters();
			List<JRightPadded<Statement>> params = paramsContainer.getPadding().getElements();

			// Need at least 2 parameters to enforce consistency
			if (params.size() < 2) {
				return m;
			}

			boolean anyWrapped = false;
			boolean allWrapped = true;

			for (JRightPadded<Statement> param : params) {
				if (containsNewline(param.getElement().getPrefix())) {
					anyWrapped = true;
				} else {
					allWrapped = false;
				}
			}

			// If no parameters are wrapped, or all are already wrapped, nothing to do
			if (!anyWrapped || allWrapped) {
				return m;
			}

			// Find the indentation to use: use the indentation from the first wrapped parameter
			String wrappedIndent = null;
			for (JRightPadded<Statement> param : params) {
				Space prefix = param.getElement().getPrefix();
				if (containsNewline(prefix)) {
					wrappedIndent = extractIndentAfterLastNewline(prefix.getWhitespace());
					break;
				}
			}

			if (wrappedIndent == null) {
				return m;
			}

			// Apply wrapping to all parameters
			String newlineAndIndent = "\n" + wrappedIndent;
			List<JRightPadded<Statement>> newParams = new java.util.ArrayList<>(params.size());

			for (JRightPadded<Statement> param : params) {
				Statement element = param.getElement();
				Space prefix = element.getPrefix();

				if (!containsNewline(prefix)) {
					// Preserve any comments from the original prefix
					Space newPrefix = prefix.withWhitespace(newlineAndIndent);
					newParams.add(param.withElement(element.withPrefix(newPrefix)));
				} else {
					newParams.add(param);
				}
			}

			JContainer<Statement> newContainer = paramsContainer.getPadding().withElements(newParams);
			return m.getPadding().withParameters(newContainer);
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
