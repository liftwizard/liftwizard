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

package io.liftwizard.junit.extension.match.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.liftwizard.junit.extension.match.AbstractMatchExtension;
import io.liftwizard.junit.extension.match.FileSlurper;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class JsonMatchExtension
        extends AbstractMatchExtension
{
    private final ObjectMapper objectMapper;

    public JsonMatchExtension(@Nonnull Class<?> callingClass)
    {
        this(callingClass, JsonMatchExtension.newObjectMapper());
    }

    public JsonMatchExtension(@Nonnull Class<?> callingClass, ObjectMapper objectMapper)
    {
        super(callingClass);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }

    @Override
    protected void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws IOException, URISyntaxException
    {
        if (this.resourceRerecorderExtension.mustRerecord(resourceClassPathLocation))
        {
            String prettyPrintedString = this.getPrettyPrintedString(actualString);

            Path packagePath = this.resourceRerecorderExtension.getPackagePath();
            File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

            this.resourceRerecorderExtension.writeStringToFile(resourceClassPathLocation, prettyPrintedString, resourceFile);
            if (!this.rerecordEnabled)
            {
                String detailMessage = resourceClassPathLocation + " did not exist. Created it.";
                this.errorCollectorExtension.addError(new AssertionError(detailMessage));
            }
        }
        else
        {
            InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
            Objects.requireNonNull(inputStream, () -> resourceClassPathLocation + " not found.");
            String expectedStringFromFile = FileSlurper.slurp(inputStream, StandardCharsets.UTF_8);
            URI    uri                    = this.callingClass.getResource(resourceClassPathLocation).toURI();

            if (!this.validateExpectedStringFromFile(expectedStringFromFile, uri))
            {
                return;
            }

            String fileContents = this.getPrettyPrintedString(actualString);
            Optional<String> message = this.compareAndGetDiff(fileContents, expectedStringFromFile);
            if (message.isPresent())
            {
                String detailMessage = this.resourceRerecorderExtension.handleMismatch(resourceClassPathLocation, fileContents);
                AssertionError assertionError = new AssertionError(detailMessage);
                this.errorCollectorExtension.addError(assertionError);
            }
        }
    }

    protected Optional<String> compareAndGetDiff(@Nonnull String actualString, String expectedStringFromFile)
    {
        try
        {
            JSONCompareResult result = JSONCompare.compareJSON(
                    expectedStringFromFile,
                    actualString,
                    JSONCompareMode.STRICT);
            if (result.passed())
            {
                return Optional.empty();
            }

            if (result.failed())
            {
                String message = result.getMessage();
                return Optional.of(message);
            }

            throw new AssertionError(result);
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected boolean validateExpectedStringFromFile(String expectedStringFromFile, URI uri)
    {
        try
        {
            this.objectMapper.readTree(expectedStringFromFile);
            return true;
        }
        catch (JacksonException e)
        {
            String detailMessage = "Invalid JSON in %s:%n%s".formatted(
                    uri,
                    expectedStringFromFile);
            AssertionError assertionError = new AssertionError(detailMessage, e);
            this.errorCollectorExtension.addError(assertionError);
            return false;
        }
    }

    @Override
    protected String getPrettyPrintedString(@Nonnull String string)
    {
        try
        {
            JsonNode jsonNode            = this.objectMapper.readTree(string);
            String   prettyPrintedString = this.objectMapper.writeValueAsString(jsonNode);
            return prettyPrintedString;
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
