/*
 * Copyright 2020 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.logging.logstash;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import net.logstash.logback.decorate.JsonFactoryDecorator;

public class ObjectMapperConfigJsonFactoryDecorator
        implements JsonFactoryDecorator
{
    private final boolean prettyPrint;
    private final Include serializationInclusion;

    public ObjectMapperConfigJsonFactoryDecorator(boolean prettyPrint, Include serializationInclusion)
    {
        this.prettyPrint            = prettyPrint;
        this.serializationInclusion = serializationInclusion;
    }

    @Override
    public JsonFactory decorate(JsonFactory factory)
    {
        ObjectMapper objectMapper = (ObjectMapper) factory.getCodec();
        ObjectMapperConfig.configure(objectMapper, this.prettyPrint, this.serializationInclusion);
        return factory;
    }
}
