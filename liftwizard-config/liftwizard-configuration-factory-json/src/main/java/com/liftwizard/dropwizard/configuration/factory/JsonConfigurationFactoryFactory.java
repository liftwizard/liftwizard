package com.liftwizard.dropwizard.configuration.factory;

import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.JsonConfigurationFactory;

/**
 * Allows configuring Dropwizard using json.
 * Based on {@link DefaultConfigurationFactoryFactory} but replacing yml with json.
 *
 * @see DefaultConfigurationFactoryFactory
 */
public class JsonConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T>
{
    @Override
    public ConfigurationFactory<T> create(
            Class<T> aClass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix)
    {
        ObjectMapper strictObjectMapper = this.getStrictObjectMapper(objectMapper);
        return new JsonConfigurationFactory<>(
                aClass,
                validator,
                strictObjectMapper,
                propertyPrefix);
    }

    private ObjectMapper getStrictObjectMapper(ObjectMapper objectMapper)
    {
        ObjectMapper strictObjectMapper = objectMapper.copy();
        strictObjectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        strictObjectMapper.enable(Feature.STRICT_DUPLICATE_DETECTION);

        strictObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        strictObjectMapper.enable(Feature.ALLOW_COMMENTS);
        strictObjectMapper.enable(Feature.ALLOW_YAML_COMMENTS);
        strictObjectMapper.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        strictObjectMapper.enable(Feature.ALLOW_TRAILING_COMMA);

        strictObjectMapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        strictObjectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);
        strictObjectMapper.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        strictObjectMapper.enable(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        strictObjectMapper.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);

        strictObjectMapper.setDateFormat(new StdDateFormat());

        return strictObjectMapper;
    }
}
