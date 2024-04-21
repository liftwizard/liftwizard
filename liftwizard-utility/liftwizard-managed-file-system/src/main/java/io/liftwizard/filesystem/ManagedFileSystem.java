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

package io.liftwizard.filesystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManagedFileSystem
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedFileSystem.class);

    private static final LoadingCache<URI, FileSystem> MANAGED_FILE_SYSTEMS = Caffeine
            .newBuilder()
            .weakValues()
            .evictionListener(ManagedFileSystem::close)
            .build(ManagedFileSystem::getFileSystem);

    private ManagedFileSystem()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    private static void close(URI uri, FileSystem fileSystem, RemovalCause cause)
    {
        LOGGER.debug("Closing file system for {} due to {}", uri, cause);
        try
        {
            fileSystem.close();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Failed to close file system for " + uri, e);
        }
    }

    public static Path get(URI uri)
    {
        String scheme = uri.getScheme();
        if (!scheme.equals("file"))
        {
            FileSystem fileSystem = getWithNormalizedUri(uri);
            return fileSystem.getPath("/");
        }

        try
        {
            FileSystem fileSystem = getWithNormalizedUri(new URI("file:///"));
            String     path       = uri.getPath();
            return fileSystem.getPath(path);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static FileSystem getWithNormalizedUri(URI uri)
    {
        FileSystem fileSystem = MANAGED_FILE_SYSTEMS.get(uri);
        if (fileSystem.isOpen())
        {
            return fileSystem;
        }

        MANAGED_FILE_SYSTEMS.invalidate(uri);
        return MANAGED_FILE_SYSTEMS.get(uri);
    }

    private static FileSystem getFileSystem(URI uri)
            throws IOException
    {
        try
        {
            return FileSystems.getFileSystem(uri);
        }
        catch (FileSystemNotFoundException notFoundException)
        {
            try
            {
                return FileSystems.newFileSystem(uri, Map.of());
            }
            catch (FileSystemAlreadyExistsException alreadyExistsException)
            {
                return FileSystems.getFileSystem(uri);
            }
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            throw new IllegalArgumentException("Failed to get file system for " + uri, illegalArgumentException);
        }
    }
}
