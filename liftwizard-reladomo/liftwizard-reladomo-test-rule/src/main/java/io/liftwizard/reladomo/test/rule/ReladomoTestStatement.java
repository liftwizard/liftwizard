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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraBusinessException;
import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.MithraDatabaseObject;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.mithraruntime.MithraRuntimeType;
import com.gs.fw.common.mithra.test.MithraTestDataParser;
import com.gs.fw.common.mithra.util.MithraConfigurationManager;
import com.gs.fw.common.mithra.util.fileparser.MithraParsedData;
import io.liftwizard.reladomo.ddl.executor.DatabaseDdlExecutor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.runners.model.Statement;

public class ReladomoTestStatement
        extends Statement
{
    private static final Class<?>[] NO_PARAMS = {};
    private static final Object[]   NO_ARGS   = {};

    private final Statement             base;
    private final ImmutableList<String> configuredTestDataFileNames;
    private final Optional<String>      runtimeConfigurationPath;
    private final boolean               isDropCreateTablesEnabled;
    private final String                ddlLocationPattern;
    private final String                idxLocationPattern;

    private final Supplier<? extends SourcelessConnectionManager> connectionManager;

    public ReladomoTestStatement(
            @Nonnull Statement base,
            @Nonnull Optional<String> runtimeConfigurationPath,
            @Nonnull ImmutableList<String> configuredTestDataFileNames,
            boolean isDropCreateTablesEnabled,
            @Nonnull String ddlLocationPattern,
            @Nonnull String idxLocationPattern,
            @Nonnull Supplier<? extends SourcelessConnectionManager> connectionManager)
    {
        this.base                        = Objects.requireNonNull(base);
        this.runtimeConfigurationPath    = Objects.requireNonNull(runtimeConfigurationPath);
        this.configuredTestDataFileNames = Objects.requireNonNull(configuredTestDataFileNames);
        this.isDropCreateTablesEnabled   = isDropCreateTablesEnabled;
        this.ddlLocationPattern          = Objects.requireNonNull(ddlLocationPattern);
        this.idxLocationPattern          = Objects.requireNonNull(idxLocationPattern);
        this.connectionManager           = Objects.requireNonNull(connectionManager);
    }

    private void before()
            throws SQLException
    {
        this.createTables();
        this.readRuntimeConfiguration();
        this.loadTestData();
    }

    private void createTables() throws SQLException
    {
        if (!this.isDropCreateTablesEnabled)
        {
            return;
        }

        SourcelessConnectionManager sourcelessConnectionManager = this.connectionManager.get();
        try (Connection connection = sourcelessConnectionManager.getConnection())
        {
            DatabaseDdlExecutor.executeSql(connection, this.ddlLocationPattern, this.idxLocationPattern);
        }
    }

    private void readRuntimeConfiguration()
    {
        this.runtimeConfigurationPath.ifPresent(ReladomoTestStatement::readRuntimeConfiguration);
    }

    private static void readRuntimeConfiguration(String runtimeConfigurationPath)
    {
        try (
                InputStream inputStream = ReladomoTestRule.class.getClassLoader()
                        .getResourceAsStream(runtimeConfigurationPath))
        {
            MithraConfigurationManager mithraConfigurationManager =
                    MithraManagerProvider.getMithraManager().getConfigManager();
            MithraRuntimeType mithraRuntimeType = mithraConfigurationManager.parseConfiguration(inputStream);
            mithraConfigurationManager.initializeRuntime(mithraRuntimeType);
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

    private void loadTestData()
    {
        for (String testDataFileName : this.configuredTestDataFileNames)
        {
            try
            {
                this.loadTestData(testDataFileName);
            }
            catch (ReflectiveOperationException e)
            {
                throw new RuntimeException(testDataFileName, e);
            }
        }
    }

    private void loadTestData(String testDataFileName)
            throws ReflectiveOperationException
    {
        MithraTestDataParser   parser         = new MithraTestDataParser(testDataFileName);
        List<MithraParsedData> parsedDataList = parser.getResults();

        for (MithraParsedData mithraParsedData : parsedDataList)
        {
            this.handleMithraParsedData(mithraParsedData);
        }
    }

    private void handleMithraParsedData(MithraParsedData mithraParsedData)
            throws ReflectiveOperationException
    {
        List<Attribute<?, ?>>  attributes      = mithraParsedData.getAttributes();
        List<MithraDataObject> dataObjects     = mithraParsedData.getDataObjects();
        String                 parsedClassName = mithraParsedData.getParsedClassName();

        if (!MithraManagerProvider.getMithraManager().getConfigManager().isClassConfigured(parsedClassName))
        {
            throw new AssertionError();
        }

        String             finderClassName    = parsedClassName + "Finder";
        Class<?>           finderClass        = Class.forName(finderClassName);
        Method             method             = finderClass.getMethod("getMithraObjectPortal", NO_PARAMS);
        MithraObjectPortal mithraObjectPortal = (MithraObjectPortal) method.invoke(null, NO_ARGS);

        MithraDatabaseObject databaseObject = mithraObjectPortal.getDatabaseObject();
        SourcelessConnectionManager databaseObjectConnectionManager =
                (SourcelessConnectionManager) databaseObject.getConnectionManager();
        if (databaseObjectConnectionManager != this.connectionManager.get())
        {
            // TODO: This is a second channel to get the connection manager
            throw new AssertionError();
        }

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
