/*
 * Copyright 2024 Craig Motlin
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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.logstash.logback.decorate.JsonFactoryDecorator;
import net.logstash.logback.encoder.LogstashEncoder;

public class LogstashEncoderFactory
{
    private          boolean includeContext             = true;
    private          boolean includeMdc                 = true;
    private          boolean includeStructuredArguments = true;
    private          boolean includedNonStructuredArguments;
    private          boolean includeTags                = true;
    private          String  customFields;
    private          boolean prettyPrint;
    private @NotNull Include serializationInclusion     = Include.NON_ABSENT;

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
    public boolean isIncludeMdc()
    {
        return this.includeMdc;
    }

    @JsonProperty
    public void setIncludeMdc(boolean includeMdc)
    {
        this.includeMdc = includeMdc;
    }

    @JsonProperty
    public boolean isIncludeStructuredArguments()
    {
        return this.includeStructuredArguments;
    }

    @JsonProperty
    public void setIncludeStructuredArguments(boolean includeStructuredArguments)
    {
        this.includeStructuredArguments = includeStructuredArguments;
    }

    @JsonProperty
    public boolean isIncludedNonStructuredArguments()
    {
        return this.includedNonStructuredArguments;
    }

    @JsonProperty
    public void setIncludedNonStructuredArguments(boolean includedNonStructuredArguments)
    {
        this.includedNonStructuredArguments = includedNonStructuredArguments;
    }

    @JsonProperty
    public boolean isIncludeTags()
    {
        return this.includeTags;
    }

    @JsonProperty
    public void setIncludeTags(boolean includeTags)
    {
        this.includeTags = includeTags;
    }

    @JsonProperty
    public String getCustomFields()
    {
        return this.customFields;
    }

    @JsonProperty
    public void setCustomFields(String customFields)
    {
        this.customFields = customFields;
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

    public Encoder<ILoggingEvent> build(boolean includeCallerData)
    {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setIncludeCallerData(includeCallerData);
        encoder.setIncludeContext(this.includeContext);
        encoder.setIncludeMdc(this.includeMdc);
        encoder.setIncludeStructuredArguments(this.includeStructuredArguments);
        encoder.setIncludeNonStructuredArguments(this.includedNonStructuredArguments);
        encoder.setIncludeTags(this.includeTags);
        encoder.setCustomFields(this.customFields);
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
