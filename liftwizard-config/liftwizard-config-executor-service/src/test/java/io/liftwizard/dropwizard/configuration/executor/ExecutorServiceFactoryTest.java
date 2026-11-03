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

package io.liftwizard.dropwizard.configuration.executor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ExecutorServiceFactoryTest
{
	@RegisterExtension
	private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();

	private <T> T getConfiguredType(Class<T> klass, String path)
		throws IOException, ConfigurationException
	{
		var factory = new JsonConfigurationFactory<T>(
			klass,
			Validators.newValidator(),
			ExecutorServiceFactoryTest.newObjectMapper(),
			"dw"
		);
		return factory.build(new ResourceConfigurationSourceProvider(), path);
	}

	@Test
	void executorServiceFactory()
		throws Exception
	{
		ExecutorServiceFactory executorServiceFactory = this.getConfiguredType(
			ExecutorServiceFactory.class,
			"default-executor-service-config-test.json5"
		);
		assertThat(executorServiceFactory).isInstanceOf(ExecutorServiceFactory.class);
	}

	@Test
	void defaultScheduledExecutorServiceFactory()
		throws Exception
	{
		ScheduledExecutorServiceFactory scheduledExecutorServiceFactory = this.getConfiguredType(
			ScheduledExecutorServiceFactory.class,
			"default-executor-service-config-test.json5"
		);
		assertThat(scheduledExecutorServiceFactory).isInstanceOf(DefaultScheduledExecutorServiceFactory.class);
	}

	@Test
	void noopScheduledExecutorServiceFactory()
		throws Exception
	{
		ScheduledExecutorServiceFactory scheduledExecutorServiceFactory = this.getConfiguredType(
			ScheduledExecutorServiceFactory.class,
			"noop-executor-service-config-test.json5"
		);
		assertThat(scheduledExecutorServiceFactory).isInstanceOf(NoopScheduledExecutorServiceFactory.class);
	}

	@Test
	void invalidExecutorServiceFactory()
		throws Exception
	{
		try
		{
			this.getConfiguredType(ExecutorServiceFactory.class, "invalid-executor-service-config-test.json5");
			fail("");
		}
		catch (ConfigurationValidationException e)
		{
			String message = e.getMessage();
			assertThat(message).contains("maxThreads < minThreads");
		}
	}

	@Test
	void poolGrowsToMaxThreads()
		throws Exception
	{
		ExecutorServiceFactory executorServiceFactory = new ExecutorServiceFactory();
		executorServiceFactory.setNameFormat("grow-test-%d");
		executorServiceFactory.setMinThreads(0);
		executorServiceFactory.setMaxThreads(4);

		MetricRegistry metricRegistry = new MetricRegistry();
		ExecutorService executorService = executorServiceFactory.build(
			new LifecycleEnvironment(metricRegistry),
			metricRegistry
		);
		try
		{
			CountDownLatch allRunning = new CountDownLatch(4);
			CountDownLatch release = new CountDownLatch(1);
			for (int i = 0; i < 4; i++)
			{
				executorService.execute(() ->
				{
					allRunning.countDown();
					try
					{
						release.await();
					}
					catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
					}
				});
			}

			boolean allTasksRanConcurrently = allRunning.await(5, TimeUnit.SECONDS);
			release.countDown();
			assertThat(allTasksRanConcurrently).isTrue();
		}
		finally
		{
			executorService.shutdownNow();
		}
	}

	private static ObjectMapper newObjectMapper()
	{
		ObjectMapper objectMapper = Jackson.newObjectMapper();
		ObjectMapperConfig.configure(objectMapper);
		return objectMapper;
	}
}
