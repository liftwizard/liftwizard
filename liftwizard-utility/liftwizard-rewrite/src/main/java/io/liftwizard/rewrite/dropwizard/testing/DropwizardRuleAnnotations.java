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

package io.liftwizard.rewrite.dropwizard.testing;

import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class DropwizardRuleAnnotations extends Recipe {

	@Option(
		displayName = "Extension type",
		description = "Fully qualified name of the JUnit 5 extension type to match on fields.",
		example = "io.dropwizard.testing.junit5.DropwizardClientExtension"
	)
	private final String extensionTypeFqn;

	@JsonCreator
	public DropwizardRuleAnnotations(@JsonProperty("extensionTypeFqn") String extensionTypeFqn) {
		this.extensionTypeFqn = extensionTypeFqn;
	}

	@Override
	public String getDisplayName() {
		return "Replace `@ClassRule`/`@Rule` with `@RegisterExtension` and add `@ExtendWith`";
	}

	@Override
	public String getDescription() {
		return "Replace JUnit 4 `@ClassRule`/`@Rule` annotations with JUnit 5 `@RegisterExtension` "
			+ "and add `@ExtendWith(DropwizardExtensionsSupport.class)` to the test class.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesType<>(this.extensionTypeFqn, false),
			new DropwizardRuleAnnotationsVisitor(this.extensionTypeFqn)
		);
	}

	private static final class DropwizardRuleAnnotationsVisitor extends JavaIsoVisitor<ExecutionContext> {

		private final String extensionTypeFqn;

		private DropwizardRuleAnnotationsVisitor(String extensionTypeFqn) {
			this.extensionTypeFqn = Objects.requireNonNull(extensionTypeFqn);
		}

		@Override
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
			J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

			boolean hasExtensionField = cd.getBody().getStatements().stream()
				.filter(J.VariableDeclarations.class::isInstance)
				.map(J.VariableDeclarations.class::cast)
				.anyMatch(this::isExtensionType);

			if (!hasExtensionField) {
				return cd;
			}

			boolean alreadyHasExtendWith = cd.getLeadingAnnotations().stream()
				.anyMatch(this::isExtendWithDropwizardExtensionsSupport);

			if (alreadyHasExtendWith) {
				return cd;
			}

			this.maybeAddImport("org.junit.jupiter.api.extension.ExtendWith");
			this.maybeAddImport("io.dropwizard.testing.junit5.DropwizardExtensionsSupport");

			this.updateCursor(cd);

			return JavaTemplate.builder("@ExtendWith(DropwizardExtensionsSupport.class)")
				.imports(
					"org.junit.jupiter.api.extension.ExtendWith",
					"io.dropwizard.testing.junit5.DropwizardExtensionsSupport"
				)
				.javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api").dependsOn(
					"""
					package io.dropwizard.testing.junit5;

					public class DropwizardExtensionsSupport {
					}
					"""
				))
				.build()
				.apply(this.getCursor(), cd.getCoordinates().addAnnotation(Comparator.comparing(
					J.Annotation::getSimpleName
				)));
		}

		@Override
		public J.VariableDeclarations visitVariableDeclarations(
			J.VariableDeclarations multiVariable,
			ExecutionContext ctx
		) {
			J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

			if (!this.isExtensionType(vd)) {
				return vd;
			}

			boolean hasClassRule = this.hasAnnotationByName(vd, "ClassRule");
			boolean hasRule = this.hasAnnotationByName(vd, "Rule");

			if (!hasClassRule && !hasRule) {
				return vd;
			}

			if (this.hasAnnotationByName(vd, "RegisterExtension")) {
				return vd;
			}

			vd = vd.withLeadingAnnotations(
				vd.getLeadingAnnotations().stream()
					.filter((ann) ->
						!"ClassRule".equals(ann.getSimpleName())
							&& !"Rule".equals(ann.getSimpleName()))
					.toList()
			);

			this.maybeRemoveImport("org.junit.ClassRule");
			this.maybeRemoveImport("org.junit.Rule");
			this.maybeAddImport("org.junit.jupiter.api.extension.RegisterExtension");

			this.updateCursor(vd);

			return JavaTemplate.builder("@RegisterExtension")
				.imports("org.junit.jupiter.api.extension.RegisterExtension")
				.javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"))
				.build()
				.apply(this.getCursor(), vd.getCoordinates().addAnnotation(Comparator.comparing(
					J.Annotation::getSimpleName
				)));
		}

		private boolean hasAnnotationByName(J.VariableDeclarations vd, String simpleName) {
			return vd.getLeadingAnnotations().stream()
				.anyMatch((ann) -> simpleName.equals(ann.getSimpleName()));
		}

		private boolean isExtensionType(J.VariableDeclarations vd) {
			J typeExpr = vd.getTypeExpression();
			if (typeExpr == null) {
				return false;
			}
			return this.matchesExtensionType(typeExpr);
		}

		private boolean matchesExtensionType(J tree) {
			if (tree instanceof J.Identifier identifier) {
				JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
				return type != null && this.extensionTypeFqn.equals(type.getFullyQualifiedName());
			}
			if (tree instanceof J.ParameterizedType paramType) {
				return this.matchesExtensionType(paramType.getClazz());
			}
			return false;
		}

		private boolean isExtendWithDropwizardExtensionsSupport(J.Annotation annotation) {
			JavaType.FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
			if (type == null || !"org.junit.jupiter.api.extension.ExtendWith".equals(type.getFullyQualifiedName())) {
				return false;
			}
			if (annotation.getArguments() == null) {
				return false;
			}
			return annotation.getArguments().stream()
				.filter(J.FieldAccess.class::isInstance)
				.map(J.FieldAccess.class::cast)
				.filter((fa) -> "class".equals(fa.getSimpleName()))
				.anyMatch((fa) -> {
					JavaType targetType = fa.getTarget().getType();
					return TypeUtils.isOfClassType(
						targetType,
						"io.dropwizard.testing.junit5.DropwizardExtensionsSupport"
					);
				});
		}
	}
}
