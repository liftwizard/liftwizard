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

package io.liftwizard.dropwizard.bundle.reladomo;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.codahale.metrics.MetricRegistry;
import com.google.auto.service.AutoService;
import com.gs.fw.common.mithra.MithraBusinessException;
import com.gs.fw.common.mithra.MithraManager;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.util.DefaultInfinityTimestamp;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactory;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class ReladomoBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoBundle.class);

    @Override
    public int getPriority()
    {
        return -3;
    }

    @Override
    public void runWithMdc(
            @Nonnull Object configuration,
            @Nonnull Environment environment)
    {
        ReladomoFactoryProvider reladomoFactoryProvider = this.safeCastConfiguration(
                ReladomoFactoryProvider.class,
                configuration);

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        ReladomoBundle.assertTimezoneUTC();

        ReladomoFactory reladomoFactory = reladomoFactoryProvider.getReladomoFactory();

        int defaultMinQueriesToKeep = reladomoFactory.getDefaultMinQueriesToKeep();
        ReladomoBundle.setDefaultMinQueriesToKeep(defaultMinQueriesToKeep);
        int defaultRelationshipCacheSize = reladomoFactory.getDefaultRelationshipCacheSize();
        ReladomoBundle.setDefaultRelationshipCacheSize(defaultRelationshipCacheSize);

        Duration transactionTimeout        = reladomoFactory.getTransactionTimeout();
        int      transactionTimeoutSeconds = Math.toIntExact(transactionTimeout.toSeconds());
        ReladomoBundle.setTransactionTimeout(transactionTimeoutSeconds);
        // Notification should be configured here. Refer to notification/Notification.html under reladomo-javadoc.jar.

        List<String> runtimeConfigurationPaths = reladomoFactory.getRuntimeConfigurationPaths();
        runtimeConfigurationPaths.forEach(this::loadRuntimeConfiguration);

        boolean captureTransactionLevelPerformanceData =
                reladomoFactory.isCaptureTransactionLevelPerformanceData();
        ReladomoBundle.setCaptureTransactionLevelPerformanceData(captureTransactionLevelPerformanceData);

        boolean enableRetrieveCountMetrics = reladomoFactory.isEnableRetrieveCountMetrics();
        if (enableRetrieveCountMetrics)
        {
            this.registerRetrieveCountMetrics(environment.metrics());
        }
        MithraManagerProvider.getMithraManager().fullyInitialize();

        environment.lifecycle().manage(new ManagedReladomoCleanup());

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }

    private static void assertTimezoneUTC()
    {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        if (!defaultTimeZone.equals(TimeZone.getTimeZone("UTC")))
        {
            LOGGER.warn(
                    "Expected default TimeZone to be UTC, but was: {}.",
                    defaultTimeZone);
        }

        long expectedInfinityMilli = LocalDateTime.of(9999, 12, 1, 23, 59, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        long actualInfinityMilli = DefaultInfinityTimestamp.getDefaultInfinity().getTime();

        if (actualInfinityMilli != expectedInfinityMilli)
        {
            long difference = actualInfinityMilli - expectedInfinityMilli;
            long offset     = difference / (1000 * 60 * 60);

            LOGGER.warn(
                    "Expected default Infinity to be {}, but was: {}. Offset: {} hours.",
                    expectedInfinityMilli,
                    actualInfinityMilli,
                    offset);
        }
    }

    private void registerRetrieveCountMetrics(MetricRegistry metricRegistry)
    {
        metricRegistry.gauge(
                MetricRegistry.name(this.getClass(), "DatabaseRetrieveCount"),
                () -> MithraManagerProvider.getMithraManager()::getDatabaseRetrieveCount);
        metricRegistry.gauge(
                MetricRegistry.name(this.getClass(), "RemoteRetrieveCount"),
                () -> MithraManagerProvider.getMithraManager()::getRemoteRetrieveCount);
    }

    private static void setDefaultRelationshipCacheSize(int defaultRelationshipCacheSize)
    {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setDefaultRelationshipCacheSize(defaultRelationshipCacheSize);
    }

    private static void setDefaultMinQueriesToKeep(int defaultMinQueriesToKeep)
    {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setDefaultMinQueriesToKeep(defaultMinQueriesToKeep);
    }

    private static void setTransactionTimeout(int transactionTimeoutSeconds)
    {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setTransactionTimeout(transactionTimeoutSeconds);
    }

    private static void setCaptureTransactionLevelPerformanceData(boolean captureTransactionLevelPerformanceData)
    {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setCaptureTransactionLevelPerformanceData(captureTransactionLevelPerformanceData);
    }

    private void loadRuntimeConfiguration(String runtimeConfigurationPath)
    {
        LOGGER.info("Loading Reladomo configuration XML: {}", runtimeConfigurationPath);
        try (
                InputStream inputStream = this.getClass().getClassLoader()
                        .getResourceAsStream(runtimeConfigurationPath))
        {
            MithraManagerProvider.getMithraManager().readConfiguration(inputStream);
        }
        catch (MithraBusinessException e)
        {
            throw new RuntimeException(runtimeConfigurationPath, e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
