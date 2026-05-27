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

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.metrics.ReporterFactory;
import io.liftwizard.logging.metrics.dogstatsd.DogStatsDReporter;

/**
 * A {@link ReporterFactory} for {@link DogStatsDReporter} instances. Configured via the
 * type discriminator {@code "dogstatsd"} in YAML/JSON.
 *
 * <pre>{@code
 * metrics:
 *   reporters:
 *     - type: dogstatsd
 *       host: localhost
 *       port: 8125
 *       prefix: myapp
 *       constantTags:
 *         - env:prod
 *         - service:api
 * }</pre>
 */
@JsonTypeName("dogstatsd")
@AutoService(ReporterFactory.class)
public class DogStatsDReporterFactory extends BaseReporterFactory {

	@NotEmpty
	private String host = "localhost";

	@Min(1)
	private int port = 8125;

	@NotNull
	private String prefix = "";

	@NotNull
	private List<String> constantTags = List.of();

	@JsonProperty
	public String getHost() {
		return this.host;
	}

	@JsonProperty
	public void setHost(String host) {
		this.host = host;
	}

	@JsonProperty
	public int getPort() {
		return this.port;
	}

	@JsonProperty
	public void setPort(int port) {
		this.port = port;
	}

	@JsonProperty
	public String getPrefix() {
		return this.prefix;
	}

	@JsonProperty
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@JsonProperty
	public List<String> getConstantTags() {
		return this.constantTags;
	}

	@JsonProperty
	public void setConstantTags(List<String> constantTags) {
		this.constantTags = constantTags;
	}

	@Override
	public ScheduledReporter build(MetricRegistry registry) {
		StatsDClient statsd = new NonBlockingStatsDClientBuilder()
			.prefix(this.prefix)
			.hostname(this.host)
			.port(this.port)
			.constantTags(this.constantTags.toArray(new String[0]))
			.build();
		return DogStatsDReporter.forRegistry(registry)
			.filter(this.getFilter())
			.convertRatesTo(this.getRateUnit())
			.convertDurationsTo(this.getDurationUnit())
			.closeStatsdOnStop(true)
			.build(statsd);
	}
}
