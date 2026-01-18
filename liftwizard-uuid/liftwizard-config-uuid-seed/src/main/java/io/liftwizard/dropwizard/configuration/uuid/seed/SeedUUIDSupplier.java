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

package io.liftwizard.dropwizard.configuration.uuid.seed;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SeedUUIDSupplier implements Supplier<UUID> {

	private final String seed;
	private int counter;

	public SeedUUIDSupplier(@Nonnull String seed) {
		this.seed = requireNonNull(seed);
	}

	@Nonnull
	@Override
	public UUID get() {
		this.counter++;
		String name = this.seed + this.counter;
		byte[] bytes = name.getBytes(UTF_8);
		return UUID.nameUUIDFromBytes(bytes);
	}
}
