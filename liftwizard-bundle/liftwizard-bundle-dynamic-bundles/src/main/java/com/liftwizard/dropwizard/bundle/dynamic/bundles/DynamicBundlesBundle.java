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

package com.liftwizard.dropwizard.bundle.dynamic.bundles;

import java.util.ServiceLoader;

import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public class DynamicBundlesBundle
        implements ConfiguredBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicBundlesBundle.class);

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.initializeWithMdc(bootstrap);
        }
    }

    private void initializeWithMdc(Bootstrap<?> bootstrap)
    {
        ServiceLoader<PrioritizedBundle> serviceLoader = ServiceLoader.load(PrioritizedBundle.class);
        ImmutableList<PrioritizedBundle> prioritizedBundles = Lists.immutable.withAll(serviceLoader)
                .toSortedListBy(PrioritizedBundle::getPriority)
                .toImmutable();

        if (prioritizedBundles.isEmpty())
        {
            LOGGER.warn("Didn't find any implementations of PrioritizedBundle using ServiceLoader.");
        }

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info(
                    "Found PrioritizedBundles using ServiceLoader:\n{}",
                    prioritizedBundles
                            .collect(this::getBundleString)
                            .makeString("\n"));
        }

        for (PrioritizedBundle bundle : prioritizedBundles)
        {
            bootstrap.addBundle(bundle);
        }
    }

    private String getBundleString(PrioritizedBundle bundle)
    {
        return String.format("    %s: %d", bundle.getClass().getSimpleName(), bundle.getPriority());
    }

    @Override
    public void run(Object configuration, Environment environment)
    {
    }
}
