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

package io.liftwizard.reladomo.ddl.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.set.mutable.SetAdapter;
import org.h2.tools.RunScript;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseDdlExecutor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDdlExecutor.class);

    private DatabaseDdlExecutor()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static void executeSql(
            Connection connection,
            String ddlLocationPattern,
            String idxLocationPattern,
            String fkLocationPattern)
    {
        MutableSet<URL> urls = Sets.mutable
                // Maven's classpath, including maven itself, appears here
                .withAll(ClasspathHelper.forJavaClassPath())
                // The "usual" classpath appears here
                .withAll(ClasspathHelper.forClassLoader());
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setScanners(new ResourcesScanner())
                .setUrls(urls);
        Reflections        reflections  = new Reflections(configurationBuilder);
        MutableSet<String> ddlLocations = SetAdapter.adapt(reflections.getResources(Pattern.compile(ddlLocationPattern)));
        MutableSet<String> idxLocations = SetAdapter.adapt(reflections.getResources(Pattern.compile(idxLocationPattern)));
        MutableSet<String> fkLocations  = SetAdapter.adapt(reflections.getResources(Pattern.compile(fkLocationPattern)));
        LOGGER.info("Scanning urls: {}", urls.collect(URL::toString).toSortedList());
        LOGGER.info("Found {} SQL ddl scripts.", ddlLocations.size());
        LOGGER.info("Found {} SQL idx scripts.", idxLocations.size());
        LOGGER.info("Found {} SQL fk scripts.", fkLocations.size());

        ddlLocations.forEachWith(DatabaseDdlExecutor::runScript, connection);
        idxLocations.forEachWith(DatabaseDdlExecutor::runScript, connection);
        fkLocations.forEachWith(DatabaseDdlExecutor::runScript, connection);
    }

    private static void runScript(String ddlLocation, @Nonnull Connection connection)
    {
        LOGGER.debug("Running SQL script: {}", ddlLocation);

        InputStream inputStream = DatabaseDdlExecutor.class.getResourceAsStream("/" + ddlLocation);
        if (inputStream == null)
        {
            String message = String.format("Could not find sql script '%s' on classpath.", ddlLocation);
            throw new RuntimeException(message);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            RunScript.execute(connection, reader);
        }
        catch (@Nonnull IOException | SQLException e)
        {
            LOGGER.error("Failed to run sql script {}.", ddlLocation, e);
            throw new RuntimeException(e);
        }
    }
}
