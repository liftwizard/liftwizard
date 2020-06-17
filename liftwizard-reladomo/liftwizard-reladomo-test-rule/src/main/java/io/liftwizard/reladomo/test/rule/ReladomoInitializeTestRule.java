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

package io.liftwizard.reladomo.test.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraBusinessException;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.mithraruntime.MithraRuntimeType;
import com.gs.fw.common.mithra.util.MithraConfigurationManager;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ReladomoInitializeTestRule
        implements TestRule
{
    @Nonnull
    private final String runtimeConfigurationPath;

    public ReladomoInitializeTestRule(@Nonnull String runtimeConfigurationPath)
    {
        this.runtimeConfigurationPath = Objects.requireNonNull(runtimeConfigurationPath);
    }

    @Nonnull
    @Override
    public Statement apply(@Nonnull Statement base, @Nonnull Description description)
    {
        return new ReadRuntimeConfigurationStatement(
                base,
                this.runtimeConfigurationPath);
    }

    public static class ReadRuntimeConfigurationStatement
            extends Statement
    {
        private final Statement base;
        private final String    runtimeConfigurationPath;

        public ReadRuntimeConfigurationStatement(
                @Nonnull Statement base,
                @Nonnull String runtimeConfigurationPath)
        {
            this.base                     = Objects.requireNonNull(base);
            this.runtimeConfigurationPath = Objects.requireNonNull(runtimeConfigurationPath);
        }

        private void before()
        {
            try (
                    InputStream inputStream = ReladomoTestRuleBuilder.class.getClassLoader()
                            .getResourceAsStream(this.runtimeConfigurationPath))
            {
                MithraConfigurationManager mithraConfigurationManager =
                        MithraManagerProvider.getMithraManager().getConfigManager();
                MithraRuntimeType mithraRuntimeType = mithraConfigurationManager.parseConfiguration(inputStream);
                mithraConfigurationManager.initializeRuntime(mithraRuntimeType);
                mithraConfigurationManager.fullyInitialize();
            }
            catch (MithraBusinessException e)
            {
                throw new RuntimeException(this.runtimeConfigurationPath, e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        private void after()
        {
            MithraManagerProvider.getMithraManager().clearAllQueryCaches();
            MithraManagerProvider.getMithraManager().cleanUpPrimaryKeyGenerators();
            MithraManagerProvider.getMithraManager().cleanUpRuntimeCacheControllers();
            MithraManagerProvider.getMithraManager().getConfigManager().resetAllInitializedClasses();
        }

        @Override
        public void evaluate() throws Throwable
        {
            this.before();
            try
            {
                this.base.evaluate();
            }
            finally
            {
                this.after();
            }
        }
    }
}
