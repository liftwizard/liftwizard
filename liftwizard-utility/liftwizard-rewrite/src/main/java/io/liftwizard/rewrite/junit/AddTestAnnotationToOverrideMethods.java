/*
 * Copyright 2026 Craig Motlin
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

package io.liftwizard.rewrite.junit;

import java.util.Comparator;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class AddTestAnnotationToOverrideMethods extends Recipe {

	@Override
	public String getDisplayName() {
		return "Add `@Test` to `@Override` methods whose parent has `@Test`";
	}

	@Override
	public String getDescription() {
		return (
			"Add `@Test` annotation to `@Override` methods in test classes when the "
			+ "overridden parent method has `@Test`. This prevents test methods from "
			+ "being silently skipped when a subclass overrides a parent test method "
			+ "without carrying the `@Test` annotation forward."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new AddTestAnnotationVisitor();
	}

	private static final class AddTestAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
			J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);

			if (!hasAnnotation(md, "Override")) {
				return md;
			}

			if (hasAnnotation(md, "Test")) {
				return md;
			}

			JavaType.Method methodType = md.getMethodType();
			if (methodType == null) {
				return md;
			}

			JavaType.Method overridden = methodType.getOverride();
			if (overridden == null) {
				return md;
			}

			boolean parentHasTest = overridden
				.getAnnotations()
				.stream()
				.anyMatch((a) -> "org.junit.jupiter.api.Test".equals(a.getFullyQualifiedName()));

			if (!parentHasTest) {
				return md;
			}

			this.maybeAddImport("org.junit.jupiter.api.Test");

			this.updateCursor(md);

			return JavaTemplate.builder("@Test")
				.imports("org.junit.jupiter.api.Test")
				.javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"))
				.build()
				.apply(
					this.getCursor(),
					md.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName))
				);
		}

		private static boolean hasAnnotation(J.MethodDeclaration md, String simpleName) {
			return md
				.getLeadingAnnotations()
				.stream()
				.anyMatch((a) -> simpleName.equals(a.getSimpleName()));
		}
	}
}
