/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.reladomo.serialize;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;
import com.gs.fw.common.mithra.util.serializer.Serialized;
import io.dropwizard.jackson.Jackson;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;

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
        ObjectMapperConfig.configure(objectMapper);
        return serialize(mithraObject, objectMapper);
    }

    public static String serialize(@Nonnull MithraObject mithraObject, @Nonnull ObjectMapper objectMapper)
    {
        RelatedFinder finder = mithraObject.zGetPortal().getFinder();
        SerializationConfig serializationConfig = SerializationConfig.shallowWithDefaultAttributes(finder);
        Serialized<MithraObject> serialized = new Serialized<>(mithraObject, serializationConfig);

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
