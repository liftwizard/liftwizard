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

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsPath;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.uri.UriTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerLoggingRequestFilterTest
{
	@Test
	void reencodedAbsolutePathDoesNotThrow()
		throws Exception
	{
		var servletRequestUrl = "http://localhost/api/items/{id}";
		URI jerseyRequestUri = URI.create("http://localhost/api/items/%7Bid%7D");

		StructuredArgumentsPath path = filterPath(servletRequestUrl, jerseyRequestUri, "/items", "/{id}");

		assertThat(path.getAbsolute()).isEqualTo(servletRequestUrl);
		assertThat(path.getTemplate()).isEqualTo("/items/{id}");
	}

	@Test
	void reencodedPipeInAbsolutePathDoesNotThrow()
		throws Exception
	{
		var servletRequestUrl = "http://localhost/api/a|b";
		URI jerseyRequestUri = URI.create("http://localhost/api/a%7Cb");

		StructuredArgumentsPath path = filterPath(servletRequestUrl, jerseyRequestUri, "/api", "/{name}");

		assertThat(path.getAbsolute()).isEqualTo(servletRequestUrl);
		assertThat(path.getTemplate()).isEqualTo("/api/{name}");
	}

	private static StructuredArgumentsPath filterPath(
		String servletRequestUrl,
		URI jerseyRequestUri,
		String resourceTemplate,
		String methodTemplate
	)
		throws Exception
	{
		var structuredArguments = new StructuredArguments();
		var path = new StructuredArgumentsPath(servletRequestUrl, jerseyRequestUri.getRawPath());
		structuredArguments.getRequest().getHttp().setPath(path);

		var securityContext = (SecurityContext) Proxy.newProxyInstance(
			ServerLoggingRequestFilterTest.class.getClassLoader(),
			new Class<?>[]
			{
				SecurityContext.class,
			},
			(proxy, method, args) -> null
		);

		var requestContext = new ContainerRequest(
			URI.create("http://localhost/api/"),
			jerseyRequestUri,
			"GET",
			securityContext,
			new MapPropertiesDelegate(),
			null
		);
		requestContext.setProperty("structuredArguments", structuredArguments);
		var routingContext = (UriRoutingContext) requestContext.getUriInfo();
		Matcher matcher = Pattern.compile("(.*)").matcher(jerseyRequestUri.getRawPath());
		assertThat(matcher.matches()).isTrue();
		routingContext.pushMatchResult(matcher);
		routingContext.pushMatchResult(matcher);
		routingContext.pushTemplates(new UriTemplate(resourceTemplate), new UriTemplate(methodTemplate));

		var resourceInfo = (ResourceInfo) Proxy.newProxyInstance(
			ServerLoggingRequestFilterTest.class.getClassLoader(),
			new Class<?>[]
			{
				ResourceInfo.class,
			},
			(proxy, method, args) -> null
		);

		var filter = new ServerLoggingRequestFilter((principal) -> Map.of());
		Field resourceInfoField = ServerLoggingRequestFilter.class.getDeclaredField("resourceInfo");
		resourceInfoField.setAccessible(true);
		resourceInfoField.set(filter, resourceInfo);

		filter.filter(requestContext);

		return structuredArguments.getRequest().getHttp().getPath();
	}
}
