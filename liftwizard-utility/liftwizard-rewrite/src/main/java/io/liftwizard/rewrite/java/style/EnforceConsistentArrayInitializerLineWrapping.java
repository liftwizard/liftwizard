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
 * Enforces consistent line wrapping in array initializer elements.
 *
 * <p>When an array initializer ({@code new String[] { ... }} or {@code { ... }})
 * has multiple elements and ANY element is on a different line than the opening
 * brace, this recipe ensures ALL elements are line-wrapped (one per line) for
 * consistency.
 *
 * <p>Single-line array initializers are left unchanged. Initializers with only one
 * element are also left unchanged. Valid fixed-multiple groupings (e.g., key-value
 * pairs) are also left unchanged.
 */
public class EnforceConsistentArrayInitializerLineWrapping extends AbstractEnforceConsistentLineWrapping {

	@Override
	public String getDisplayName() {
		return "Enforce consistent array initializer line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When an array initializer has multiple elements and any element is line-wrapped, "
			+ "enforce that all elements use line wrapping for consistency. "
			+ "Valid fixed-multiple groupings like key-value pairs are left unchanged."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			@Override
			public J.NewArray visitNewArray(J.NewArray newArray, ExecutionContext ctx) {
				J.NewArray na = super.visitNewArray(newArray, ctx);

				JContainer<Expression> initContainer = na.getPadding().getInitializer();
				if (initContainer == null) {
					return na;
				}

				List<JRightPadded<Expression>> elements = initContainer.getPadding().getElements();

				if (!shouldEnforceWrapping(elements)) {
					return na;
				}

				String wrappedIndent = findWrappedIndent(elements);
				if (wrappedIndent == null) {
					return na;
				}

				List<JRightPadded<Expression>> newElements = applyWrapping(elements, wrappedIndent);
				JContainer<Expression> newContainer = initContainer.getPadding().withElements(newElements);
				return na.getPadding().withInitializer(newContainer);
			}
		};
	}
}
