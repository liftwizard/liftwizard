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

package io.liftwizard.configuration.metrics.reporter.dogstatsd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.liftwizard.logging.metrics.dogstatsd.DogStatsDReporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DogStatsDReporterFactoryTest {

	@Test
	void factoryIsRegisteredAsReporterServiceProvider() throws Exception {
		var url = Thread.currentThread()
			.getContextClassLoader()
			.getResource("META-INF/services/io.dropwizard.metrics.ReporterFactory");
		assertThat(url).isNotNull();
		try (var reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			String contents = reader.lines().collect(Collectors.joining("\n"));
			assertThat(contents).contains(DogStatsDReporterFactory.class.getName());
		}
	}

	@Test
	void factoryBuildsDogStatsDReporter() {
		DogStatsDReporterFactory factory = new DogStatsDReporterFactory();
		factory.setHost("localhost");
		factory.setPort(8125);
		factory.setPrefix("test");
		factory.setConstantTags(List.of("env:test"));

		ScheduledReporter reporter = factory.build(new MetricRegistry());

		assertThat(reporter).isInstanceOf(DogStatsDReporter.class);
	}

	@Test
	void defaultsArePopulated() {
		DogStatsDReporterFactory factory = new DogStatsDReporterFactory();
		assertThat(factory.getHost()).isEqualTo("localhost");
		assertThat(factory.getPort()).isEqualTo(8125);
		assertThat(factory.getPrefix()).isEmpty();
		assertThat(factory.getConstantTags()).isEmpty();
	}
}
