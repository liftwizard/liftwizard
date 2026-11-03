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

package io.liftwizard.serialization.jackson.pretty;

import java.io.IOException;
import java.io.Serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.CacheProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * Wraps every root generator in a {@link JsonPrettyPrinterGenerator}, so {@link JsonPrettyPrinter} works through {@code ObjectMapper.writeValueAsString()} and {@code ObjectMapper.writerWithDefaultPrettyPrinter()}.
 *
 * <p>Install with {@code objectMapper.setSerializerProvider(new JsonPrettyPrinterSerializerProvider())}. Unlike a {@code JsonGeneratorDecorator}, this works on an existing {@code ObjectMapper} whose {@code JsonFactory} is already fixed.
 */
public class JsonPrettyPrinterSerializerProvider
	extends DefaultSerializerProvider
{
	@Serial
	private static final long serialVersionUID = 1L;

	public JsonPrettyPrinterSerializerProvider()
	{
	}

	protected JsonPrettyPrinterSerializerProvider(JsonPrettyPrinterSerializerProvider src)
	{
		super(src);
	}

	protected JsonPrettyPrinterSerializerProvider(
		SerializerProvider src,
		SerializationConfig config,
		SerializerFactory serializerFactory
	)
	{
		super(src, config, serializerFactory);
	}

	protected JsonPrettyPrinterSerializerProvider(JsonPrettyPrinterSerializerProvider src, CacheProvider cacheProvider)
	{
		super(src, cacheProvider);
	}

	@Override
	public DefaultSerializerProvider copy()
	{
		return new JsonPrettyPrinterSerializerProvider(this);
	}

	@Override
	public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory serializerFactory)
	{
		return new JsonPrettyPrinterSerializerProvider(this, config, serializerFactory);
	}

	@Override
	public DefaultSerializerProvider withCaches(CacheProvider cacheProvider)
	{
		return new JsonPrettyPrinterSerializerProvider(this, cacheProvider);
	}

	@Override
	public void serializeValue(JsonGenerator gen, Object value)
		throws IOException
	{
		super.serializeValue(wrap(gen), value);
	}

	@Override
	public void serializeValue(JsonGenerator gen, Object value, JavaType rootType)
		throws IOException
	{
		super.serializeValue(wrap(gen), value, rootType);
	}

	@Override
	public void serializeValue(JsonGenerator gen, Object value, JavaType rootType, JsonSerializer<Object> ser)
		throws IOException
	{
		super.serializeValue(wrap(gen), value, rootType, ser);
	}

	@Override
	public void serializePolymorphic(
		JsonGenerator gen,
		Object value,
		JavaType rootType,
		JsonSerializer<Object> valueSer,
		TypeSerializer typeSer
	)
		throws IOException
	{
		super.serializePolymorphic(wrap(gen), value, rootType, valueSer, typeSer);
	}

	private static JsonGenerator wrap(JsonGenerator gen)
	{
		return gen instanceof JsonPrettyPrinterGenerator ? gen : new JsonPrettyPrinterGenerator(gen);
	}
}
