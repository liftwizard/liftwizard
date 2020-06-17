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

package io.liftwizard.reladomo.test.rule;

import java.sql.Connection;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ReladomoTestRuleBuilder
{
    private       Optional<ExecuteSqlTestRule>         executeSqlTestRule = Optional.empty();
    private       Optional<ReladomoInitializeTestRule> initializeTestRule = Optional.empty();
    private final ReladomoPurgeAllTestRule             purgeAllTestRule   = new ReladomoPurgeAllTestRule();
    private       ReladomoLoadDataTestRule             loadDataTestRule   = new ReladomoLoadDataTestRule();

    public ReladomoTestRuleBuilder setRuntimeConfigurationPath(@Nonnull String runtimeConfigurationPath)
    {
        this.initializeTestRule = Optional.of(new ReladomoInitializeTestRule(runtimeConfigurationPath));
        return this;
    }

    public ReladomoTestRuleBuilder setTestDataFileNames(@Nonnull String... testDataFileNames)
    {
        this.loadDataTestRule = new ReladomoLoadDataTestRule(testDataFileNames);
        return this;
    }

    public ReladomoTestRuleBuilder setTestDataFileNames(@Nonnull ImmutableList<String> testDataFileNames)
    {
        this.loadDataTestRule = new ReladomoLoadDataTestRule(testDataFileNames);
        return this;
    }

    public ReladomoTestRuleBuilder enableDropCreateTables()
    {
        if (this.executeSqlTestRule.isEmpty())
        {
            this.executeSqlTestRule = Optional.of(new ExecuteSqlTestRule());
        }
        return this;
    }

    public ReladomoTestRuleBuilder disableDropCreateTables()
    {
        this.executeSqlTestRule = Optional.empty();
        return this;
    }

    public ReladomoTestRuleBuilder setDdlLocationPattern(@Nonnull String ddlLocationPattern)
    {
        if (this.executeSqlTestRule.isEmpty())
        {
            this.executeSqlTestRule = Optional.of(new ExecuteSqlTestRule());
        }
        this.executeSqlTestRule.get().setDdlLocationPattern(ddlLocationPattern);
        return this;
    }

    public ReladomoTestRuleBuilder setIdxLocationPattern(@Nonnull String idxLocationPattern)
    {
        if (this.executeSqlTestRule.isEmpty())
        {
            this.executeSqlTestRule = Optional.of(new ExecuteSqlTestRule());
        }
        this.executeSqlTestRule.get().setIdxLocationPattern(idxLocationPattern);
        return this;
    }

    public ReladomoTestRuleBuilder setConnectionSupplier(@Nonnull Supplier<? extends Connection> connectionSupplier)
    {
        if (this.executeSqlTestRule.isEmpty())
        {
            this.executeSqlTestRule = Optional.of(new ExecuteSqlTestRule());
        }
        this.executeSqlTestRule.get().setConnectionSupplier(connectionSupplier);
        return this;
    }

    public TestRule build()
    {
        return RuleChain.emptyRuleChain()
                .around(this.executeSqlTestRule.map(RuleChain::outerRule).orElseGet(RuleChain::emptyRuleChain))
                .around(this.initializeTestRule.map(RuleChain::outerRule).orElseGet(RuleChain::emptyRuleChain))
                .around(this.purgeAllTestRule)
                .around(this.loadDataTestRule);
    }
}
