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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.liftwizard.junit.extension.error.ErrorCollectorExtension;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class AbstractMatchExtension
        implements BeforeEachCallback, AfterEachCallback
{
    protected final Class<?> callingClass;
    protected final boolean rerecordEnabled;

    protected final ResourceRerecorderExtension resourceRerecorderExtension;
    protected final ErrorCollectorExtension     errorCollectorExtension = new ErrorCollectorExtension();

    public AbstractMatchExtension(@Nonnull Class<?> callingClass)
    {
        this(callingClass, Boolean.parseBoolean(System.getenv("LIFTWIZARD_FILE_MATCH_RULE_RERECORD")));
    }

    public AbstractMatchExtension(@Nonnull Class<?> callingClass, boolean rerecordEnabled)
    {
        this.callingClass                = Objects.requireNonNull(callingClass);
        this.rerecordEnabled             = rerecordEnabled;
        this.resourceRerecorderExtension = new ResourceRerecorderExtension(callingClass, rerecordEnabled);
    }

    protected Path getPackagePath()
    {
        String               packageName      = this.callingClass.getPackage().getName();
        ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
        Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
        return packageNameParts.injectInto(testResources, Path::resolve);
    }

    public void assertFileContents(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
    {
        try
        {
            this.assertFileContentsOrThrow(resourceClassPathLocation, actualString);
        }
        catch (Exception e)
        {
            throw new RuntimeException(resourceClassPathLocation, e);
        }
    }

    protected abstract void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString)
            throws Exception;

    protected abstract String getPrettyPrintedString(@Nonnull String string);

    @Override
    public void beforeEach(ExtensionContext context)
            throws IOException
    {
        this.resourceRerecorderExtension.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        this.errorCollectorExtension.afterEach(context);
    }
}
