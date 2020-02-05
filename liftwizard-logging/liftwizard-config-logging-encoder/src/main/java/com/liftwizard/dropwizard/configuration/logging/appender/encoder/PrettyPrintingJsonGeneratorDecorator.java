package com.liftwizard.dropwizard.configuration.logging.appender.encoder;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.liftwizard.serialization.jackson.pretty.JsonLinesPrettyPrinter;
import net.logstash.logback.decorate.JsonGeneratorDecorator;

public class PrettyPrintingJsonGeneratorDecorator implements JsonGeneratorDecorator
{
    @Override
    public JsonGenerator decorate(@Nonnull JsonGenerator generator)
    {
        generator.setPrettyPrinter(new JsonLinesPrettyPrinter());
        generator.useDefaultPrettyPrinter();
        return generator;
    }
}
