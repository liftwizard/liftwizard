package com.liftwizard.dropwizard.configuration.reladomo;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import org.eclipse.collections.impl.factory.Lists;

public class ReladomoFactory
{
    // Something like 30 seconds to 2 minutes makes sense in production
    private          Duration     transactionTimeout                     = Duration.minutes(5);
    // reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml in production
    private @NotNull List<String> runtimeConfigurationPaths              = Arrays.asList(
            "reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml");
    private          boolean      enableRetrieveCountMetrics             = true;
    private          boolean      captureTransactionLevelPerformanceData = true;

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
        return Lists.mutable.withAll(this.runtimeConfigurationPaths);
    }

    @JsonProperty
    public void setRuntimeConfigurationPaths(List<String> runtimeConfigurationPaths)
    {
        this.runtimeConfigurationPaths = runtimeConfigurationPaths;
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
