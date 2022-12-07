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

import javax.validation.constraints.NotNull;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.encoder.Encoder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.logstash.logback.decorate.JsonFactoryDecorator;
import net.logstash.logback.encoder.LogstashAccessEncoder;

public class LogstashAccessEncoderFactory
{
    private          boolean includeContext         = true;
    private          boolean prettyPrint;
    private @NotNull Include serializationInclusion = Include.NON_ABSENT;

    @JsonProperty
    public boolean isIncludeContext()
    {
        return this.includeContext;
    }

    @JsonProperty
    public void setIncludeContext(boolean includeContext)
    {
        this.includeContext = includeContext;
    }

    @JsonProperty
    public boolean isPrettyPrint()
    {
        return this.prettyPrint;
    }

    @JsonProperty
    public void setPrettyPrint(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    @JsonProperty
    public Include getSerializationInclusion()
    {
        return this.serializationInclusion;
    }

    @JsonProperty
    public void setSerializationInclusion(Include serializationInclusion)
    {
        this.serializationInclusion = serializationInclusion;
    }

    public Encoder<IAccessEvent> build()
    {
        LogstashAccessEncoder encoder = new LogstashAccessEncoder();
        encoder.setIncludeContext(this.includeContext);
        if (this.prettyPrint)
        {
            encoder.setJsonGeneratorDecorator(new PrettyPrintingJsonGeneratorDecorator());
        }
        JsonFactoryDecorator decorator = new ObjectMapperConfigJsonFactoryDecorator(
                this.prettyPrint,
                this.serializationInclusion);
        encoder.setJsonFactoryDecorator(decorator);
        return encoder;
    }
}
