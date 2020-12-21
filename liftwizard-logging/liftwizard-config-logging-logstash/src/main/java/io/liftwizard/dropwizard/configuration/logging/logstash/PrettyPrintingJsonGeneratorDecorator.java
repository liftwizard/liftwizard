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

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonGenerator;
import io.liftwizard.serialization.jackson.pretty.JsonLinesPrettyPrinter;
import net.logstash.logback.decorate.JsonGeneratorDecorator;

public class PrettyPrintingJsonGeneratorDecorator
        implements JsonGeneratorDecorator
{
    @Override
    public JsonGenerator decorate(@Nonnull JsonGenerator generator)
    {
        generator.setPrettyPrinter(new JsonLinesPrettyPrinter());
        generator.useDefaultPrettyPrinter();
        return generator;
    }
}
