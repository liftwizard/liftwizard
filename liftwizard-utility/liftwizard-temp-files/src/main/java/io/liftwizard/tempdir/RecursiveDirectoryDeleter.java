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

package io.liftwizard.tempdir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecursiveDirectoryDeleter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveDirectoryDeleter.class);

	private RecursiveDirectoryDeleter() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static void deleteRecursively(@Nonnull Path directory) throws IOException {
		Objects.requireNonNull(directory, "directory cannot be null");

		if (!Files.exists(directory)) {
			LOGGER.debug("Directory {} does not exist, skipping deletion", directory);
			return;
		}

		LOGGER.debug("Deleting directory recursively: {}", directory);
		Files.walkFileTree(directory, new DeleteAllFilesVisitor());
	}

	public static boolean tryDeleteRecursively(@Nonnull Path directory) {
		Objects.requireNonNull(directory, "directory cannot be null");

		if (!Files.exists(directory)) {
			LOGGER.debug("Directory {} does not exist, skipping deletion", directory);
			return true;
		}

		try {
			deleteRecursively(directory);
			return true;
		} catch (IOException e) {
			LOGGER.warn("Failed to delete directory {}: {}", directory, e.getMessage());
			LOGGER.debug("Exception details:", e);
			return false;
		}
	}
}
