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
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response.Status;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerLoggingResponseFilterTest<T>
{
	@SuppressWarnings("unused")
	private List<String>[] genericArrayField;

	@Test
	void canonicalClassUsesCanonicalName()
		throws IOException
	{
		assertThat(filterEntityType(Map.Entry.class)).isEqualTo("java.util.Map.Entry");
	}

	@Test
	void anonymousClassFallsBackToBinaryName()
		throws IOException
	{
		Object entity = new Object()
		{
		};

		assertThat(filterEntityType(entity.getClass())).isEqualTo(
			"io.liftwizard.servlet.logging.filter.ServerLoggingResponseFilterTest$1"
		);
	}

	@Test
	void localRecordFallsBackToBinaryName()
		throws IOException
	{
		record LocalRecord(String value)
		{
		}

		assertThat(filterEntityType(LocalRecord.class)).isEqualTo(
			"io.liftwizard.servlet.logging.filter.ServerLoggingResponseFilterTest$1LocalRecord"
		);
	}

	@Test
	void genericArrayTypeUsesTypeName()
		throws IOException, NoSuchFieldException
	{
		Type genericArrayType = ServerLoggingResponseFilterTest.class
			.getDeclaredField("genericArrayField")
			.getGenericType();

		assertThat(filterEntityType(genericArrayType)).isEqualTo("java.util.List<java.lang.String>[]");
	}

	@Test
	void typeVariableUsesTypeName()
		throws IOException
	{
		Type typeVariable = ServerLoggingResponseFilterTest.class.getTypeParameters()[0];

		assertThat(filterEntityType(typeVariable)).isEqualTo("T");
	}

	private static String filterEntityType(Type entityType)
		throws IOException
	{
		var structuredArguments = new StructuredArguments();

		var requestContext = (ContainerRequestContext) Proxy.newProxyInstance(
			ServerLoggingResponseFilterTest.class.getClassLoader(),
			new Class<?>[]
			{
				ContainerRequestContext.class,
			},
			(proxy, method, args) ->
			{
				if (method.getName().equals("getProperty") && "structuredArguments".equals(args[0]))
				{
					return structuredArguments;
				}
				throw new UnsupportedOperationException(method.getName());
			}
		);

		var responseContext = (ContainerResponseContext) Proxy.newProxyInstance(
			ServerLoggingResponseFilterTest.class.getClassLoader(),
			new Class<?>[]
			{
				ContainerResponseContext.class,
			},
			(proxy, method, args) ->
			{
				return switch (method.getName())
				{
					case "getStatusInfo" -> Status.OK;
					case "getEntityType" -> entityType;
					default -> throw new UnsupportedOperationException(method.getName());
				};
			}
		);

		new ServerLoggingResponseFilter().filter(requestContext, responseContext);

		return structuredArguments.getResponse().getHttp().getEntityType();
	}
}
