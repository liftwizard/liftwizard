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

package io.liftwizard.reladomo.test.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraBusinessException;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.mithraruntime.MithraRuntimeType;
import com.gs.fw.common.mithra.util.MithraConfigurationManager;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ReladomoInitializeExtension
        implements BeforeEachCallback, AfterEachCallback
{
    @Nonnull
    private final String runtimeConfigurationPath;

    public ReladomoInitializeExtension(@Nonnull String runtimeConfigurationPath)
    {
        this.runtimeConfigurationPath = Objects.requireNonNull(runtimeConfigurationPath);
    }

    @Override
    public void beforeEach(ExtensionContext context)
    {
        try (
                InputStream inputStream = this
                        .getClass()
                        .getClassLoader()
                        .getResourceAsStream(this.runtimeConfigurationPath))
        {
            MithraConfigurationManager mithraConfigurationManager =
                    MithraManagerProvider.getMithraManager().getConfigManager();
            MithraRuntimeType mithraRuntimeType = mithraConfigurationManager.parseConfiguration(inputStream);
            mithraConfigurationManager.initializeRuntime(mithraRuntimeType);
            mithraConfigurationManager.fullyInitialize();
        }
        catch (MithraBusinessException | IOException e)
        {
            throw new RuntimeException(this.runtimeConfigurationPath, e);
        }
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        MithraManagerProvider.getMithraManager().clearAllQueryCaches();
        MithraManagerProvider.getMithraManager().cleanUpPrimaryKeyGenerators();
        MithraManagerProvider.getMithraManager().cleanUpRuntimeCacheControllers();
        MithraManagerProvider.getMithraManager().getConfigManager().resetAllInitializedClasses();
    }
}
