package com.liftwizard.dropwizard.bundle.cors;

import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.cors.CorsFactory;
import com.liftwizard.dropwizard.configuration.cors.CorsFactoryProvider;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class CorsBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        CorsFactoryProvider corsFactoryProvider = this.safeCastConfiguration(CorsFactoryProvider.class, configuration);
        CorsFactory         corsFactory         = corsFactoryProvider.getCorsFactory();
        if (!corsFactory.isEnabled())
        {
            LOGGER.info("{} disabled.", CorsBundle.class.getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", CorsBundle.class.getSimpleName());

        // https://stackoverflow.com/a/25801822/23572
        Dynamic cors = environment.servlets().addFilter(corsFactory.getFilterName(), CrossOriginFilter.class);

        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, corsFactory.getAllowedOrigins());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, corsFactory.getAllowedHeaders());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, corsFactory.getAllowedMethods());
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, corsFactory.getAllowCredentials());
        cors.addMappingForUrlPatterns(
                EnumSet.allOf(DispatcherType.class),
                true,
                corsFactory.getUrlPatterns().toArray(new String[]{}));

        LOGGER.info("Completing {}.", CorsBundle.class.getSimpleName());
    }
}
