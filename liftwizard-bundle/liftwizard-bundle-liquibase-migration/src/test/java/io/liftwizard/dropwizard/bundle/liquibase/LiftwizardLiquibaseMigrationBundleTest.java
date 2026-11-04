/*
 * Copyright 2026 Craig Motlin
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourcesFactory;
import io.liftwizard.dropwizard.configuration.liquibase.migration.LiquibaseDataSourceMigrationFactory;
import io.liftwizard.dropwizard.configuration.liquibase.migration.LiquibaseMigrationFactory;
import io.liftwizard.dropwizard.db.NamedDataSourceFactory;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiftwizardLiquibaseMigrationBundleTest
{
	private static final String DATA_SOURCE_NAME = "h2-test";
	private static final String MIGRATION_FILE = "liquibase-migration-bundle-test/migrations.xml";

	private final String jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
	private final StopTrackingDataSource dataSource = new StopTrackingDataSource(this.jdbcUrl);
	private final MetricRegistry metricRegistry = new MetricRegistry();
	private final LifecycleEnvironment lifecycle = new LifecycleEnvironment(this.metricRegistry);
	private final NamedDataSourcesFactory namedDataSourcesFactory = this.newNamedDataSourcesFactory();

	@Test
	void runMigratesWithoutStoppingSharedDataSource()
		throws Exception
	{
		this.runMigrations(false);

		assertThat(this.dataSource.isStopped()).isFalse();
		assertThat(this.tableExists()).isTrue();
		assertThat(this.getManagedObjects()).containsExactly(this.dataSource);
	}

	@Test
	void dropEntireSchemaOnStartupAndShutdownDropsSchemaOnShutdown()
		throws Exception
	{
		this.runMigrations(true);

		assertThat(this.dataSource.isStopped()).isFalse();
		assertThat(this.tableExists()).isTrue();

		List<Managed> managedObjects = this.getManagedObjects();
		assertThat(managedObjects).hasSize(2);
		assertThat(managedObjects.getFirst()).isSameAs(this.dataSource);
		assertThat(managedObjects.get(1)).isInstanceOf(LiquibaseDropAllManaged.class);

		// Dropwizard stops managed objects in reverse registration order.
		for (int i = managedObjects.size() - 1; i >= 0; i--)
		{
			managedObjects.get(i).stop();
		}

		assertThat(this.dataSource.isStopped()).isTrue();
		assertThat(this.tableExists()).isFalse();
	}

	private void runMigrations(boolean dropEntireSchemaOnStartupAndShutdown)
		throws Exception
	{
		var dataSourceMigrationFactory = new LiquibaseDataSourceMigrationFactory();
		dataSourceMigrationFactory.setDataSourceName(DATA_SOURCE_NAME);
		dataSourceMigrationFactory.setMigrationFileName(MIGRATION_FILE);

		var liquibaseMigrationFactory = new LiquibaseMigrationFactory();
		liquibaseMigrationFactory.setEnabled(true);
		liquibaseMigrationFactory.setDropEntireSchemaOnStartupAndShutdown(dropEntireSchemaOnStartupAndShutdown);
		liquibaseMigrationFactory.setDataSourceMigrations(List.of(dataSourceMigrationFactory));

		NamedDataSourceProvider dataSourceProvider = () -> this.namedDataSourcesFactory;
		new LiftwizardLiquibaseMigrationBundle().runMigrations(
			liquibaseMigrationFactory,
			dataSourceProvider,
			this.metricRegistry,
			this.lifecycle
		);
	}

	private NamedDataSourcesFactory newNamedDataSourcesFactory()
	{
		StopTrackingDataSource trackingDataSource = this.dataSource;
		NamedDataSourceFactory namedDataSourceFactory = new NamedDataSourceFactory()
		{
			@Override
			public ManagedDataSource build(MetricRegistry ignoredMetricRegistry)
			{
				return trackingDataSource;
			}
		};
		namedDataSourceFactory.setName(DATA_SOURCE_NAME);

		var result = new NamedDataSourcesFactory();
		result.setNamedDataSourceFactories(List.of(namedDataSourceFactory));
		return result;
	}

	private List<Managed> getManagedObjects()
	{
		MutableList<Managed> result = Lists.mutable.empty();
		for (Object managedObject : this.lifecycle.getManagedObjects())
		{
			if (managedObject instanceof JettyManaged jettyManaged)
			{
				result.add(jettyManaged.getManaged());
			}
		}
		return result;
	}

	private boolean tableExists()
		throws SQLException
	{
		try (
			Connection connection = DriverManager.getConnection(this.jdbcUrl);
			ResultSet tables = connection.getMetaData().getTables(null, null, "TEST_TABLE", null);
		)
		{
			return tables.next();
		}
	}

	private static final class StopTrackingDataSource
		implements ManagedDataSource
	{
		private final JdbcDataSource delegate = new JdbcDataSource();
		private boolean stopped;

		private StopTrackingDataSource(String jdbcUrl)
		{
			this.delegate.setURL(jdbcUrl);
		}

		@Override
		public Connection getConnection()
			throws SQLException
		{
			this.checkNotStopped();
			return this.delegate.getConnection();
		}

		@Override
		public Connection getConnection(String username, String password)
			throws SQLException
		{
			this.checkNotStopped();
			return this.delegate.getConnection(username, password);
		}

		private void checkNotStopped()
			throws SQLException
		{
			if (this.stopped)
			{
				throw new SQLException("Data source was stopped");
			}
		}

		@Override
		public PrintWriter getLogWriter()
		{
			return this.delegate.getLogWriter();
		}

		@Override
		public void setLogWriter(PrintWriter out)
		{
			this.delegate.setLogWriter(out);
		}

		@Override
		public void setLoginTimeout(int seconds)
		{
			this.delegate.setLoginTimeout(seconds);
		}

		@Override
		public int getLoginTimeout()
		{
			return this.delegate.getLoginTimeout();
		}

		@Override
		public Logger getParentLogger()
			throws SQLFeatureNotSupportedException
		{
			return this.delegate.getParentLogger();
		}

		@Override
		public <T> T unwrap(Class<T> iface)
			throws SQLException
		{
			return this.delegate.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface)
			throws SQLException
		{
			return this.delegate.isWrapperFor(iface);
		}

		@Override
		public void start()
		{
		}

		@Override
		public void stop()
		{
			this.stopped = true;
		}

		private boolean isStopped()
		{
			return this.stopped;
		}
	}
}
