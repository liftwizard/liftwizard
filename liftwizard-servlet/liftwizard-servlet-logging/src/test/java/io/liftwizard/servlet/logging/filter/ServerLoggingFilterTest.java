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

package io.liftwizard.servlet.logging.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ServerLoggingFilterTest
{
	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

	@Test
	void unflushedWriterOutputReachesClientAndLog()
		throws IOException, ServletException
	{
		String body = "x".repeat(100);
		Result result = runFilter(1000, (request, response) -> response.getWriter().print(body));

		assertThat(result.clientBody()).isEqualTo(body);
		assertThat(result.loggedBody()).isEqualTo(body);
	}

	@Test
	void bodyOfExactlyMaxEntitySizeIsNotTruncated()
		throws IOException, ServletException
	{
		Result result = runFilter(5, (request, response) ->
			response.getOutputStream().write("hello".getBytes(StandardCharsets.UTF_8))
		);

		assertThat(result.clientBody()).isEqualTo("hello");
		assertThat(result.loggedBody()).isEqualTo("hello");
	}

	@Test
	void bodyLongerThanMaxEntitySizeIsTruncated()
		throws IOException, ServletException
	{
		Result result = runFilter(5, (request, response) ->
			response.getOutputStream().write("hello!".getBytes(StandardCharsets.UTF_8))
		);

		assertThat(result.clientBody()).isEqualTo("hello!");
		assertThat(result.loggedBody()).isEqualTo("hello...more...");
	}

	private static Result runFilter(int maxEntitySize, FilterChain chain)
		throws IOException, ServletException
	{
		var loggingConfig = new LoggingConfig(
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false,
			Lists.immutable.empty(),
			Lists.immutable.empty(),
			maxEntitySize
		);
		var logged = new AtomicReference<StructuredArguments>();
		var filter = new ServerLoggingFilter(loggingConfig, logged::set, CLOCK);

		var request = new MockHttpServletRequest("GET", "/test");
		var response = new MockHttpServletResponse();
		response.setCharacterEncoding("UTF-8");

		filter.doFilter(request, response, chain);

		String loggedBody = logged.get().getResponse().getHttp().getBody();
		return new Result(response.getContentAsString(), loggedBody);
	}

	private record Result(String clientBody, String loggedBody)
	{
	}
}
