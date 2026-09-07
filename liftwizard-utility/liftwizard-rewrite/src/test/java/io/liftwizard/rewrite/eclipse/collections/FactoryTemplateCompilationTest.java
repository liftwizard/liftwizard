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

package io.liftwizard.rewrite.eclipse.collections;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.ToolProvider;

import io.liftwizard.rewrite.eclipse.collections.adoption.CollectionsEmptyToFactory;
import io.liftwizard.rewrite.eclipse.collections.adoption.JCFHashMapConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.adoption.JCFHashSetConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.adoption.JCFListConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.adoption.JCFTreeMapConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.adoption.JCFTreeSetConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.bestpractices.ECListConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.bestpractices.ECMapConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.bestpractices.ECSetConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.bestpractices.ECSortedMapConstructorToFactory;
import io.liftwizard.rewrite.eclipse.collections.bestpractices.ECSortedSetConstructorToFactory;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;

class FactoryTemplateCompilationTest extends AbstractEclipseCollectionsTest {

	@TempDir
	Path directory;

	@Test
	void stubMethodsMatchLibraryInterfaces() throws IOException, ReflectiveOperationException {
		MutableList<String> sources = Lists.mutable.empty();
		var parsed = JavaParser.fromJavaVersion()
			.build()
			.parse(EclipseCollectionsTemplateStubs.factories().toArray(String[]::new))
			.toList();
		for (var source : parsed) {
			var compilationUnit = (J.CompilationUnit) source;
			for (J.ClassDeclaration factory : compilationUnit.getClasses()) {
				for (Statement member : factory.getBody().getStatements()) {
					if (!(member instanceof J.ClassDeclaration nested)) {
						continue;
					}
					String fieldName =
						Character.toLowerCase(nested.getSimpleName().charAt(0)) + nested.getSimpleName().substring(1);
					Class<?> factoryClass = Class.forName(factory.getType().getFullyQualifiedName());
					String contract = factoryClass.getField(fieldName).getType().getCanonicalName();
					StringBuilder body = new StringBuilder();
					for (Statement statement : nested.getBody().getStatements()) {
						if (statement instanceof J.MethodDeclaration method) {
							body.append("@Override\n").append(method.printTrimmed()).append('\n');
						}
					}
					sources.add(
						"abstract class FactoryContract"
						+ sources.size()
						+ " implements "
						+ contract
						+ " {\n"
						+ body
						+ "}\n"
					);
				}
			}
		}
		this.assertCompiles(sources);
	}

	@Test
	void transformedFactoriesCompileAgainstLibrary() throws IOException {
		this.assertCompiles(List.of(this.fixture("factories-before.java")));
		this.rewriteRun(
			(spec) ->
				spec.recipes(
					new JCFListConstructorToFactory(),
					new JCFHashMapConstructorToFactory(),
					new JCFHashSetConstructorToFactory(),
					new JCFTreeMapConstructorToFactory(),
					new JCFTreeSetConstructorToFactory(),
					new ECListConstructorToFactory(),
					new ECMapConstructorToFactory(),
					new ECSetConstructorToFactory(),
					new ECSortedMapConstructorToFactory(),
					new ECSortedSetConstructorToFactory(),
					new CollectionsEmptyToFactory()
				),
			java(this.fixture("factories-before.java"), this.fixture("factories-after.java"), (spec) ->
				spec.afterRecipe((cu) -> this.assertCompiles(List.of(cu.printAll())))
			)
		);
	}

	private void assertCompiles(List<String> sources) throws IOException {
		MutableList<String> arguments = Lists.mutable.with(
			"-proc:none",
			"-classpath",
			System.getProperty("java.class.path"),
			"-d",
			this.directory.toString()
		);
		for (int i = 0; i < sources.size(); i++) {
			Path source = this.directory.resolve("FactoryExample" + i + ".java");
			Files.writeString(source, sources.get(i));
			arguments.add(source.toString());
		}
		var diagnostics = new ByteArrayOutputStream();
		int result = ToolProvider.getSystemJavaCompiler().run(
			null,
			diagnostics,
			diagnostics,
			arguments.toArray(String[]::new)
		);
		assertThat(result).withFailMessage(diagnostics.toString(StandardCharsets.UTF_8)).isZero();
	}
}
