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

package io.liftwizard.junit.extension.match.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import io.liftwizard.junit.extension.match.AbstractMatchExtension;
import io.liftwizard.junit.extension.match.FileSlurper;

public class FileMatchExtension
        extends AbstractMatchExtension
{
    public FileMatchExtension(@Nonnull Class<?> callingClass)
    {
        super(callingClass);
    }

    @Override
    protected void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws URISyntaxException, IOException
    {
        if (this.resourceRerecorderExtension.mustRerecord(resourceClassPathLocation))
        {
            String prettyPrintedString = this.getPrettyPrintedString(actualString);

            Path packagePath  = this.resourceRerecorderExtension.getPackagePath();
            File resourceFile = packagePath.resolve(resourceClassPathLocation).toFile();

            this.resourceRerecorderExtension.writeStringToFile(
                    resourceClassPathLocation,
                    prettyPrintedString,
                    resourceFile);
            if (!this.rerecordEnabled)
            {
                String detailMessage = resourceClassPathLocation + " did not exist. Created it.";
                this.errorCollectorExtension.addError(new AssertionError(detailMessage));
            }
        }
        else
        {
            InputStream inputStream            = this.callingClass.getResourceAsStream(resourceClassPathLocation);
            String      expectedStringFromFile = FileSlurper.slurp(inputStream, StandardCharsets.UTF_8);

            if (!actualString.equals(expectedStringFromFile))
            {
                String         detailMessage  = this.resourceRerecorderExtension.handleMismatch(
                        resourceClassPathLocation,
                        actualString);
                AssertionError assertionError = new AssertionError(detailMessage);
                this.errorCollectorExtension.addError(assertionError);
            }
        }
    }

    @Override
    protected String getPrettyPrintedString(@Nonnull String string)
    {
        return string;
    }
}
