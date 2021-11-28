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

package io.liftwizard.servlet.logging.logstash.encoder;

import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredArgumentsLogstashEncoderLogger
        implements BiConsumer<StructuredArguments, Optional<String>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredArgumentsLogstashEncoderLogger.class);

    @Override
    public void accept(@Nonnull StructuredArguments structuredArguments, @Nonnull Optional<String> maybeBody)
    {
        String message = maybeBody.orElseGet(structuredArguments::getEvent);
        // TODO 2021-12-07: Null out the event here

        LOGGER.debug(
                Markers.appendFields(structuredArguments),
                message);
    }
}
