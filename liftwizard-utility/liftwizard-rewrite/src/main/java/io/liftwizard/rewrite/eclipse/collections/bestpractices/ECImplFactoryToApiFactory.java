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

import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECImplFactoryToApiFactory extends Recipe {

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
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ECImplFactoryToApiFactoryVisitor();
	}

	private static final class ECImplFactoryToApiFactoryVisitor extends JavaVisitor<ExecutionContext> {

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

			String apiClassName = implClassName.replace(".impl.factory.", ".api.factory.");
			JavaType.FullyQualified apiType = JavaType.ShallowClass.build(apiClassName);

			J.Identifier newTarget = id.withType(apiType);

			maybeRemoveImport(implClassName);
			maybeAddImport(apiClassName);

			return fa.withTarget(newTarget);
		}
	}
}
