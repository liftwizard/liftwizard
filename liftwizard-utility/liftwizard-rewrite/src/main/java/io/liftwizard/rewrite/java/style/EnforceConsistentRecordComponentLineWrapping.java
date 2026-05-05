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
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Statement;

/**
 * Enforces consistent line wrapping in record component lists.
 *
 * <p>When a record declaration has multiple components and ANY component is on a different
 * line than the opening parenthesis, this recipe ensures ALL components are
 * line-wrapped (one per line) for consistency.
 *
 * <p>Single-line record declarations are left unchanged. Records with only one component
 * are also left unchanged. Valid fixed-multiple groupings (e.g., key-value pairs)
 * are also left unchanged. Non-record class declarations are left unchanged.
 */
public class EnforceConsistentRecordComponentLineWrapping extends AbstractEnforceConsistentLineWrapping {

	@Override
	public String getDisplayName() {
		return "Enforce consistent record component line wrapping";
	}

	@Override
	public String getDescription() {
		return (
			"When a record declaration has multiple components and any component is line-wrapped, "
			+ "enforce that all components use line wrapping for consistency. "
			+ "Valid fixed-multiple groupings like key-value pairs are left unchanged."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			@Override
			public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
				J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

				if (cd.getKind() != J.ClassDeclaration.Kind.Type.Record) {
					return cd;
				}

				JContainer<Statement> primaryConstructor = cd.getPadding().getPrimaryConstructor();
				if (primaryConstructor == null) {
					return cd;
				}

				List<JRightPadded<Statement>> components = primaryConstructor.getPadding().getElements();

				if (!shouldEnforceWrapping(components)) {
					return cd;
				}

				String wrappedIndent = findWrappedIndent(components);
				if (wrappedIndent == null) {
					return cd;
				}

				List<JRightPadded<Statement>> newComponents = applyWrapping(components, wrappedIndent);
				JContainer<Statement> newContainer = primaryConstructor.getPadding().withElements(newComponents);
				return cd.getPadding().withPrimaryConstructor(newContainer);
			}
		};
	}
}
