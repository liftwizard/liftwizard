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

package io.liftwizard.dropwizard.configuration.reladomo;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;

public class ReladomoFactory
{
    private          int          defaultRelationshipCacheSize           = 10_000;
    private          int          defaultMinQueriesToKeep                = 32;
    // Something like 30 seconds to 2 minutes makes sense in production
    private          Duration     transactionTimeout                     = Duration.minutes(5);
    // reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml in production
    private @NotNull List<String> runtimeConfigurationPaths              = List.of(
            "reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml");
    private          boolean      enableRetrieveCountMetrics             = true;
    private          boolean      captureTransactionLevelPerformanceData = true;

    @JsonProperty
    public int getDefaultMinQueriesToKeep()
    {
        return this.defaultMinQueriesToKeep;
    }

    @JsonProperty
    public void setDefaultMinQueriesToKeep(int defaultMinQueriesToKeep)
    {
        this.defaultMinQueriesToKeep = defaultMinQueriesToKeep;
    }

    @JsonProperty
    public int getDefaultRelationshipCacheSize()
    {
        return this.defaultRelationshipCacheSize;
    }

    @JsonProperty
    public void setDefaultRelationshipCacheSize(int defaultRelationshipCacheSize)
    {
        this.defaultRelationshipCacheSize = defaultRelationshipCacheSize;
    }

    @JsonProperty
    public Duration getTransactionTimeout()
    {
        return this.transactionTimeout;
    }

    @JsonProperty
    public void setTransactionTimeout(Duration transactionTimeout)
    {
        this.transactionTimeout = transactionTimeout;
    }

    @JsonProperty
    public List<String> getRuntimeConfigurationPaths()
    {
        return this.runtimeConfigurationPaths;
    }

    @JsonProperty
    public void setRuntimeConfigurationPaths(List<String> runtimeConfigurationPaths)
    {
        this.runtimeConfigurationPaths = Collections.unmodifiableList(runtimeConfigurationPaths);
    }

    @JsonProperty
    public boolean isEnableRetrieveCountMetrics()
    {
        return this.enableRetrieveCountMetrics;
    }

    @JsonProperty
    public void setEnableRetrieveCountMetrics(boolean enableRetrieveCountMetrics)
    {
        this.enableRetrieveCountMetrics = enableRetrieveCountMetrics;
    }

    @JsonProperty
    public boolean isCaptureTransactionLevelPerformanceData()
    {
        return this.captureTransactionLevelPerformanceData;
    }

    @JsonProperty
    public void setCaptureTransactionLevelPerformanceData(boolean captureTransactionLevelPerformanceData)
    {
        this.captureTransactionLevelPerformanceData = captureTransactionLevelPerformanceData;
    }
}
