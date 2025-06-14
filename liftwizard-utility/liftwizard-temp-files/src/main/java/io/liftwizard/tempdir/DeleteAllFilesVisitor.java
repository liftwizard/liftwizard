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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DeleteAllFilesVisitor extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllFilesVisitor.class);

    @Override
    public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attributes) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(@Nonnull Path path, @Nonnull IOException exception) throws IOException {
        LOGGER.warn("Failed to visit path {}: {}", path, exception.getMessage());
        throw exception;
    }

    @Override
    public FileVisitResult postVisitDirectory(@Nonnull Path path, IOException exception) throws IOException {
        if (exception != null) {
            LOGGER.warn("Error occurred while traversing directory {}: {}", path, exception.getMessage());
            throw exception;
        }

        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }
}
