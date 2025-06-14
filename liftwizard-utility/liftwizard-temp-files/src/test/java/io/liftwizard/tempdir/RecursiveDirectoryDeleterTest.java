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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@ExtendWith(LogMarkerTestExtension.class)
class RecursiveDirectoryDeleterTest {

    @TempDir
    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        Path nestedDir = Files.createDirectory(this.testDir.resolve("nested"));
        Files.write(nestedDir.resolve("file1.txt"), "test content".getBytes(StandardCharsets.UTF_8));
        Files.write(this.testDir.resolve("root-file.txt"), "root content".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deleteRecursively_shouldDeleteDirectoryWithContents() throws IOException {
        RecursiveDirectoryDeleter.deleteRecursively(this.testDir);

        assertThat(Files.exists(this.testDir)).isFalse();
    }

    @Test
    void deleteRecursively_shouldHandleNonExistentDirectory() throws IOException {
        Path nonExistentDir = Paths.get(this.testDir.toString(), "non-existent-" + System.currentTimeMillis());
        assertThat(Files.exists(nonExistentDir)).isFalse();

        RecursiveDirectoryDeleter.deleteRecursively(nonExistentDir);
    }

    @Test
    void tryDeleteRecursively_shouldReturnTrueOnSuccess() {
        boolean result = RecursiveDirectoryDeleter.tryDeleteRecursively(this.testDir);

        assertThat(result).isTrue();
        assertThat(Files.exists(this.testDir)).isFalse();
    }

    @Test
    void tryDeleteRecursively_shouldReturnTrueForNonExistentDirectory() {
        Path nonExistentDir = Paths.get(this.testDir.toString(), "non-existent-" + System.currentTimeMillis());
        assertThat(Files.exists(nonExistentDir)).isFalse();

        boolean result = RecursiveDirectoryDeleter.tryDeleteRecursively(nonExistentDir);

        assertThat(result).isTrue();
    }

    @Test
    void tryDeleteRecursively_shouldReturnFalseForReadOnlyFile() throws IOException {
        Path readOnlyFile = this.testDir.resolve("readonly.txt");
        Files.write(readOnlyFile, "content".getBytes(StandardCharsets.UTF_8));
        boolean setReadOnly = readOnlyFile.toFile().setReadOnly();
        assertThat(setReadOnly).isTrue();

        // Check if this platform actually prevents deletion of read-only files
        Path testFile = this.testDir.resolve("test-deletion.txt");
        Files.write(testFile, "test".getBytes(StandardCharsets.UTF_8));
        testFile.toFile().setReadOnly();
        boolean canDeleteReadOnly = testFile.toFile().delete();

        // Skip test on platforms that allow deletion of read-only files (e.g., macOS, Linux)
        assumeFalse(canDeleteReadOnly, "Platform allows deletion of read-only files");

        boolean result = RecursiveDirectoryDeleter.tryDeleteRecursively(this.testDir);

        readOnlyFile.toFile().setWritable(true);

        assertThat(result).isFalse();
        assertThat(Files.exists(readOnlyFile)).isTrue();
    }
}
