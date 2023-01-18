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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.MithraDatabaseObject;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.test.MithraTestDataParser;
import com.gs.fw.common.mithra.util.fileparser.MithraParsedData;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReladomoLoadDataTestRule
        implements TestRule
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoLoadDataTestRule.class);

    @Nonnull
    private final ImmutableList<String> testDataFileNames;

    public ReladomoLoadDataTestRule(@Nonnull ImmutableList<String> testDataFileNames)
    {
        this.testDataFileNames = testDataFileNames;
    }

    public ReladomoLoadDataTestRule(@Nonnull String... testDataFileNames)
    {
        this(Lists.immutable.with(testDataFileNames));
    }

    @Nonnull
    @Override
    public Statement apply(@Nonnull Statement base, @Nonnull Description description)
    {
        ImmutableList<String> configuredTestDataFileNames = this.getConfiguredTestDataFileNames(description);

        return new LoadDataStatement(base, configuredTestDataFileNames);
    }

    private ImmutableList<String> getConfiguredTestDataFileNames(@Nonnull Description description)
    {
        ReladomoTestFile reladomoTestFileAnnotation = description.getAnnotation(ReladomoTestFile.class);
        if (reladomoTestFileAnnotation == null)
        {
            return this.testDataFileNames;
        }
        return Lists.immutable.with(reladomoTestFileAnnotation.value());
    }

    public static class LoadDataStatement
            extends Statement
    {
        private static final Class<?>[] NO_PARAMS = {};
        private static final Object[]   NO_ARGS   = {};

        private final Statement             base;
        private final ImmutableList<String> configuredTestDataFileNames;

        public LoadDataStatement(
                @Nonnull Statement base,
                @Nonnull ImmutableList<String> configuredTestDataFileNames)
        {
            this.base                        = Objects.requireNonNull(base);
            this.configuredTestDataFileNames = Objects.requireNonNull(configuredTestDataFileNames);
        }

        private void before()
        {
            for (String testDataFileName : this.configuredTestDataFileNames)
            {
                try
                {
                    this.loadTestData(testDataFileName);
                }
                catch (ReflectiveOperationException e)
                {
                    throw new RuntimeException("Error while loading test data file: " + testDataFileName, e);
                }
            }
        }

        private void loadTestData(String testDataFileName)
                throws ReflectiveOperationException
        {
            LOGGER.debug("Loading test data from file: {}", testDataFileName);
            MithraTestDataParser   parser         = new MithraTestDataParser(testDataFileName);
            List<MithraParsedData> parsedDataList = parser.getResults();

            for (MithraParsedData mithraParsedData : parsedDataList)
            {
                this.handleMithraParsedData(mithraParsedData);
            }
        }

        private void handleMithraParsedData(@Nonnull MithraParsedData mithraParsedData)
                throws ReflectiveOperationException
        {
            List<Attribute<?, ?>>  attributes      = mithraParsedData.getAttributes();
            List<MithraDataObject> dataObjects     = mithraParsedData.getDataObjects();
            String                 parsedClassName = mithraParsedData.getParsedClassName();

            if (!MithraManagerProvider.getMithraManager().getConfigManager().isClassConfigured(parsedClassName))
            {
                throw new RuntimeException("Class " + parsedClassName + " is not configured. Did you remember to run ReladomoReadRuntimeConfigurationTestRule?");
            }

            String             finderClassName    = parsedClassName + "Finder";
            Class<?>           finderClass        = Class.forName(finderClassName);
            Method             method             = finderClass.getMethod("getMithraObjectPortal", NO_PARAMS);
            MithraObjectPortal mithraObjectPortal = (MithraObjectPortal) method.invoke(null, NO_ARGS);

            MithraDatabaseObject databaseObject = mithraObjectPortal.getDatabaseObject();
            SourcelessConnectionManager databaseObjectConnectionManager =
                    (SourcelessConnectionManager) databaseObject.getConnectionManager();

            LOGGER.debug("Loading test data for class {} using connection manager: {}", parsedClassName, databaseObjectConnectionManager);

            Class<? extends MithraDatabaseObject> databaseObjectClass = databaseObject.getClass();

            Method insertDataMethod = databaseObjectClass.getMethod("insertData", List.class, List.class, Object.class);
            insertDataMethod.invoke(databaseObject, attributes, dataObjects, null);
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
