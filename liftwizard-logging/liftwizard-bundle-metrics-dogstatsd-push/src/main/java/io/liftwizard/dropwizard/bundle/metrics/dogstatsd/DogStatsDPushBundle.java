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

package io.liftwizard.dropwizard.bundle.metrics.dogstatsd;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.logging.metrics.dogstatsd.DogStatsDReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dropwizard bundle that replaces the application's {@link com.codahale.metrics.MetricRegistry}
 * with a {@link PushingMetricRegistry}. Calls to {@code registry.counter(name)} and friends return
 * variants that push every update to a DogStatsD agent.
 *
 * <p>Pull-style {@code Gauge<T>} suppliers cannot be intercepted, so when
 * {@link DogStatsDPushFactory#isPollGauges()} is true the bundle also starts a
 * {@link DogStatsDReporter} filtered to {@link Gauge} metrics only.
 *
 * <p>Because the registry must be installed at {@code initialize()} time — before configuration
 * has been parsed — this bundle must be added to the {@code Bootstrap} BEFORE any other bundle
 * that creates metrics. {@link #initialize(Bootstrap)} enforces this by throwing
 * {@link IllegalStateException} if pre-existing metrics are found on the bootstrap registry.
 * Metrics created after a later bundle's {@code initialize} but before this bundle's
 * {@code run} use a {@link NoOpStatsDClient} placeholder until {@code run} swaps in the real
 * client.
 */
public class DogStatsDPushBundle implements ConfiguredBundle<DogStatsDPushFactoryProvider> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DogStatsDPushBundle.class);

	private PushingMetricRegistry registry;

	public PushingMetricRegistry getRegistry() {
		return this.registry;
	}

	@Override
	public void initialize(@Nonnull Bootstrap<?> bootstrap) {
		MetricRegistry existing = bootstrap.getMetricRegistry();
		Map<String, Metric> existingMetrics = existing.getMetrics();
		if (!existingMetrics.isEmpty()) {
			throw new IllegalStateException(
				"DogStatsDPushBundle must be added to the Bootstrap before any other bundle "
				+ "that creates metrics. Found "
				+ existingMetrics.size()
				+ " pre-existing metric(s): "
				+ existingMetrics.keySet()
			);
		}
		this.registry = new PushingMetricRegistry(new NoOpStatsDClient());
		bootstrap.setMetricRegistry(this.registry);
	}

	@Override
	public void run(@Nonnull DogStatsDPushFactoryProvider configuration, @Nonnull Environment environment) {
		DogStatsDPushFactory factory = Objects.requireNonNull(configuration.getDogStatsDPushFactory());
		if (!factory.isEnabled()) {
			LOGGER.info("{} disabled.", this.getClass().getSimpleName());
			return;
		}

		StatsDClient client = new NonBlockingStatsDClientBuilder()
			.prefix(factory.getPrefix())
			.hostname(factory.getHost())
			.port(factory.getPort())
			.constantTags(factory.getConstantTags().toArray(new String[0]))
			.build();
		this.registry.setStatsDClient(client);
		environment.lifecycle().manage(new ClientLifecycle(client));

		if (factory.isPollGauges()) {
			DogStatsDReporter reporter = DogStatsDReporter.forRegistry(this.registry)
				.filter((name, metric) -> metric instanceof Gauge && !(metric instanceof PushingSettableGauge))
				.build(client);
			reporter.start(factory.getPollFrequencySeconds(), TimeUnit.SECONDS);
			environment.lifecycle().manage(new ReporterLifecycle(reporter));
		}

		LOGGER.info(
			"{} pushing metrics to {}:{}",
			this.getClass().getSimpleName(),
			factory.getHost(),
			factory.getPort()
		);
	}

	private static final class ClientLifecycle implements Managed {

		private final StatsDClient client;

		ClientLifecycle(StatsDClient client) {
			this.client = client;
		}

		@Override
		public void stop() {
			this.client.stop();
		}
	}

	private static final class ReporterLifecycle implements Managed {

		private final DogStatsDReporter reporter;

		ReporterLifecycle(DogStatsDReporter reporter) {
			this.reporter = reporter;
		}

		@Override
		public void stop() {
			this.reporter.stop();
		}
	}
}
