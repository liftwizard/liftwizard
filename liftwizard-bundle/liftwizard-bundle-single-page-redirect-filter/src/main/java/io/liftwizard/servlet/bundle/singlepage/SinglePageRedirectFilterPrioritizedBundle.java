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

package io.liftwizard.servlet.bundle.singlepage;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.servlet.config.singlepage.SinglePageRedirectFilterFactory;
import io.liftwizard.servlet.config.singlepage.SinglePageRedirectFilterFactoryProvider;

@AutoService(PrioritizedBundle.class)
public class SinglePageRedirectFilterPrioritizedBundle
        implements PrioritizedBundle
{
    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        SinglePageRedirectFilterFactoryProvider factoryProvider = this.safeCastConfiguration(
                SinglePageRedirectFilterFactoryProvider.class,
                configuration);

        SinglePageRedirectFilterFactory factory = factoryProvider.getSinglePageRedirectFilterFactory();
        SinglePageRedirectFilterBundle.handleRegistration(this, environment, factory);
    }
}
