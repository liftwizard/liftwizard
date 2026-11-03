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

package io.liftwizard.model.reladomo.operation.compiler;

import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.compiler.model.ItemFinder;
import io.liftwizard.model.reladomo.operation.compiler.model.OwnerFinder;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class ReladomoOperationCompilerNotExistsTest
{
	@RegisterExtension
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml"
	);

	@Test
	void compilesNestedNotExistsWithWordOperators()
	{
		ImmutableMap<String, Operation> expected = Maps.immutable
			.<String, Operation>empty()
			.newWithKeyValue(
				"this.items { this.name = \"a\" } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().eq("a"))
			)
			.newWithKeyValue(
				"this.items { this.name startsWith \"a\" } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().startsWith("a"))
			)
			.newWithKeyValue(
				"this.items { this.name endsWith \"a\" } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().endsWith("a"))
			)
			.newWithKeyValue(
				"this.items { this.name contains \"a\" } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().contains("a"))
			)
			.newWithKeyValue(
				"this.items { this.name wildCardEquals \"a*\" } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().wildCardEq("a*"))
			)
			.newWithKeyValue(
				"this.items { this.name in [\"a\", \"b\"] } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().in(Sets.mutable.with("a", "b")))
			)
			.newWithKeyValue(
				"this.items { this.name is null } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().isNull())
			)
			.newWithKeyValue(
				"this.items { this.name is not null } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().isNotNull())
			)
			.newWithKeyValue(
				"this.items { this.name = \"a\" and this.id = 1 } not exists",
				OwnerFinder.items().notExists(
					ItemFinder.all().and(ItemFinder.name().eq("a")).and(ItemFinder.id().eq(1L))
				)
			)
			.newWithKeyValue(
				"this.items { this.name = \"a\" or this.id = 1 } not exists",
				OwnerFinder.items().notExists(ItemFinder.name().eq("a").or(ItemFinder.id().eq(1L)))
			);

		var compiler = new ReladomoOperationCompiler();
		ImmutableMap<String, Operation> actual = expected
			.keysView()
			.toMap((source) -> source, (source) -> compiler.compile(OwnerFinder.getFinderInstance(), source))
			.toImmutable();

		assertThat(actual).isEqualTo(expected);
	}
}
