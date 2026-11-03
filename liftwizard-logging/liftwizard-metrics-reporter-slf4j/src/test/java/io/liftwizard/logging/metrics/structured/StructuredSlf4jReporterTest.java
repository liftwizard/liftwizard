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

package io.liftwizard.logging.metrics.structured;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredSlf4jReporterTest
{
	@Test
	void reportsWithoutMarkerWhenNoMarkerIsConfigured()
	{
		LoggerContext loggerContext = new LoggerContext();
		Logger logger = loggerContext.getLogger("metrics");
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.setContext(loggerContext);
		listAppender.start();
		logger.addAppender(listAppender);

		MetricRegistry registry = new MetricRegistry();
		registry.counter("requests").inc(3);

		Map<String, Object> expectedArgument = new LinkedHashMap<>();
		expectedArgument.put("metric_type", "COUNTER");
		expectedArgument.put("metric_name", "requests");
		expectedArgument.put("count", 3L);

		try (StructuredSlf4jReporter reporter = StructuredSlf4jReporter.forRegistry(registry).outputTo(logger).build())
		{
			reporter.report();

			assertThat(listAppender.list).hasSize(1);
			ILoggingEvent event = listAppender.list.get(0);
			assertThat(event.getMarker()).isNull();
			assertThat(event.getMessage()).isEqualTo("metrics");
			assertThat(event.getArgumentArray()).containsExactly(expectedArgument);
		}
	}
}
