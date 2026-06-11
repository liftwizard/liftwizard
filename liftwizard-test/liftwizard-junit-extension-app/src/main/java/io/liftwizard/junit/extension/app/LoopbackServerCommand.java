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
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * A {@link ServerCommand} that binds connectors without an explicit {@code bindHost} to {@code 127.0.0.1}
 * instead of the wildcard address.
 *
 * <p>On macOS, a wildcard bind on an ephemeral port ({@code port: 0}) can coexist with a local daemon
 * (such as Tailscale's LocalAPI) that already holds the same port with a specific {@code 127.0.0.1} bind.
 * Client connections to {@code 127.0.0.1:port} route to the more-specific socket, so test requests
 * silently reach the wrong server. Binding to loopback makes the kernel see the conflict and pick a
 * different ephemeral port.
 */
public class LoopbackServerCommand<C extends Configuration> extends ServerCommand<C> {

	public LoopbackServerCommand(Application<C> application) {
		super(application);
	}

	@Override
	protected void run(Environment environment, Namespace namespace, C configuration) throws Exception {
		ServerFactory serverFactory = configuration.getServerFactory();
		if (serverFactory instanceof DefaultServerFactory defaultServerFactory) {
			defaultServerFactory.getApplicationConnectors().forEach(LoopbackServerCommand::bindToLoopback);
			defaultServerFactory.getAdminConnectors().forEach(LoopbackServerCommand::bindToLoopback);
		} else if (serverFactory instanceof SimpleServerFactory simpleServerFactory) {
			bindToLoopback(simpleServerFactory.getConnector());
		}
		super.run(environment, namespace, configuration);
	}

	private static void bindToLoopback(ConnectorFactory connectorFactory) {
		if (
			connectorFactory instanceof HttpConnectorFactory httpConnectorFactory
			&& httpConnectorFactory.getBindHost() == null
		) {
			httpConnectorFactory.setBindHost("127.0.0.1");
		}
	}
}
