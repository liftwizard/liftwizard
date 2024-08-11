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

package io.liftwizard.junit.rule.match.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.liftwizard.junit.rule.match.AbstractMatchRule;

import static org.junit.Assert.assertEquals;

public class FileMatchRule
        extends AbstractMatchRule
{
    public FileMatchRule(@Nonnull Class<?> callingClass)
    {
        super(callingClass);
    }

    @Override
    protected void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws URISyntaxException, IOException
    {
        Path packagePath = getPackagePath(this.callingClass);
        if (this.rerecordEnabled && !CLEANED_PATHS.contains(packagePath))
        {
            deleteDirectoryRecursively(packagePath);
            CLEANED_PATHS.add(packagePath);
        }

        InputStream inputStream = this.callingClass.getResourceAsStream(resourceClassPathLocation);
        if ((this.rerecordEnabled || inputStream == null) && !this.rerecordedPaths.contains(resourceClassPathLocation))
        {
            File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

            this.writeStringToFile(resourceClassPathLocation, actualString, resourceFile);
            if (!this.rerecordEnabled)
            {
                this.addError(new AssertionError(resourceClassPathLocation + " did not exist. Created it."));
            }
        }
        else
        {
            Objects.requireNonNull(inputStream, () -> resourceClassPathLocation + " not found.");
            String expectedStringFromFile = slurp(inputStream, StandardCharsets.UTF_8);

            URL resource = Objects.requireNonNull(this.callingClass.getResource(resourceClassPathLocation));
            URI uri = resource.toURI();

            if (!actualString.equals(expectedStringFromFile))
            {
                if (this.rerecordedPaths.contains(resourceClassPathLocation))
                {
                    String detailMessage = "Rerecorded file: %s. Not recording again with contents:%n%s".formatted(
                            uri,
                            actualString);
                    AssertionError assertionError = new AssertionError(detailMessage);
                    this.addError(assertionError);
                    return;
                }

                File file = new File(uri);
                this.writeStringToFile(resourceClassPathLocation, actualString, file);
            }

            this.checkSucceeds(() ->
            {
                assertEquals("Writing expected file to: " + uri, expectedStringFromFile, actualString);
                return null;
            });
        }
    }

    @Override
    protected String getPrettyPrintedString(@Nonnull String string)
    {
        return string;
    }
}
