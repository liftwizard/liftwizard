package com.liftwizard.dropwizard.configuration.logging.appender.encoder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import net.logstash.logback.decorate.JsonFactoryDecorator;

public class ObjectMapperConfigJsonFactoryDecorator implements JsonFactoryDecorator
{
    @Override
    public JsonFactory decorate(JsonFactory factory)
    {
        ObjectMapper objectMapper = (ObjectMapper) factory.getCodec();
        ObjectMapperConfig.configure(objectMapper, true, Include.NON_ABSENT);
        return factory;
    }
}
