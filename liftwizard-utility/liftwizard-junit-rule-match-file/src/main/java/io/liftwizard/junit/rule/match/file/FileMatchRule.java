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
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.junit.rules.ErrorCollector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FileMatchRule extends ErrorCollector
{
    public static String slurp(@Nonnull String resourceClassPathLocation, @Nonnull Class<?> callingClass)
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        Objects.requireNonNull(inputStream, resourceClassPathLocation);
        return FileMatchRule.slurp(inputStream);
    }

    public static String slurp(@Nonnull InputStream inputStream)
    {
        try (Scanner scanner = new Scanner(inputStream))
        {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public void assertFileContents(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            @Nonnull Class<?> callingClass)
    {
        try
        {
            this.assertFileContentsOrThrow(resourceClassPathLocation, actualString, callingClass);
        }
        catch (@Nonnull URISyntaxException | FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            @Nonnull Class<?> callingClass)
            throws URISyntaxException, FileNotFoundException
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        if (inputStream == null)
        {
            String               packageName      = callingClass.getPackage().getName();
            ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
            Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
            Path                 packagePath      = packageNameParts.injectInto(testResources, Path::resolve);
            File                 resourceFile     = packagePath.resolve(resourceClassPathLocation).toFile();

            assertThat(resourceFile.exists(), is(false));
            this.writeStringToFile(actualString, resourceFile);
            fail(resourceClassPathLocation);
        }

        String expectedStringFromFile = FileMatchRule.slurp(inputStream);
        URI    uri                    = callingClass.getResource(resourceClassPathLocation).toURI();
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

    private void writeStringToFile(@Nonnull String string, @Nonnull File file) throws FileNotFoundException
    {
        try (PrintWriter printWriter = new PrintWriter(file))
        {
            printWriter.write(string);
        }
    }
}
