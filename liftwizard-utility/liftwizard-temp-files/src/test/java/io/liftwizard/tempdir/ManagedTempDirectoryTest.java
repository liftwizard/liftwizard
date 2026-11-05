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

package io.liftwizard.tempdir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(LogMarkerTestExtension.class)
class ManagedTempDirectoryTest
{
	@Test
	void createTempDirectory_shouldCreateDirectoryThatExistsAndIsWritable()
		throws IOException
	{
		Path tempDir = ManagedTempDirectory.createTempDirectory("test-prefix");

		assertThat(tempDir).exists().isDirectory();
		assertThat(tempDir.toFile()).canWrite();
		assertThat(tempDir.getFileName().toString()).startsWith("test-prefix");

		RecursiveDirectoryDeleter.deleteRecursively(tempDir);
	}

	@Test
	void create_shouldReturnManagedInstanceWithAccessiblePath()
		throws IOException
	{
		try (ManagedTempDirectory managedDir = ManagedTempDirectory.create("managed-test"))
		{
			Path tempDir = managedDir.getPath();

			assertThat(tempDir).exists().isDirectory();
			assertThat(tempDir.getFileName().toString()).startsWith("managed-test");

			Path testFile = tempDir.resolve("test-file.txt");
			Files.write(testFile, "test content".getBytes(StandardCharsets.UTF_8));
			assertThat(testFile).exists();
		}
	}

	@Test
	void close_shouldDeleteDirectoryAndContents()
		throws IOException
	{
		ManagedTempDirectory managedDir = ManagedTempDirectory.create("close-test");
		Path tempDir = managedDir.getPath();
		Path testFile = tempDir.resolve("test-file.txt");
		Files.write(testFile, "test content".getBytes(StandardCharsets.UTF_8));

		managedDir.close();

		assertThat(tempDir).doesNotExist();
		assertThat(testFile).doesNotExist();
	}

	@Test
	void close_shouldBeIdempotent()
		throws IOException
	{
		ManagedTempDirectory managedDir = ManagedTempDirectory.create("idempotent-test");

		managedDir.close();
		// Second close should not throw
		managedDir.close();
	}

	@Test
	void tryClose_shouldReturnTrueOnSuccess()
		throws IOException
	{
		ManagedTempDirectory managedDir = ManagedTempDirectory.create("try-close-test");

		boolean result = managedDir.tryClose();

		assertThat(result).isTrue();
		assertThat(managedDir.getPath()).doesNotExist();
	}

	@Test
	void tryClose_shouldRetryAfterFailedDeletion()
		throws IOException
	{
		ManagedTempDirectory managedDir = ManagedTempDirectory.create("try-close-retry-test");
		lockSubdirectory(managedDir.getPath());

		assertThat(managedDir.tryClose()).isFalse();
		assertThat(managedDir.tryClose()).isFalse();
		assertThat(managedDir.getPath()).exists();

		unlock(managedDir.getPath());

		assertThat(managedDir.tryClose()).isTrue();
		assertThat(managedDir.getPath()).doesNotExist();
	}

	@Test
	void close_shouldRetryAfterFailedDeletion()
		throws IOException
	{
		ManagedTempDirectory managedDir = ManagedTempDirectory.create("close-retry-test");
		lockSubdirectory(managedDir.getPath());

		assertThatThrownBy(managedDir::close)
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Failed to delete temporary directory: " + managedDir.getPath());
		assertThat(managedDir.getPath()).exists();

		unlock(managedDir.getPath());

		managedDir.close();
		assertThat(managedDir.getPath()).doesNotExist();
	}

	@Test
	void shutdownCleanup_shouldContinuePastFailedDeletions()
		throws IOException
	{
		ManagedTempDirectory lockedManagedDir = ManagedTempDirectory.create("shutdown-locked-test");
		lockSubdirectory(lockedManagedDir.getPath());
		ImmutableList<ManagedTempDirectory> managedDirs = Lists.immutable
			.with("a", "b", "c", "d", "e", "f", "g", "h")
			.collect((suffix) -> ManagedTempDirectory.create("shutdown-test-" + suffix));

		ManagedTempDirectory.shutdownCleanup();

		assertThat(managedDirs.collect(ManagedTempDirectory::getPath)).allSatisfy((path) ->
			assertThat(path).doesNotExist()
		);
		assertThat(lockedManagedDir.getPath()).exists();

		unlock(lockedManagedDir.getPath());
		lockedManagedDir.close();
	}

	private static void lockSubdirectory(Path parent)
		throws IOException
	{
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
		Path lockedDir = Files.createDirectory(parent.resolve("locked"));
		Files.writeString(lockedDir.resolve("locked-file.txt"), "locked content");
		Files.setPosixFilePermissions(lockedDir, PosixFilePermissions.fromString("r-x------"));
	}

	private static void unlock(Path parent)
		throws IOException
	{
		Files.setPosixFilePermissions(parent.resolve("locked"), PosixFilePermissions.fromString("rwx------"));
	}
}
