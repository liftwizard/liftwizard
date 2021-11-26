/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.healthcheck.commonpool;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.config.healthcheck.commonpool.CommonPoolHealthCheckFactory;
import io.liftwizard.dropwizard.config.healthcheck.commonpool.CommonPoolHealthCheckFactoryProvider;
import io.liftwizard.dropwizard.healthcheck.commonpool.CommonPoolHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class CommonPoolHealthCheckBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonPoolHealthCheckBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        CommonPoolHealthCheckFactoryProvider factoryProvider = this.safeCastConfiguration(
                CommonPoolHealthCheckFactoryProvider.class,
                configuration);
        CommonPoolHealthCheckFactory factory =
                factoryProvider.getCommonPoolHealthCheckFactory();
        if (!factory.isEnabled())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        CommonPoolHealthCheck healthCheck = new CommonPoolHealthCheck(
                factory.getThreadNamePrefix(),
                factory.getThreadStates(),
                factory.getAlwaysAllowedPatterns(),
                factory.getBannedPatterns());
        environment.healthChecks().register("common-pool", healthCheck);

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }
}
