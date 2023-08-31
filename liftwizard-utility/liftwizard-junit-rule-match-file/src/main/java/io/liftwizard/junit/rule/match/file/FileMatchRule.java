/*
 * Copyright 2020 Craig Motlin
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

package io.liftwizard.junit.rule.match.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.junit.rules.ErrorCollector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileMatchRule
        extends ErrorCollector
{
    private static final MutableSet<Path> CLEANED_PATHS = Sets.mutable.empty();

    @Nonnull
    private final Class<?> callingClass;
    private final boolean  rerecordEnabled;

    public FileMatchRule(@Nonnull Class<?> callingClass)
    {
        this.callingClass    = Objects.requireNonNull(callingClass);
        this.rerecordEnabled = Boolean.parseBoolean(System.getenv("LIFTWIZARD_FILE_MATCH_RULE_RERECORD"));
    }

    public static String slurp(@Nonnull String resourceClassPathLocation, @Nonnull Class<?> callingClass)
    {
        return FileMatchRule.slurp(resourceClassPathLocation, callingClass, StandardCharsets.UTF_8);
    }

    public static String slurp(
            @Nonnull String resourceClassPathLocation,
            @Nonnull Class<?> callingClass,
            Charset charset)
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        Objects.requireNonNull(inputStream, resourceClassPathLocation);
        return FileMatchRule.slurp(inputStream, charset);
    }

    public static String slurp(@Nonnull InputStream inputStream, Charset charset)
    {
        try (Scanner scanner = new Scanner(inputStream, charset))
        {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public void assertFileContents(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
    {
        try
        {
            this.assertFileContentsOrThrow(resourceClassPathLocation, actualString);
        }
        catch (@Nonnull URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException | NoSuchElementException e)
        {
            throw new RuntimeException(resourceClassPathLocation, e);
        }
    }

    private void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws URISyntaxException, IOException
    {
        Path packagePath = FileMatchRule.getPackagePath(this.callingClass);
        if (this.rerecordEnabled && !this.CLEANED_PATHS.contains(packagePath))
        {
            FileMatchRule.deleteDirectoryRecursively(packagePath);
            this.CLEANED_PATHS.add(packagePath);
        }
        InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
        if (this.rerecordEnabled || inputStream == null)
        {
            File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

            assertThat(resourceFile.getAbsolutePath(), resourceFile.exists(), is(false));
            this.writeStringToFile(actualString, resourceFile);
            if (!this.rerecordEnabled)
            {
                this.addError(new AssertionError(resourceClassPathLocation + " did not exist. Created it."));
            }
        }
        else
        {
            String expectedStringFromFile = FileMatchRule.slurp(inputStream, StandardCharsets.UTF_8);
            URI    uri                    = this.callingClass.getResource(resourceClassPathLocation).toURI();
            if (!actualString.equals(expectedStringFromFile))
            {
                File file = new File(uri);
                this.writeStringToFile(actualString, file);
            }
            this.checkThat(
                    "Writing expected file to: " + uri,
                    actualString,
                    is(expectedStringFromFile));
        }
    }

    @Nonnull
    private static void deleteDirectoryRecursively(@Nonnull Path directory)
            throws IOException
    {
        if (!directory.toFile().exists())
        {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(@Nonnull Path dir, IOException exc)
                    throws IOException
            {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static Path getPackagePath(@Nonnull Class<?> callingClass)
    {
        String               packageName      = callingClass.getPackage().getName();
        ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
        Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
        return packageNameParts.injectInto(testResources, Path::resolve);
    }

    private void writeStringToFile(@Nonnull String string, @Nonnull File file)
            throws FileNotFoundException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        try (PrintWriter printWriter = new PrintWriter(file))
        {
            printWriter.print(string);
        }
    }
}
