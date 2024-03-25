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

package io.liftwizard.junit.extension.match;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ResourceRerecorderExtension
        implements BeforeEachCallback
{
    protected static final MutableSet<Path> CLEANED_PATHS = Sets.mutable.empty();

    private final Class<?> callingClass;
    private final boolean  rerecordEnabled;

    private final MutableSet<String> rerecordedPaths = Sets.mutable.empty();

    public ResourceRerecorderExtension(Class<?> callingClass, boolean rerecordEnabled)
    {
        this.callingClass    = Objects.requireNonNull(callingClass);
        this.rerecordEnabled = rerecordEnabled;
    }

    @Override
    public void beforeEach(ExtensionContext context)
            throws IOException
    {
        Path packagePath = this.getPackagePath();
        if (this.rerecordEnabled && !CLEANED_PATHS.contains(packagePath))
        {
            deleteDirectoryRecursively(packagePath);
            CLEANED_PATHS.add(packagePath);
        }
    }

    public Path getPackagePath()
    {
        String               packageName      = this.callingClass.getPackage().getName();
        ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
        Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
        return packageNameParts.injectInto(testResources, Path::resolve);
    }

    @Nonnull
    public static void deleteDirectoryRecursively(@Nonnull Path directory)
            throws IOException
    {
        if (!directory.toFile().exists())
        {
            return;
        }
        Files.walkFileTree(directory, new DeleteAllFilesVisitor());
    }

    public boolean mustRerecord(String resourceClassPathLocation)
    {
        InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
        return (this.rerecordEnabled || inputStream == null)
                && !this.rerecordedPaths.contains(resourceClassPathLocation);
    }

    public String handleMismatch(String resourceClassPathLocation, String fileContents)
            throws URISyntaxException, FileNotFoundException
    {
        URI uri = this.callingClass.getResource(resourceClassPathLocation).toURI();

        if (this.rerecordedPaths.contains(resourceClassPathLocation))
        {
            return "Rerecorded file: %s. Not recording again with contents:%n%s".formatted(
                    uri,
                    fileContents);
        }

        File file = new File(uri);
        this.writeStringToFile(resourceClassPathLocation, fileContents, file);

        return "Writing expected file to: %s%n".formatted(uri);
    }

    public void writeStringToFile(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String fileContents,
            @Nonnull File destinationFile)
            throws FileNotFoundException
    {
        this.rerecordedPaths.add(resourceClassPathLocation);

        if (!destinationFile.exists())
        {
            destinationFile.getParentFile().mkdirs();
        }

        try (PrintWriter printWriter = new PrintWriter(destinationFile))
        {
            printWriter.print(fileContents);
        }
    }
}
