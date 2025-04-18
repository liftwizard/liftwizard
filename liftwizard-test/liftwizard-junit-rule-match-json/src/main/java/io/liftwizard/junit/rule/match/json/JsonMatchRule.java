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

package io.liftwizard.junit.rule.match.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import io.liftwizard.junit.rule.match.AbstractMatchRule;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class JsonMatchRule extends AbstractMatchRule {

    private final ObjectMapper objectMapper;

    public JsonMatchRule(@Nonnull Class<?> callingClass) {
        super(callingClass);
        this.objectMapper = JsonMatchRule.newObjectMapper();
    }

    private static ObjectMapper newObjectMapper() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }

    @Override
    protected void assertFileContentsOrThrow(@Nonnull String resourceClassPathLocation, @Nonnull String actualString)
        throws URISyntaxException, IOException {
        Path packagePath = getPackagePath(this.callingClass);
        if (this.rerecordEnabled && !CLEANED_PATHS.contains(packagePath)) {
            deleteDirectoryRecursively(packagePath);
            CLEANED_PATHS.add(packagePath);
        }

        InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
        if (
            (this.rerecordEnabled || inputStream == null) && !this.rerecordedPaths.contains(resourceClassPathLocation)
        ) {
            File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

            this.writeStringToFile(resourceClassPathLocation, actualString, resourceFile);
            if (!this.rerecordEnabled) {
                this.addError(new AssertionError(resourceClassPathLocation + " did not exist. Created it."));
            }
        } else {
            Objects.requireNonNull(inputStream, () -> resourceClassPathLocation + " not found.");
            String expectedStringFromFile = slurp(inputStream, StandardCharsets.UTF_8);

            URL resource = Objects.requireNonNull(this.callingClass.getResource(resourceClassPathLocation));
            URI uri = resource.toURI();

            if (!this.validateExpectedStringFromFile(expectedStringFromFile, uri)) {
                return;
            }

            Optional<String> message = this.compareAndGetDiff(actualString, expectedStringFromFile);
            if (message.isPresent()) {
                if (this.rerecordedPaths.contains(resourceClassPathLocation)) {
                    String detailMessage =
                        "Rerecorded file: %s. Not recording again with contents:%n%s".formatted(uri, actualString);
                    AssertionError assertionError = new AssertionError(detailMessage);
                    this.addError(assertionError);
                    return;
                }

                File file = new File(uri);
                this.writeStringToFile(resourceClassPathLocation, actualString, file);

                String detailMessage = "Writing expected file to: %s%n%s".formatted(uri, message);
                AssertionError assertionError = new AssertionError(detailMessage);
                this.addError(assertionError);
            }
        }
    }

    protected Optional<String> compareAndGetDiff(@Nonnull String actualString, String expectedStringFromFile) {
        try {
            JSONCompareResult result = JSONCompare.compareJSON(
                expectedStringFromFile,
                actualString,
                JSONCompareMode.STRICT
            );
            if (result.passed()) {
                return Optional.empty();
            }

            if (result.failed()) {
                String message = result.getMessage();
                return Optional.of(message);
            }

            throw new AssertionError(result);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateExpectedStringFromFile(String expectedStringFromFile, URI uri) {
        try {
            this.objectMapper.readTree(expectedStringFromFile);
            return true;
        } catch (JacksonException e) {
            String detailMessage = "Invalid JSON in %s:%n%s".formatted(uri, expectedStringFromFile);
            AssertionError assertionError = new AssertionError(detailMessage, e);
            this.addError(assertionError);
            return false;
        }
    }

    @Override
    protected String getPrettyPrintedString(@Nonnull String string) {
        try {
            JsonNode jsonNode = this.objectMapper.readTree(string);
            String prettyPrintedString = this.objectMapper.writeValueAsString(jsonNode);
            return prettyPrintedString;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
