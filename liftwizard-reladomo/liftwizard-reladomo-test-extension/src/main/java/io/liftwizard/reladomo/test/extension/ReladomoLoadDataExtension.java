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

package io.liftwizard.reladomo.test.extension;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReladomoLoadDataExtension
        implements BeforeEachCallback, AfterEachCallback
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoLoadDataExtension.class);

    private static final Class<?>[] NO_PARAMS = {};
    private static final Object[] NO_ARGS = {};

    @Nonnull
    private final ImmutableList<String> testDataFileNames;

    public ReladomoLoadDataExtension(@Nonnull String... testDataFileNames)
    {
        this(Lists.immutable.with(testDataFileNames));
    }

    public ReladomoLoadDataExtension(@Nonnull ImmutableList<String> testDataFileNames)
    {
        this.testDataFileNames = testDataFileNames;
    }

    @Override
    public void beforeEach(ExtensionContext context)
    {
        ImmutableList<String> configuredTestDataFileNames = this.getConfiguredTestDataFileNames(context);

        for (String testDataFileName : configuredTestDataFileNames)
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

    private ImmutableList<String> getConfiguredTestDataFileNames(@Nonnull ExtensionContext context)
    {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isEmpty())
        {
            return this.testDataFileNames;
        }

        ReladomoTestFile reladomoTestFileAnnotation = element.get().getAnnotation(ReladomoTestFile.class);
        if (reladomoTestFileAnnotation == null)
        {
            return this.testDataFileNames;
        }
        return Lists.immutable.with(reladomoTestFileAnnotation.value());
    }

    private void loadTestData(String testDataFileName)
            throws ReflectiveOperationException
    {
        LOGGER.debug("Loading test data from file: {}", testDataFileName);
        MithraTestDataParser parser = new MithraTestDataParser(testDataFileName);
        List<MithraParsedData> parsedDataList = parser.getResults();

        for (MithraParsedData mithraParsedData : parsedDataList)
        {
            this.handleMithraParsedData(mithraParsedData);
        }
    }

    private void handleMithraParsedData(@Nonnull MithraParsedData mithraParsedData)
            throws ReflectiveOperationException
    {
        List<Attribute<?, ?>> attributes = mithraParsedData.getAttributes();
        List<MithraDataObject> dataObjects = mithraParsedData.getDataObjects();
        String parsedClassName = mithraParsedData.getParsedClassName();

        if (!MithraManagerProvider.getMithraManager().getConfigManager().isClassConfigured(parsedClassName))
        {
            throw new RuntimeException("Class "
                    + parsedClassName
                    + " is not configured. Did you remember to run ReladomoReadRuntimeConfigurationTestRule?");
        }

        String finderClassName = parsedClassName + "Finder";
        Class<?> finderClass = Class.forName(finderClassName);
        Method method = finderClass.getMethod("getMithraObjectPortal", NO_PARAMS);
        MithraObjectPortal mithraObjectPortal = (MithraObjectPortal) method.invoke(null, NO_ARGS);

        MithraDatabaseObject databaseObject = mithraObjectPortal.getDatabaseObject();
        SourcelessConnectionManager databaseObjectConnectionManager =
                (SourcelessConnectionManager) databaseObject.getConnectionManager();

        LOGGER.debug(
                "Loading test data for class {} using connection manager: {}",
                parsedClassName,
                databaseObjectConnectionManager);

        Class<? extends MithraDatabaseObject> databaseObjectClass = databaseObject.getClass();

        Method insertDataMethod = databaseObjectClass.getMethod("insertData", List.class, List.class, Object.class);
        insertDataMethod.invoke(databaseObject, attributes, dataObjects, null);
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
