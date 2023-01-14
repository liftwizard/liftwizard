/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.junit.rule.match.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.json.JSONException;
import org.junit.rules.ErrorCollector;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonMatchRule
        extends ErrorCollector
{
    public static String slurp(@Nonnull InputStream inputStream, Charset charset)
    {
        try (Scanner scanner = new Scanner(inputStream, charset))
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
            this.assertFileContentsOrThrow(
                    resourceClassPathLocation,
                    actualString,
                    callingClass);
        }
        catch (@Nonnull URISyntaxException | FileNotFoundException | JSONException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchElementException e)
        {
            throw new RuntimeException(resourceClassPathLocation, e);
        }
    }

    private void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            @Nonnull Class<?> callingClass)
            throws URISyntaxException, FileNotFoundException, JSONException
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        if (inputStream == null)
        {
            String               packageName      = callingClass.getPackage().getName();
            ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
            Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
            Path                 packagePath      = packageNameParts.injectInto(testResources, Path::resolve);
            File                 resourceFile     = packagePath.resolve(resourceClassPathLocation).toFile();

            assertThat(resourceFile.getAbsolutePath(), resourceFile.exists(), is(false));
            this.writeStringToFile(actualString, resourceFile);
            this.addError(new AssertionError(resourceClassPathLocation));
        }
        else
        {
            String expectedStringFromFile = JsonMatchRule.slurp(inputStream, StandardCharsets.UTF_8);
            URI    uri                    = callingClass.getResource(resourceClassPathLocation).toURI();
            if (!actualString.equals(expectedStringFromFile))
            {
                File file = new File(uri);
                this.writeStringToFile(actualString, file);
            }
            JSONAssert.assertEquals(
                    actualString,
                    expectedStringFromFile,
                    actualString,
                    JSONCompareMode.STRICT);
        }
    }

    private void writeStringToFile(@Nonnull String string, @Nonnull File file) throws FileNotFoundException
    {
        try (PrintWriter printWriter = new PrintWriter(file))
        {
            printWriter.write(string);
        }
    }
}
