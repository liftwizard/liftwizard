package com.liftwizard.dropwizard.bundle.httplogging;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import com.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class JerseyHttpLoggingBundle
        implements PrioritizedBundle<Object>
{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JerseyHttpLoggingBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        JerseyHttpLoggingFactoryProvider jerseyHttpLoggingFactoryProvider = this.safeCastConfiguration(
                JerseyHttpLoggingFactoryProvider.class,
                configuration);

        JerseyHttpLoggingFactory factory = jerseyHttpLoggingFactoryProvider.getJerseyHttpLoggingFactory();
        if (!factory.isEnabled())
        {
            LOGGER.info("{} disabled.", JerseyHttpLoggingBundle.class.getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", JerseyHttpLoggingBundle.class.getSimpleName());

        Level     level         = Level.parse(factory.getLevel());
        Verbosity verbosity     = Verbosity.valueOf(factory.getVerbosity());
        int       maxEntitySize = Math.toIntExact(factory.getMaxEntitySize().toBytes());

        Logger         logger         = Logger.getLogger(JerseyHttpLoggingBundle.class.getName());
        LoggingFeature loggingFeature = new LoggingFeature(logger, level, verbosity, maxEntitySize);
        environment.jersey().register(loggingFeature);

        LOGGER.info("Completing {}.", JerseyHttpLoggingBundle.class.getSimpleName());
    }
}
