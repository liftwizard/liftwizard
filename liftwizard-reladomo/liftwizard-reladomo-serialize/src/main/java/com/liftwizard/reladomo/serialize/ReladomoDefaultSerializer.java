package com.liftwizard.reladomo.serialize;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;
import com.gs.fw.common.mithra.util.serializer.Serialized;
import com.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import io.dropwizard.jackson.Jackson;

public final class ReladomoDefaultSerializer
{
    private ReladomoDefaultSerializer()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String serialize(@Nonnull MithraObject mithraObject)
    {
        // TODO: Initialize with shared ObjectMapper
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper, true, Include.NON_ABSENT);
        return serialize(mithraObject, objectMapper);
    }

    public static String serialize(@Nonnull MithraObject mithraObject, @Nonnull ObjectMapper objectMapper)
    {
        RelatedFinder            finder              = mithraObject.zGetPortal().getFinder();
        SerializationConfig      serializationConfig = SerializationConfig.shallowWithDefaultAttributes(finder);
        Serialized<MithraObject> serialized          = new Serialized<>(mithraObject, serializationConfig);

        try
        {
            return objectMapper.writeValueAsString(serialized);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
