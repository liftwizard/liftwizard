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

package io.liftwizard.rewrite;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

import org.openrewrite.test.SourceSpecs;

import static org.openrewrite.java.Assertions.java;

/**
 * Loads OpenRewrite test inputs and expected outputs from src/test/resources instead of inline text blocks.
 *
 * <p>Setting the environment variable {@code LIFTWIZARD_FILE_MATCH_RULE_RERECORD} to {@code true} overwrites the
 * expected fixtures with whatever the recipes actually emit, which is how expected fixtures are regenerated after a
 * recipe or style change.
 */
public interface AbstractRewriteFixtures {
	String RERECORD_ENVIRONMENT_VARIABLE = "LIFTWIZARD_FILE_MATCH_RULE_RERECORD";

	default boolean isRerecordEnabled() {
		return Boolean.parseBoolean(System.getenv(RERECORD_ENVIRONMENT_VARIABLE));
	}

	/**
	 * A source file that a recipe is expected to change, stored as a before fixture and an after fixture that share the
	 * base name. For example {@code javaFixture("replacePatterns/01")} reads {@code replacePatterns/01-before.java} and
	 * {@code replacePatterns/01-after.java}.
	 */
	default SourceSpecs javaFixture(String name) {
		String before = this.fixture(name + "-before.java");

		if (this.isRerecordEnabled()) {
			return java(before, before, (spec) ->
				spec.after((actual) -> {
					this.rerecordFixture(name + "-after.java", actual);
					return actual;
				})
			);
		}

		return java(before, this.fixture(name + "-after.java"));
	}

	/** A source file that a recipe must leave alone. */
	default SourceSpecs javaFixtureUnchanged(String name) {
		return java(this.fixture(name + "-unchanged.java"));
	}

	default String fixture(String name) {
		String resourcePath = this.getClass().getSimpleName() + "/" + name;
		InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
		Objects.requireNonNull(inputStream, resourcePath);
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
			return scanner.useDelimiter("\\A").next();
		}
	}

	private void rerecordFixture(String name, String contents) {
		Path path = this.fixturePath(name);
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, contents.endsWith("\n") ? contents : contents + "\n", StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Rerecording fixture: " + path, e);
		}
	}

	private Path fixturePath(String name) {
		Path result = Path.of("src", "test", "resources").toAbsolutePath();
		for (String packagePart : this.getClass().getPackageName().split("\\.")) {
			result = result.resolve(packagePart);
		}
		return result.resolve(this.getClass().getSimpleName()).resolve(name);
	}
}
