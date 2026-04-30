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

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Statement;

/**
 * Enforces consistent line wrapping in method declaration parameters.
 *
 * <p>When a method declaration has multiple parameters and ANY parameter is on a different
 * line than the opening parenthesis, this recipe ensures ALL parameters are
 * line-wrapped (one per line) for consistency.
 *
 * <p>Single-line method declarations are left unchanged. Declarations with only one parameter
 * are also left unchanged. Valid fixed-multiple groupings are also left unchanged.
 */
public class EnforceConsistentMethodParameterLineWrapping extends AbstractEnforceConsistentLineWrapping {

	@Override
	public String getDisplayName() {
		return "Enforce consistent method parameter line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When a method declaration has multiple parameters and any parameter is line-wrapped, "
			+ "enforce that all parameters use line wrapping for consistency. "
			+ "Valid fixed-multiple groupings like key-value pairs are left unchanged."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			@Override
			public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
				J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

				JContainer<Statement> paramsContainer = m.getPadding().getParameters();
				List<JRightPadded<Statement>> params = paramsContainer.getPadding().getElements();

				if (!shouldEnforceWrapping(params)) {
					return m;
				}

				String wrappedIndent = findWrappedIndent(params);
				if (wrappedIndent == null) {
					return m;
				}

				List<JRightPadded<Statement>> newParams = applyWrapping(params, wrappedIndent);
				newParams = normalizeInternalNewlines(newParams);
				JContainer<Statement> newContainer = paramsContainer.getPadding().withElements(newParams);
				return m.getPadding().withParameters(newContainer);
			}
		};
	}

	private static List<JRightPadded<Statement>> normalizeInternalNewlines(List<JRightPadded<Statement>> params) {
		List<JRightPadded<Statement>> result = new ArrayList<>(params.size());
		for (JRightPadded<Statement> param : params) {
			result.add(normalizeParam(param));
		}
		return result;
	}

	private static JRightPadded<Statement> normalizeParam(JRightPadded<Statement> param) {
		if (param.getElement() instanceof J.VariableDeclarations varDecls) {
			J.VariableDeclarations normalized = normalizeVariableDeclaration(varDecls);
			if (normalized != varDecls) {
				return param.withElement(normalized);
			}
		}
		return param;
	}

	private static J.VariableDeclarations normalizeVariableDeclaration(J.VariableDeclarations varDecls) {
		List<JRightPadded<J.VariableDeclarations.NamedVariable>> paddedVars = varDecls.getPadding().getVariables();
		for (int i = 0; i < paddedVars.size(); i++) {
			JRightPadded<J.VariableDeclarations.NamedVariable> paddedVar = paddedVars.get(i);
			J.VariableDeclarations.NamedVariable namedVar = paddedVar.getElement();
			if (containsNewline(namedVar.getPrefix())) {
				namedVar = namedVar.withPrefix(namedVar.getPrefix().withWhitespace(" "));
				List<JRightPadded<J.VariableDeclarations.NamedVariable>> newVars = new ArrayList<>(paddedVars);
				newVars.set(i, paddedVar.withElement(namedVar));
				return varDecls.getPadding().withVariables(newVars);
			}
		}
		return varDecls;
	}
}
