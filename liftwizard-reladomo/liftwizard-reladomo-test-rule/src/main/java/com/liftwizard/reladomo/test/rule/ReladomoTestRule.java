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

package com.liftwizard.reladomo.test.rule;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ReladomoTestRule
        implements TestRule
{
    @Nonnull
    private Optional<String> runtimeConfigurationPath = Optional.empty();

    @Nonnull
    private ImmutableList<String> testDataFileNames = Lists.immutable.empty();

    private boolean isDropCreateTablesEnabled;
    private String  ddlLocationPattern = ".*\\.ddl";
    private String  idxLocationPattern = ".*\\.idx";

    @Nonnull
    private Supplier<? extends SourcelessConnectionManager> connectionManager = H2InMemoryConnectionManager::getInstance;

    public ReladomoTestRule setRuntimeConfigurationPath(@Nonnull String runtimeConfigurationPath)
    {
        this.runtimeConfigurationPath = Optional.of(runtimeConfigurationPath);
        return this;
    }

    public ReladomoTestRule setTestDataFileNames(@Nonnull String... testDataFileNames)
    {
        this.testDataFileNames = Lists.immutable.with(testDataFileNames);
        return this;
    }

    public ReladomoTestRule setTestDataFileNames(@Nonnull ImmutableList<String> testDataFileNames)
    {
        this.testDataFileNames = Objects.requireNonNull(testDataFileNames);
        return this;
    }

    public ReladomoTestRule enableDropCreateTables()
    {
        this.isDropCreateTablesEnabled = true;
        return this;
    }

    public ReladomoTestRule disableDropCreateTables()
    {
        this.isDropCreateTablesEnabled = false;
        return this;
    }

    public ReladomoTestRule setDdlLocationPattern(@Nonnull String ddlLocationPattern)
    {
        this.ddlLocationPattern        = Objects.requireNonNull(ddlLocationPattern);
        this.isDropCreateTablesEnabled = true;
        return this;
    }

    public ReladomoTestRule setIdxLocationPattern(@Nonnull String idxLocationPattern)
    {
        this.idxLocationPattern        = Objects.requireNonNull(idxLocationPattern);
        this.isDropCreateTablesEnabled = true;
        return this;
    }

    public ReladomoTestRule setConnectionManager(@Nonnull Supplier<? extends SourcelessConnectionManager> connectionManager)
    {
        this.connectionManager = Objects.requireNonNull(connectionManager);
        return this;
    }

    @Nonnull
    @Override
    public Statement apply(@Nonnull Statement base, @Nonnull Description description)
    {
        ImmutableList<String> configuredTestDataFileNames = this.getConfiguredTestDataFileNames(description);

        return new ReladomoTestStatement(
                base,
                this.runtimeConfigurationPath,
                configuredTestDataFileNames,
                this.isDropCreateTablesEnabled,
                this.ddlLocationPattern,
                this.idxLocationPattern,
                this.connectionManager);
    }

    private ImmutableList<String> getConfiguredTestDataFileNames(@Nonnull Description description)
    {
        ReladomoTestFile reladomoTestFileAnnotation = description.getAnnotation(ReladomoTestFile.class);
        if (reladomoTestFileAnnotation == null)
        {
            return this.testDataFileNames;
        }
        return Lists.immutable.with(reladomoTestFileAnnotation.value());
    }
}
