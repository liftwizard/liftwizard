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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;

/**
 * Tells a {@link JsonPrettyPrinter} that the next value is an object, before the delegate asks the printer for the field/value separator.
 *
 * <p>Jackson calls {@link JsonPrettyPrinter#writeObjectFieldValueSeparator} before it knows the value's type, and writes scalars with no further pretty-printer callback. This wrapper supplies the missing signal.
 */
public class JsonPrettyPrinterGenerator
	extends JsonGeneratorDelegate
{
	public JsonPrettyPrinterGenerator(JsonGenerator delegate)
	{
		// false so that writeObject() and writeTree() serialize through this wrapper
		super(delegate, false);
	}

	@Override
	public void writeStartObject()
		throws IOException
	{
		this.beforeStartObject();
		super.writeStartObject();
	}

	@Override
	public void writeStartObject(Object forValue)
		throws IOException
	{
		this.beforeStartObject();
		super.writeStartObject(forValue);
	}

	@Override
	public void writeStartObject(Object forValue, int size)
		throws IOException
	{
		this.beforeStartObject();
		super.writeStartObject(forValue, size);
	}

	private void beforeStartObject()
	{
		if (this.getPrettyPrinter() instanceof JsonPrettyPrinter jsonPrettyPrinter)
		{
			jsonPrettyPrinter.beforeStartObject();
		}
	}
}
