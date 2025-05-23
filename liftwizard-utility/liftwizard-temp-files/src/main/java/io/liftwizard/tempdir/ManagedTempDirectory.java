/*
 * Copyright 2024 Craig Motlin
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
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManagedTempDirectory implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedTempDirectory.class);

    private static final Set<ManagedTempDirectory> SHUTDOWN_REGISTRY = ConcurrentHashMap.newKeySet();

    private static final Thread SHUTDOWN_HOOK = new Thread(
        ManagedTempDirectory::shutdownCleanup,
        "ManagedTempDirectory-Shutdown-Hook"
    );

    static {
        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
    }

    private static void shutdownCleanup() {
        LOGGER.debug("Running shutdown hook to clean up {} temporary directories", SHUTDOWN_REGISTRY.size());
        SHUTDOWN_REGISTRY.forEach(instance -> {
            try {
                instance.close();
            } catch (IOException e) {
                LOGGER.warn(
                    "Failed to clean up temporary directory {} during shutdown: {}",
                    instance.path,
                    e.getMessage()
                );
                LOGGER.debug("Exception details:", e);
            }
        });
    }

    public static Path createTempDirectory(@Nonnull String prefix) throws IOException {
        Objects.requireNonNull(prefix, "prefix cannot be null");

        Path tempDir = Files.createTempDirectory(prefix);
        var instance = new ManagedTempDirectory(tempDir);
        SHUTDOWN_REGISTRY.add(instance);

        LOGGER.debug("Created temporary directory (registered for JVM shutdown cleanup): {}", tempDir);
        return tempDir;
    }

    public static ManagedTempDirectory create(@Nonnull String prefix) throws IOException {
        return create(prefix, new FileAttribute<?>[0]);
    }

    public static ManagedTempDirectory create(@Nonnull String prefix, FileAttribute<?>... attrs) throws IOException {
        Objects.requireNonNull(prefix, "prefix cannot be null");

        Path tempDir = Files.createTempDirectory(prefix, attrs);
        var instance = new ManagedTempDirectory(tempDir);
        SHUTDOWN_REGISTRY.add(instance);

        LOGGER.debug("Created managed temporary directory: {}", tempDir);
        return instance;
    }

    private final Path path;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private ManagedTempDirectory(@Nonnull Path path) {
        this.path = Objects.requireNonNull(path, "path cannot be null");
    }

    @Nonnull
    public Path getPath() {
        return this.path;
    }

    @Override
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            LOGGER.debug("Closing managed temporary directory: {}", this.path);
            SHUTDOWN_REGISTRY.remove(this);
            RecursiveDirectoryDeleter.deleteRecursively(this.path);
        }
    }

    public boolean tryClose() {
        if (this.closed.compareAndSet(false, true)) {
            LOGGER.debug("Attempting to close managed temporary directory: {}", this.path);
            SHUTDOWN_REGISTRY.remove(this);
            return RecursiveDirectoryDeleter.tryDeleteRecursively(this.path);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManagedTempDirectory that = (ManagedTempDirectory) o;
        return this.path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    @Override
    public String toString() {
        return "ManagedTempDirectory{path=" + this.path + ", closed=" + this.closed.get() + '}';
    }
}
