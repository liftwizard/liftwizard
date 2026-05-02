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

package io.liftwizard.rewrite.dropwizard.migration;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

public class Dropwizard3IAccessEventGetSequenceNumber extends Recipe {

	private static final String I_ACCESS_EVENT = "ch.qos.logback.access.spi.IAccessEvent";
	private static final String I_ACCESS_EVENT_STUB = """
		package ch.qos.logback.access.spi;

		public interface IAccessEvent {
		    long getSequenceNumber();
		}
		""";

	@Override
	public String getDisplayName() {
		return "Add getSequenceNumber() stub to IAccessEvent implementations";
	}

	@Override
	public String getDescription() {
		return (
			"Logback 1.5.x added `long getSequenceNumber()` to `ch.qos.logback.access.spi.IAccessEvent`. "
			+ "Classes implementing IAccessEvent without overriding it will fail to compile. "
			+ "This recipe inserts an UnsupportedOperationException stub for any concrete IAccessEvent "
			+ "implementation that is missing the method, matching the convention used by stub/fake "
			+ "test implementations."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesType<>(I_ACCESS_EVENT, false), new GetSequenceNumberVisitor());
	}

	private static final class GetSequenceNumberVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
			J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

			if (cd.getKind() == J.ClassDeclaration.Kind.Type.Interface) {
				return cd;
			}
			if (!implementsIAccessEvent(cd)) {
				return cd;
			}
			if (alreadyDeclaresGetSequenceNumber(cd)) {
				return cd;
			}

			return JavaTemplate.builder(
				"""
				@Override
				public long getSequenceNumber() {
				    throw new UnsupportedOperationException(
				        this.getClass().getSimpleName() + ".getSequenceNumber() not implemented yet"
				    );
				}
				"""
			)
				.javaParser(JavaParser.fromJavaVersion().dependsOn(I_ACCESS_EVENT_STUB))
				.build()
				.apply(this.updateCursor(cd), cd.getBody().getCoordinates().lastStatement());
		}

		private static boolean implementsIAccessEvent(J.ClassDeclaration cd) {
			if (cd.getImplements() == null) {
				return false;
			}
			return cd
				.getImplements()
				.stream()
				.anyMatch((typeTree) -> TypeUtils.isOfClassType(typeTree.getType(), I_ACCESS_EVENT));
		}

		private static boolean alreadyDeclaresGetSequenceNumber(J.ClassDeclaration cd) {
			return cd
				.getBody()
				.getStatements()
				.stream()
				.filter(J.MethodDeclaration.class::isInstance)
				.map(J.MethodDeclaration.class::cast)
				.anyMatch(GetSequenceNumberVisitor::isGetSequenceNumber);
		}

		private static boolean isGetSequenceNumber(J.MethodDeclaration method) {
			return "getSequenceNumber".equals(method.getSimpleName()) && method.getParameters().size() == 1;
		}
	}
}
