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

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.server.DefaultServerFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Test
	void ignoredPropertyNamesAnnotationIntrospectorRemovesRequestLog() {
		ObjectMapper objectMapper = Jackson.newObjectMapper();
		ObjectNode withRequestLog = objectMapper.valueToTree(new DefaultServerFactory());
		assertThat(withRequestLog.has("requestLog")).isTrue();

		ObjectMapper ignoringObjectMapper = Jackson.newObjectMapper();
		AnnotationIntrospector existingIntrospector = ignoringObjectMapper
			.getSerializationConfig()
			.getAnnotationIntrospector();
		ignoringObjectMapper.setAnnotationIntrospector(
			AnnotationIntrospectorPair.pair(
				new IgnoredPropertyNamesAnnotationIntrospector(AbstractServerFactory.class, "requestLog"),
				existingIntrospector
			)
		);
		ObjectNode withoutRequestLog = ignoringObjectMapper.valueToTree(new DefaultServerFactory());
		assertThat(withoutRequestLog.has("requestLog")).isFalse();
	}

	@Test
	void ignoredPropertyNamesAnnotationIntrospectorIsScopedToDeclaringType() {
		ObjectMapper objectMapper = Jackson.newObjectMapper();
		AnnotationIntrospector existingIntrospector = objectMapper.getSerializationConfig().getAnnotationIntrospector();
		objectMapper.setAnnotationIntrospector(
			AnnotationIntrospectorPair.pair(
				new IgnoredPropertyNamesAnnotationIntrospector(AbstractServerFactory.class, "requestLog"),
				existingIntrospector
			)
		);

		ObjectNode node = objectMapper.valueToTree(new UnrelatedRequestLogHolder());
		assertThat(node.has("requestLog")).isTrue();
	}

	@Test
	void fallbackMinimizationKeepsServerButOmitsRequestLog() {
		Configuration configuration = new Configuration();
		DefaultServerFactory serverFactory = (DefaultServerFactory) configuration.getServerFactory();
		serverFactory.setMaxThreads(999);
		LogbackAccessRequestLogFactory requestLogFactory = new LogbackAccessRequestLogFactory();
		requestLogFactory.setAppenders(List.of());
		serverFactory.setRequestLogFactory(requestLogFactory);

		ObjectMapper objectMapper = Jackson.newObjectMapper();
		Optional<ObjectNode> fallback = ConfigLoggingBundle.minimizeConfigurationIgnoringRequestLog(
			configuration,
			objectMapper
		);

		assertThat(fallback).isPresent();
		assertThat(fallback.get().path("server").has("maxThreads")).isTrue();
		assertThat(fallback.get().path("server").has("requestLog")).isFalse();
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

	/**
	 * An unrelated type that also has a {@code requestLog} property, used to confirm the introspector only ignores the
	 * property on the configured declaring type.
	 */
	public static final class UnrelatedRequestLogHolder {

		public String getRequestLog() {
			return "kept";
		}
	}
}
