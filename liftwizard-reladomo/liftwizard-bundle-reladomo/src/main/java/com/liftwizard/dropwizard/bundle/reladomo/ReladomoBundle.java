package com.liftwizard.dropwizard.bundle.reladomo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import com.codahale.metrics.MetricRegistry;
import com.google.auto.service.AutoService;
import com.gs.fw.common.mithra.MithraBusinessException;
import com.gs.fw.common.mithra.MithraManager;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.reladomo.ReladomoFactory;
import com.liftwizard.dropwizard.configuration.reladomo.ReladomoFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class ReladomoBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoBundle.class);

    @Override
    public int getPriority()
    {
        return -3;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        ReladomoFactoryProvider reladomoFactoryProvider = this.safeCastConfiguration(
                ReladomoFactoryProvider.class,
                configuration);

        LOGGER.info("Running {}.", ReladomoBundle.class.getSimpleName());

        ReladomoFactory reladomoFactory = reladomoFactoryProvider.getReladomoFactory();

        Duration transactionTimeout = reladomoFactory.getTransactionTimeout();
        int      transactionTimeoutSeconds = Math.toIntExact(transactionTimeout.toSeconds());
        ReladomoBundle.setTransactionTimeout(transactionTimeoutSeconds);
        // Notification should be configured here. Refer to notification/Notification.html under reladomo-javadoc.jar.

        List<String> runtimeConfigurationPaths = reladomoFactory.getRuntimeConfigurationPaths();
        runtimeConfigurationPaths.forEach(ReladomoBundle::loadRuntimeConfiguration);

        boolean captureTransactionLevelPerformanceData =
                reladomoFactory.isCaptureTransactionLevelPerformanceData();
        ReladomoBundle.setCaptureTransactionLevelPerformanceData(captureTransactionLevelPerformanceData);

        boolean enableRetrieveCountMetrics = reladomoFactory.isEnableRetrieveCountMetrics();
        if (enableRetrieveCountMetrics)
        {
            ReladomoBundle.registerRetrieveCountMetrics(environment.metrics());
        }

        environment.lifecycle().manage(new ManagedReladomoCleanup());

        LOGGER.info("Completing {}.", ReladomoBundle.class.getSimpleName());
    }

    private static void registerRetrieveCountMetrics(MetricRegistry metricRegistry)
    {
        metricRegistry.gauge(
                "Reladomo database retrieve count",
                () -> () -> MithraManagerProvider.getMithraManager().getDatabaseRetrieveCount());
        metricRegistry.gauge(
                "Reladomo remote retrieve count",
                () -> () -> MithraManagerProvider.getMithraManager().getRemoteRetrieveCount());
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

    private static void loadRuntimeConfiguration(String runtimeConfigurationPath)
    {
        LOGGER.info("Loading Reladomo configuration XML: {}", runtimeConfigurationPath);
        try (
                InputStream inputStream = ReladomoBundle.class.getClassLoader()
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
