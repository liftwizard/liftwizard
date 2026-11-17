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
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Supplies the signals that {@link JsonPrettyPrinter} cannot get from Jackson's pretty printer callbacks.
 *
 * <p>Jackson calls {@link JsonPrettyPrinter#writeObjectFieldValueSeparator} before it knows the value's type, so this wrapper announces each object first.
 *
 * <p>Jackson also gives no warning of an array's contents, so this wrapper records each outermost array into a {@link TokenBuffer}. An object inside the array ends the recording, and the array is written expanded. When the array closes without one, the wrapper waits for the next event, which tells whether a comma follows the array, then writes the array on one line if that line fits.
 */
public class JsonPrettyPrinterGenerator
	extends JsonGeneratorDelegate
{
	private static final JsonFactory INLINE_FACTORY = new JsonFactory();

	private final JsonGenerator target;

	// The array being recorded, which the delegate points at
	@Nullable
	private TokenBuffer recordedArray;

	private int recordedArrayDepth;
	// The column where the recorded array's '[' goes
	private int recordedArrayColumn;

	// A recorded array that closed, waiting for the next event. The delegate points at lookahead, which collects any values written before then.
	@Nullable
	private TokenBuffer pendingArray;

	private int pendingArrayColumn;

	@Nullable
	private TokenBuffer lookahead;

	public JsonPrettyPrinterGenerator(JsonGenerator delegate)
	{
		// false so that writeObject() and writeTree() serialize through this wrapper
		super(delegate, false);
		this.target = delegate;
	}

	/**
	 * Writes a root array that is still waiting for the next event. Call after the root value is serialized.
	 */
	public void finish()
		throws IOException
	{
		this.writePendingArray(false);
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

	@Override
	public void writeEndObject()
		throws IOException
	{
		this.writePendingArray(false);
		super.writeEndObject();
	}

	@Override
	public void writeStartArray()
		throws IOException
	{
		this.beforeStartArray();
		super.writeStartArray();
	}

	@Override
	public void writeStartArray(int size)
		throws IOException
	{
		this.beforeStartArray();
		super.writeStartArray(size);
	}

	@Override
	public void writeStartArray(Object forValue)
		throws IOException
	{
		this.beforeStartArray();
		super.writeStartArray(forValue);
	}

	@Override
	public void writeStartArray(Object forValue, int size)
		throws IOException
	{
		this.beforeStartArray();
		super.writeStartArray(forValue, size);
	}

	@Override
	public void writeEndArray()
		throws IOException
	{
		this.writePendingArray(false);
		super.writeEndArray();
		if (this.recordedArray != null)
		{
			this.recordedArrayDepth--;
			if (this.recordedArrayDepth == 0)
			{
				this.pendingArray = this.recordedArray;
				this.pendingArrayColumn = this.recordedArrayColumn;
				this.recordedArray = null;
				this.lookahead = new TokenBuffer(this.target.getCodec(), false);
				this.delegate = this.lookahead;
			}
		}
	}

	@Override
	public void writeFieldName(String name)
		throws IOException
	{
		this.writePendingArray(true);
		super.writeFieldName(name);
	}

	@Override
	public void writeFieldName(SerializableString name)
		throws IOException
	{
		this.writePendingArray(true);
		super.writeFieldName(name);
	}

	@Override
	public void writeFieldId(long id)
		throws IOException
	{
		this.writePendingArray(true);
		super.writeFieldId(id);
	}

	@Override
	public void close()
		throws IOException
	{
		this.finish();
		super.close();
	}

	private void beforeStartObject()
		throws IOException
	{
		this.writePendingArray(true);
		this.writeRecordedArrayExpanded();
		if (this.getPrettyPrinter() instanceof JsonPrettyPrinter jsonPrettyPrinter)
		{
			jsonPrettyPrinter.beforeStartObject();
		}
	}

	private void beforeStartArray()
		throws IOException
	{
		this.writePendingArray(true);
		if (this.recordedArray != null)
		{
			this.recordedArrayDepth++;
			return;
		}
		if (this.target.getPrettyPrinter() instanceof JsonPrettyPrinter jsonPrettyPrinter)
		{
			this.recordedArrayColumn = this.getColumn(jsonPrettyPrinter);
			this.recordedArray = new TokenBuffer(this.target.getCodec(), false);
			this.recordedArrayDepth = 1;
			this.delegate = this.recordedArray;
		}
	}

	private int getColumn(@Nonnull JsonPrettyPrinter jsonPrettyPrinter)
		throws IOException
	{
		int indentation = jsonPrettyPrinter.getNesting() * JsonPrettyPrinter.TAB_WIDTH;
		JsonStreamContext context = this.target.getOutputContext();
		if (!context.inObject())
		{
			return indentation;
		}
		// "name": [
		return indentation + quotedLength(context.getCurrentName()) + 2;
	}

	private void writeRecordedArrayExpanded()
		throws IOException
	{
		if (this.recordedArray == null)
		{
			return;
		}
		TokenBuffer array = this.recordedArray;
		this.recordedArray = null;
		this.delegate = this.target;
		array.serialize(this.target);
	}

	private void writePendingArray(boolean commaFollows)
		throws IOException
	{
		if (this.pendingArray == null)
		{
			return;
		}
		TokenBuffer array = this.pendingArray;
		TokenBuffer values = this.lookahead;
		this.clearPendingArray();
		this.writeArray(array, values, commaFollows);
	}

	private void clearPendingArray()
	{
		this.pendingArray = null;
		this.lookahead = null;
		this.delegate = this.target;
	}

	private void writeArray(@Nonnull TokenBuffer array, @Nullable TokenBuffer values, boolean commaFollows)
		throws IOException
	{
		boolean valuesFollow = values != null && values.firstToken() != null;
		int comma = commaFollows || valuesFollow ? 1 : 0;
		if (
			this.pendingArrayColumn + inlineLength(array) + comma <= JsonPrettyPrinter.PRINT_WIDTH &&
			this.target.getPrettyPrinter() instanceof JsonPrettyPrinter jsonPrettyPrinter
		)
		{
			jsonPrettyPrinter.beforeInlineArray();
		}
		array.serialize(this.target);
		if (valuesFollow)
		{
			values.serialize(this.target);
		}
	}

	private static int inlineLength(@Nonnull TokenBuffer array)
		throws IOException
	{
		var writer = new StringWriter();
		try (JsonGenerator generator = INLINE_FACTORY.createGenerator(writer))
		{
			generator.setPrettyPrinter(new InlineArrayPrettyPrinter());
			array.serialize(generator);
		}
		return writer.getBuffer().length();
	}

	private static int quotedLength(@Nullable String name)
		throws IOException
	{
		var writer = new StringWriter();
		try (JsonGenerator generator = INLINE_FACTORY.createGenerator(writer))
		{
			generator.writeString(name);
		}
		return writer.getBuffer().length();
	}

	private static final class InlineArrayPrettyPrinter
		extends MinimalPrettyPrinter
	{
		@Override
		public void writeArrayValueSeparator(JsonGenerator jsonGenerator)
			throws IOException
		{
			jsonGenerator.writeRaw(", ");
		}
	}
}
