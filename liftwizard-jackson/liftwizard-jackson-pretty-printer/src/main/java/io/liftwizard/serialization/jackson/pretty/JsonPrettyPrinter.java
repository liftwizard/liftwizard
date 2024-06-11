/*
 * Copyright 2024 Craig Motlin
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

public class JsonPrettyPrinter extends DefaultPrettyPrinter
{
    public JsonPrettyPrinter()
    {
        this._arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }

    @Nonnull
    @Override
    public DefaultPrettyPrinter createInstance()
    {
        return this;
    }

    @Override
    public void writeObjectFieldValueSeparator(@Nonnull JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw(this._separators.getObjectFieldValueSeparator() + " ");
    }

    @Override
    public void writeStartObject(@Nonnull JsonGenerator jsonGenerator) throws IOException
    {
        super.writeStartObject(jsonGenerator);
    }

    @Override
    public void writeEndObject(@Nonnull JsonGenerator jsonGenerator, int nrOfEntries) throws IOException
    {
        super.writeEndObject(jsonGenerator, nrOfEntries);
        if (this._nesting == 0)
        {
            jsonGenerator.writeRaw(DefaultIndenter.SYS_LF);
        }
    }
}
