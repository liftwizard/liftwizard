package com.liftwizard.serialization.jackson.pretty;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonLinesPrettyPrinter extends DefaultPrettyPrinter
{
    public JsonLinesPrettyPrinter()
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
    }
}
