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

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for {@link DogStatsDPushBundle}.
 *
 * <pre>{@code
 * dogStatsDPush:
 *   enabled: true
 *   host: localhost
 *   port: 8125
 *   prefix: myapp
 *   constantTags:
 *     - env:prod
 *   pollGauges: true
 *   pollFrequencySeconds: 60
 * }</pre>
 */
public class DogStatsDPushFactory {

	private boolean enabled = true;

	@NotEmpty
	private String host = "localhost";

	@Min(1)
	private int port = 8125;

	@NotNull
	private String prefix = "";

	@NotNull
	private List<String> constantTags = List.of();

	private boolean pollGauges = true;

	@Min(1)
	private long pollFrequencySeconds = 60;

	@JsonProperty
	public boolean isEnabled() {
		return this.enabled;
	}

	@JsonProperty
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

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

	@JsonProperty
	public boolean isPollGauges() {
		return this.pollGauges;
	}

	@JsonProperty
	public void setPollGauges(boolean pollGauges) {
		this.pollGauges = pollGauges;
	}

	@JsonProperty
	public long getPollFrequencySeconds() {
		return this.pollFrequencySeconds;
	}

	@JsonProperty
	public void setPollFrequencySeconds(long pollFrequencySeconds) {
		this.pollFrequencySeconds = pollFrequencySeconds;
	}
}
