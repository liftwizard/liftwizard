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

package io.liftwizard.dropwizard.bundle.config.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ConfigLoggingBundleTest {

	/**
	 * The minimized-configuration log is a best-effort diagnostic. A failure while computing it (for example a Jackson
	 * serializer that throws under {@code @JsonInclude(NON_DEFAULT)}) must be contained so that it cannot prevent the
	 * application from starting. This test forces the minimization to fail and asserts that {@code logConfiguration}
	 * still completes normally.
	 */
	@Test
	void minimizationFailureDoesNotPreventStartup() {
		ObjectMapper objectMapper = new FailingCopyObjectMapper(Jackson.newObjectMapper());
		var configuration = new Configuration();

		assertThatCode(() ->
			ConfigLoggingBundle.logConfiguration(configuration, objectMapper)
		).doesNotThrowAnyException();
	}

	@Test
	void logsConfigurationWithBlackbirdRegistered() {
		ObjectMapper objectMapper = Jackson.newObjectMapper().registerModule(new BlackbirdModule());
		var configuration = new Configuration();

		assertThatCode(() ->
			ConfigLoggingBundle.logConfiguration(configuration, objectMapper)
		).doesNotThrowAnyException();
	}

	@Test
	void fullConfigurationFailureDoesNotPreventStartup() {
		ObjectMapper objectMapper = new FailingWriteObjectMapper(Jackson.newObjectMapper());
		var configuration = new Configuration();

		assertThatCode(() ->
			ConfigLoggingBundle.logConfiguration(configuration, objectMapper)
		).doesNotThrowAnyException();
	}

	/**
	 * An {@link ObjectMapper} that serializes normally but fails when copied. The minimized-configuration logic copies
	 * the mapper before installing its mix-in, so this triggers a failure only on the minimization path, leaving the
	 * full-configuration serialization (which uses the mapper directly) intact.
	 */
	private static final class FailingCopyObjectMapper extends ObjectMapper {

		private FailingCopyObjectMapper(ObjectMapper source) {
			super(source);
		}

		@Override
		public ObjectMapper copy() {
			throw new IllegalStateException("Forced minimization failure for test.");
		}
	}

	/**
	 * An {@link ObjectMapper} that fails when serializing to a String, exercising the guard around the full-configuration
	 * log line.
	 */
	private static final class FailingWriteObjectMapper extends ObjectMapper {

		private FailingWriteObjectMapper(ObjectMapper source) {
			super(source);
		}

		@Override
		public String writeValueAsString(Object value) {
			throw new IllegalStateException("Forced full-configuration serialization failure for test.");
		}
	}
}
