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
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;

/**
 * Enforces consistent line wrapping in method invocation arguments.
 *
 * <p>When a method call has multiple arguments and ANY argument is on a different
 * line than the opening parenthesis, this recipe ensures ALL arguments are
 * line-wrapped (one per line) for consistency.
 *
 * <p>Single-line method calls are left unchanged. Calls with only one argument
 * are also left unchanged. Valid fixed-multiple groupings (e.g., key-value pairs)
 * are also left unchanged.
 */
public class EnforceConsistentMethodInvocationArgumentLineWrapping extends AbstractEnforceConsistentLineWrapping {

	@Override
	public String getDisplayName() {
		return "Enforce consistent method invocation argument line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When a method call has multiple arguments and any argument is line-wrapped, "
			+ "enforce that all arguments use line wrapping for consistency. "
			+ "Valid fixed-multiple groupings like key-value pairs are left unchanged."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			@Override
			public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
				J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

				JContainer<Expression> argsContainer = m.getPadding().getArguments();
				List<JRightPadded<Expression>> args = argsContainer.getPadding().getElements();

				if (!shouldEnforceWrapping(args)) {
					return m;
				}

				String wrappedIndent = findWrappedIndent(args);
				if (wrappedIndent == null) {
					return m;
				}

				List<JRightPadded<Expression>> newArgs = applyWrapping(args, wrappedIndent);
				JContainer<Expression> newContainer = argsContainer.getPadding().withElements(newArgs);
				return m.getPadding().withArguments(newContainer);
			}
		};
	}
}
