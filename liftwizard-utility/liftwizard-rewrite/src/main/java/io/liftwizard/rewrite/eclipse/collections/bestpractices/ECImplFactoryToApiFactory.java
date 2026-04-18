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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECImplFactoryToApiFactory extends ScanningRecipe<Map<Path, Set<String>>> {

	private static final Set<String> IMPL_FACTORY_CLASSES = Set.of(
		"org.eclipse.collections.impl.factory.Lists",
		"org.eclipse.collections.impl.factory.Sets",
		"org.eclipse.collections.impl.factory.Maps",
		"org.eclipse.collections.impl.factory.Bags",
		"org.eclipse.collections.impl.factory.Stacks",
		"org.eclipse.collections.impl.factory.SortedSets",
		"org.eclipse.collections.impl.factory.SortedMaps",
		"org.eclipse.collections.impl.factory.SortedBags"
	);

	private static final Set<String> FACTORY_FIELDS = Set.of("mutable", "immutable", "fixedSize");

	@Override
	public String getDisplayName() {
		return "Replace impl.factory with api.factory";
	}

	@Override
	public String getDescription() {
		return (
			"Replace org.eclipse.collections.impl.factory.* factory field access "
			+ "with api.factory equivalents. Does not transform static utility methods like "
			+ "Sets.union(), Lists.adapt(), etc."
		);
	}

	@Override
	public Map<Path, Set<String>> getInitialValue(ExecutionContext ctx) {
		return new HashMap<>();
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getScanner(Map<Path, Set<String>> acc) {
		return new JavaIsoVisitor<ExecutionContext>() {
			@Override
			public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
				if (
					!FACTORY_FIELDS.contains(fieldAccess.getSimpleName())
					&& fieldAccess.getTarget() instanceof J.Identifier id
				) {
					addIfImplFactory(id, acc);
				}
				return super.visitFieldAccess(fieldAccess, ctx);
			}

			@Override
			public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
				if (method.getSelect() instanceof J.Identifier id) {
					addIfImplFactory(id, acc);
				}
				return super.visitMethodInvocation(method, ctx);
			}

			private void addIfImplFactory(J.Identifier id, Map<Path, Set<String>> acc) {
				JavaType.FullyQualified type = TypeUtils.asFullyQualified(id.getType());
				if (type == null || !IMPL_FACTORY_CLASSES.contains(type.getFullyQualifiedName())) {
					return;
				}
				Path sourcePath = getCursor().firstEnclosingOrThrow(J.CompilationUnit.class).getSourcePath();
				acc.computeIfAbsent(sourcePath, (k) -> new HashSet<>()).add(type.getFullyQualifiedName());
			}
		};
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor(Map<Path, Set<String>> acc) {
		return new ECImplFactoryToApiFactoryVisitor(acc);
	}

	private static final class ECImplFactoryToApiFactoryVisitor extends JavaVisitor<ExecutionContext> {

		private final Map<Path, Set<String>> nonFactoryByPath;

		private ECImplFactoryToApiFactoryVisitor(Map<Path, Set<String>> nonFactoryByPath) {
			this.nonFactoryByPath = nonFactoryByPath;
		}

		@Override
		public J visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
			var fa = (J.FieldAccess) super.visitFieldAccess(fieldAccess, ctx);

			String fieldName = fa.getSimpleName();
			if (!FACTORY_FIELDS.contains(fieldName)) {
				return fa;
			}

			Expression target = fa.getTarget();
			if (!(target instanceof J.Identifier id)) {
				return fa;
			}

			JavaType.FullyQualified targetType = TypeUtils.asFullyQualified(id.getType());
			if (targetType == null) {
				return fa;
			}

			String implClassName = targetType.getFullyQualifiedName();
			if (!IMPL_FACTORY_CLASSES.contains(implClassName)) {
				return fa;
			}

			String implPackage = implClassName.substring(0, implClassName.lastIndexOf('.'));
			J.CompilationUnit cu = getCursor().firstEnclosingOrThrow(J.CompilationUnit.class);
			if (cu.getPackageDeclaration() != null) {
				String filePackage = cu.getPackageDeclaration().getExpression().printTrimmed(getCursor());
				if (implPackage.equals(filePackage)) {
					return fa;
				}
			}

			Set<String> nonFactoryClasses = this.nonFactoryByPath.getOrDefault(cu.getSourcePath(), Set.of());
			if (nonFactoryClasses.contains(implClassName)) {
				return fa;
			}

			String apiClassName = implClassName.replace(".impl.factory.", ".api.factory.");
			JavaType.FullyQualified apiType = JavaType.ShallowClass.build(apiClassName);

			J.Identifier newTarget = id.withType(apiType);

			maybeRemoveImport(implClassName);
			maybeAddImport(apiClassName);

			return fa.withTarget(newTarget);
		}
	}
}
