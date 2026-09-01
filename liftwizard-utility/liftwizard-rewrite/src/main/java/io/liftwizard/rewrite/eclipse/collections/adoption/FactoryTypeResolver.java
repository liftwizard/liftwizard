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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import java.util.Optional;

import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;

/**
 * Decides which Eclipse Collections factory class a bare simple name such as {@code Sets} denotes inside a compilation
 * unit, so a recipe emits {@code Sets.mutable.empty()} typed to match the import that is actually in scope instead of
 * a fully-qualified name.
 */
final class FactoryTypeResolver {

	private static final String API_FACTORY_PACKAGE = "org.eclipse.collections.api.factory";
	private static final String IMPL_FACTORY_PACKAGE = "org.eclipse.collections.impl.factory";

	private FactoryTypeResolver() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * Prefers the api factory. Falls back to the impl factory when the compilation unit already imports it, which is
	 * common when the file also calls impl-only utilities such as {@code Sets.union}. Returns empty when the simple
	 * name is bound to some other type, in which case the caller should leave the code alone.
	 */
	static Optional<String> resolve(Cursor cursor, String factorySimpleName) {
		J.CompilationUnit compilationUnit = cursor.firstEnclosingOrThrow(J.CompilationUnit.class);

		// At most one non-static import can bind a simple name, or the compilation unit would not compile.
		Optional<J.Import> boundImport = ListAdapter.adapt(compilationUnit.getImports()).detectOptional(
			(anImport) -> !anImport.isStatic() && getSimpleName(anImport.getTypeName()).equals(factorySimpleName)
		);

		String apiType = API_FACTORY_PACKAGE + "." + factorySimpleName;
		if (boundImport.isEmpty()) {
			return Optional.of(apiType);
		}

		String boundType = boundImport.get().getTypeName();
		String implType = IMPL_FACTORY_PACKAGE + "." + factorySimpleName;
		if (boundType.equals(apiType) || boundType.equals(implType)) {
			return Optional.of(boundType);
		}
		return Optional.empty();
	}

	private static String getSimpleName(String typeName) {
		return typeName.substring(typeName.lastIndexOf('.') + 1);
	}
}
