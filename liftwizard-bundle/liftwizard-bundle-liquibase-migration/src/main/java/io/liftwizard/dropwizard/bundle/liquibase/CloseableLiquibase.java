/*
 * Copyright 2022 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.liquibase;

import io.dropwizard.db.ManagedDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

public class CloseableLiquibase
        extends Liquibase
{
    private final ManagedDataSource dataSource;

    public CloseableLiquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database, ManagedDataSource dataSource)
    {
        super(changeLogFile, resourceAccessor, database);
        this.dataSource = dataSource;
    }

    @Override
    public void close()
            throws LiquibaseException
    {
        try
        {
            this.database.close();
        }
        finally
        {
            try
            {
                this.dataSource.stop();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
