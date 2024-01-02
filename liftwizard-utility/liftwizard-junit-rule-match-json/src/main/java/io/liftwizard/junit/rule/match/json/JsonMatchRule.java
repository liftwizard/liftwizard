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
import java.util.Objects;
import java.util.Scanner;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.json.JSONException;
import org.junit.rules.ErrorCollector;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonMatchRule
        extends ErrorCollector
{
    private static final MutableSet<Path> CLEANED_PATHS = Sets.mutable.empty();

    @Nonnull
    private final Class<?>     callingClass;
    private final boolean      rerecordEnabled;
    private final ObjectMapper objectMapper;

    public JsonMatchRule(@Nonnull Class<?> callingClass)
    {
        this.callingClass    = Objects.requireNonNull(callingClass);
        this.rerecordEnabled = Boolean.parseBoolean(System.getenv("LIFTWIZARD_FILE_MATCH_RULE_RERECORD"));
        this.objectMapper    = JsonMatchRule.newObjectMapper();
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }

    public static String slurp(@Nonnull String resourceClassPathLocation, @Nonnull Class<?> callingClass)
    {
        return JsonMatchRule.slurp(resourceClassPathLocation, callingClass, StandardCharsets.UTF_8);
    }

    public static String slurp(
            @Nonnull String resourceClassPathLocation,
            @Nonnull Class<?> callingClass,
            Charset charset)
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        Objects.requireNonNull(inputStream, resourceClassPathLocation);
        return JsonMatchRule.slurp(inputStream, charset);
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
        catch (JSONException | IOException | URISyntaxException e)
        {
            throw new RuntimeException(resourceClassPathLocation, e);
        }
    }

    private void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws URISyntaxException, IOException, JSONException
    {
        assertThat(actualString, not(is("")));
        try
        {
            this.objectMapper.readTree(actualString);
        }
        catch (JsonProcessingException e)
        {
            throw new AssertionError("Invalid JSON: " + actualString, e);
        }
        Path packagePath = JsonMatchRule.getPackagePath(this.callingClass);
        if (this.rerecordEnabled && !CLEANED_PATHS.contains(packagePath))
        {
            JsonMatchRule.deleteDirectoryRecursively(packagePath);
            CLEANED_PATHS.add(packagePath);
        }

        InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
        if (this.rerecordEnabled || inputStream == null)
        {
            this.recordFile(resourceClassPathLocation, actualString, packagePath);
        }
        else
        {
            this.compareFile(resourceClassPathLocation, actualString, inputStream);
        }
    }

    private void recordFile(@Nonnull String resourceClassPathLocation, @Nonnull String actualString, Path packagePath)
            throws FileNotFoundException
    {
        File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

        // assertThat(resourceFile.getAbsolutePath(), resourceFile.exists(), is(false));
        this.writeStringToFile(actualString, resourceFile);
        if (!this.rerecordEnabled)
        {
            this.addError(new AssertionError(resourceClassPathLocation + " did not exist. Created it."));
        }
    }

    private void compareFile(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            InputStream inputStream)
            throws URISyntaxException, FileNotFoundException
    {
        String expectedStringFromFile = JsonMatchRule.slurp(inputStream, StandardCharsets.UTF_8);
        URI    uri                    = this.callingClass.getResource(resourceClassPathLocation).toURI();

        try
        {
            JSONCompareResult result = JSONCompare.compareJSON(
                    expectedStringFromFile,
                    actualString,
                    JSONCompareMode.STRICT);
            if (result.failed())
            {
                File file = new File(uri);
                this.writeStringToFile(actualString, file);

                String message = result.getMessage();
                this.addError(new AssertionError("Writing expected file to: " + uri + '\n' + message));
            }
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
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
            JsonNode jsonNode            = this.objectMapper.readTree(string);
            String   prettyPrintedString = this.objectMapper.writeValueAsString(jsonNode);
            printWriter.print(prettyPrintedString);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
