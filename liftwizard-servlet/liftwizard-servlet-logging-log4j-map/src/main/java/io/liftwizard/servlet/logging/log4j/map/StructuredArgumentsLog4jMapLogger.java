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

package io.liftwizard.servlet.logging.log4j.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.apache.log4j.Logger;

public class StructuredArgumentsLog4jMapLogger
        implements Consumer<StructuredArguments>
{
    private static final Logger LOGGER = Logger.getLogger(StructuredArgumentsLog4jMapLogger.class);

    @Nonnull
    private final ObjectMapper objectMapper;

    public StructuredArgumentsLog4jMapLogger(@Nonnull ObjectMapper objectMapper)
    {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void accept(StructuredArguments structuredArguments)
    {
        Map<?, ?> structuredArgumentsMap = this.objectMapper.convertValue(structuredArguments, Map.class);
        Map<?, ?> mapWithToString = new HashMap<Object, Object>(structuredArgumentsMap)
        {
            @Override
            public String toString()
            {
                return "Response sent";
            }
        };
        LOGGER.debug(mapWithToString);
    }
}
