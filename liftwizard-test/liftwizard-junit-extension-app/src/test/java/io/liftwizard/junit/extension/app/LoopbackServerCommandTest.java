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

package io.liftwizard.junit.extension.app;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoopbackServerCommandTest {

	@Test
	void defaultServerFactoryBindsLoopbackByDefault() throws Exception {
		var extension = new LiftwizardAppExtension<>(
			TestApplication.class,
			ResourceHelpers.resourceFilePath("test-config.yml")
		);
		extension.before();
		try {
			var serverFactory = (DefaultServerFactory) extension.getConfiguration().getServerFactory();
			var applicationConnector = (HttpConnectorFactory) serverFactory.getApplicationConnectors().get(0);
			var adminConnector = (HttpConnectorFactory) serverFactory.getAdminConnectors().get(0);
			assertThat(applicationConnector.getBindHost()).isEqualTo("127.0.0.1");
			assertThat(adminConnector.getBindHost()).isEqualTo("127.0.0.1");
			assertThat(extension.getLocalPort()).isPositive();
		} finally {
			extension.after();
		}
	}

	@Test
	void simpleServerFactoryBindsLoopbackByDefault() throws Exception {
		var extension = new LiftwizardAppExtension<>(
			TestApplication.class,
			ResourceHelpers.resourceFilePath("test-config-simple.yml")
		);
		extension.before();
		try {
			var serverFactory = (SimpleServerFactory) extension.getConfiguration().getServerFactory();
			var connector = (HttpConnectorFactory) serverFactory.getConnector();
			assertThat(connector.getBindHost()).isEqualTo("127.0.0.1");
		} finally {
			extension.after();
		}
	}

	@Test
	void explicitBindHostIsPreserved() throws Exception {
		var extension = new LiftwizardAppExtension<>(
			TestApplication.class,
			ResourceHelpers.resourceFilePath("test-config-bind-host.yml")
		);
		extension.before();
		try {
			var serverFactory = (DefaultServerFactory) extension.getConfiguration().getServerFactory();
			var applicationConnector = (HttpConnectorFactory) serverFactory.getApplicationConnectors().get(0);
			var adminConnector = (HttpConnectorFactory) serverFactory.getAdminConnectors().get(0);
			assertThat(applicationConnector.getBindHost()).isEqualTo("localhost");
			assertThat(adminConnector.getBindHost()).isEqualTo("127.0.0.1");
		} finally {
			extension.after();
		}
	}

	@Test
	void explicitCommandInstantiatorIsRespected() throws Exception {
		var extension = new LiftwizardAppExtension<>(
			TestApplication.class,
			ResourceHelpers.resourceFilePath("test-config.yml"),
			null,
			ServerCommand::new
		);
		extension.before();
		try {
			var serverFactory = (DefaultServerFactory) extension.getConfiguration().getServerFactory();
			var applicationConnector = (HttpConnectorFactory) serverFactory.getApplicationConnectors().get(0);
			assertThat(applicationConnector.getBindHost()).isNull();
		} finally {
			extension.after();
		}
	}

	public static class TestApplication extends Application<Configuration> {

		@Override
		public void run(Configuration configuration, Environment environment) {
			// Nothing to do
		}
	}
}
