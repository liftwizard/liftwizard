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

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;

/**
 * Tab-indented pretty printer that matches oxfmt with {@code "braceStyle": "next-line"}.
 *
 * <p>A non-empty object that is a field value starts on the next line, at the key's indentation. Empty objects, arrays, objects inside arrays, and the root object keep their opening bracket on the same line.
 *
 * <p>Jackson writes the field/value separator before it knows the value's type, so this printer needs {@link JsonPrettyPrinterGenerator} to announce objects. Install {@link JsonPrettyPrinterSerializerProvider} on the {@code ObjectMapper} to get it. Without it, every field value stays on the key's line.
 */
public class JsonPrettyPrinter
	extends DefaultPrettyPrinter
{
	private static final DefaultIndenter TAB_INDENTER = new DefaultIndenter("\t", DefaultIndenter.SYS_LF);

	// Set by JsonPrettyPrinterGenerator just before the generator asks for the separator
	private boolean nextValueIsObject;
	// The separator was written without its trailing space, and the '{' is deferred until we know whether the object is empty
	private boolean objectOpenPending;

	public JsonPrettyPrinter()
	{
		this._arrayIndenter = TAB_INDENTER;
		this._objectIndenter = TAB_INDENTER;
		this._separators = Separators.createDefaultInstance().withObjectEmptySeparator("").withArrayEmptySeparator("");
		this._objectEmptySeparator = "";
		this._arrayEmptySeparator = "";
	}

	@Nonnull
	@Override
	public DefaultPrettyPrinter createInstance()
	{
		return new JsonPrettyPrinter();
	}

	void beforeStartObject()
	{
		this.nextValueIsObject = true;
	}

	@Override
	public void writeObjectFieldValueSeparator(@Nonnull JsonGenerator jsonGenerator)
		throws IOException
	{
		jsonGenerator.writeRaw(this._separators.getObjectFieldValueSeparator());
		if (this.nextValueIsObject)
		{
			this.objectOpenPending = true;
		}
		else
		{
			jsonGenerator.writeRaw(' ');
		}
	}

	@Override
	public void writeStartObject(@Nonnull JsonGenerator jsonGenerator)
		throws IOException
	{
		this.nextValueIsObject = false;
		if (this.objectOpenPending)
		{
			this._nesting++;
		}
		else
		{
			super.writeStartObject(jsonGenerator);
		}
	}

	@Override
	public void beforeObjectEntries(@Nonnull JsonGenerator jsonGenerator)
		throws IOException
	{
		if (this.objectOpenPending)
		{
			this.objectOpenPending = false;
			this._objectIndenter.writeIndentation(jsonGenerator, this._nesting - 1);
			jsonGenerator.writeRaw('{');
		}
		super.beforeObjectEntries(jsonGenerator);
	}

	@Override
	public void writeEndObject(@Nonnull JsonGenerator jsonGenerator, int nrOfEntries)
		throws IOException
	{
		if (this.objectOpenPending)
		{
			this.objectOpenPending = false;
			jsonGenerator.writeRaw(" {");
		}
		super.writeEndObject(jsonGenerator, nrOfEntries);
		if (this._nesting == 0)
		{
			jsonGenerator.writeRaw(DefaultIndenter.SYS_LF);
		}
	}

	@Override
	public void writeEndArray(@Nonnull JsonGenerator jsonGenerator, int nrOfValues)
		throws IOException
	{
		super.writeEndArray(jsonGenerator, nrOfValues);
		if (this._nesting == 0)
		{
			jsonGenerator.writeRaw(DefaultIndenter.SYS_LF);
		}
	}
}
