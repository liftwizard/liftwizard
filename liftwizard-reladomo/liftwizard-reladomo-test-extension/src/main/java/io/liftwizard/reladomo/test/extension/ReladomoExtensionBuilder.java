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

package io.liftwizard.reladomo.test.extension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ReladomoExtensionBuilder
        implements BeforeEachCallback, AfterEachCallback
{
    private Optional<ExecuteSqlExtension> executeSqlExtension = Optional.empty();
    private Optional<ReladomoInitializeExtension> initializeExtension = Optional.empty();
    private Optional<ReladomoPurgeAllExtension> purgeAllExtension = Optional.empty();
    private ReladomoLoadDataExtension loadDataExtension = new ReladomoLoadDataExtension();

    @Override
    public void beforeEach(ExtensionContext context)
            throws SQLException
    {
        if (this.executeSqlExtension.isPresent())
        {
            this.executeSqlExtension.get().beforeEach(context);
        }
        this.initializeExtension.ifPresent(extension -> extension.beforeEach(context));
        this.purgeAllExtension.ifPresent(extension -> extension.beforeEach(context));
        this.loadDataExtension.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context)
            throws SQLException
    {
        this.loadDataExtension.afterEach(context);
        this.purgeAllExtension.ifPresent(extension -> extension.afterEach(context));
        this.initializeExtension.ifPresent(extension -> extension.afterEach(context));
        if (this.executeSqlExtension.isPresent())
        {
            this.executeSqlExtension.get().afterEach(context);
        }
    }

    public ReladomoExtensionBuilder setRuntimeConfigurationPath(@Nonnull String runtimeConfigurationPath)
    {
        this.initializeExtension = Optional.of(new ReladomoInitializeExtension(runtimeConfigurationPath));
        return this;
    }

    public ReladomoExtensionBuilder setTestDataFileNames(@Nonnull String... testDataFileNames)
    {
        this.loadDataExtension = new ReladomoLoadDataExtension(testDataFileNames);
        return this;
    }

    public ReladomoExtensionBuilder setTestDataFileNames(@Nonnull ImmutableList<String> testDataFileNames)
    {
        this.loadDataExtension = new ReladomoLoadDataExtension(testDataFileNames);
        return this;
    }

    public ReladomoExtensionBuilder enableDropCreateTables()
    {
        if (this.executeSqlExtension.isEmpty())
        {
            this.executeSqlExtension = Optional.of(new ExecuteSqlExtension());
        }
        return this;
    }

    public ReladomoExtensionBuilder disableDropCreateTables()
    {
        this.executeSqlExtension = Optional.empty();
        return this;
    }

    public ReladomoExtensionBuilder setDdlLocationPattern(@Nonnull String ddlLocationPattern)
    {
        if (this.executeSqlExtension.isEmpty())
        {
            this.executeSqlExtension = Optional.of(new ExecuteSqlExtension());
        }
        this.executeSqlExtension.get().setDdlLocationPattern(ddlLocationPattern);
        return this;
    }

    public ReladomoExtensionBuilder setIdxLocationPattern(@Nonnull String idxLocationPattern)
    {
        if (this.executeSqlExtension.isEmpty())
        {
            this.executeSqlExtension = Optional.of(new ExecuteSqlExtension());
        }
        this.executeSqlExtension.get().setIdxLocationPattern(idxLocationPattern);
        return this;
    }

    public ReladomoExtensionBuilder setFkLocationPattern(@Nonnull String fkLocationPattern)
    {
        if (this.executeSqlExtension.isEmpty())
        {
            this.executeSqlExtension = Optional.of(new ExecuteSqlExtension());
        }
        this.executeSqlExtension.get().setFkLocationPattern(fkLocationPattern);
        return this;
    }

    public ReladomoExtensionBuilder setConnectionSupplier(@Nonnull Supplier<? extends Connection> connectionSupplier)
    {
        if (this.executeSqlExtension.isEmpty())
        {
            this.executeSqlExtension = Optional.of(new ExecuteSqlExtension());
        }
        this.executeSqlExtension.get().setConnectionSupplier(connectionSupplier);
        return this;
    }

    public ReladomoExtensionBuilder withPurgeAllExtension()
    {
        this.purgeAllExtension = Optional.of(new ReladomoPurgeAllExtension());
        return this;
    }

    public ReladomoExtensionBuilder withoutPurgeAllExtension()
    {
        this.purgeAllExtension = Optional.empty();
        return this;
    }
}
