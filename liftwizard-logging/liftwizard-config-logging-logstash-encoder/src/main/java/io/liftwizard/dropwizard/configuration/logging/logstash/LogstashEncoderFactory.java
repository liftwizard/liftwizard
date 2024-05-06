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

import java.util.List;
import java.util.TimeZone;

import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.logstash.logback.composite.ContextJsonProvider;
import net.logstash.logback.composite.GlobalCustomFieldsJsonProvider;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.composite.loggingevent.CallerDataJsonProvider;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventNestedJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventThreadNameJsonProvider;
import net.logstash.logback.composite.loggingevent.LogstashMarkersJsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.composite.loggingevent.RootStackTraceElementJsonProvider;
import net.logstash.logback.composite.loggingevent.StackHashJsonProvider;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import net.logstash.logback.composite.loggingevent.TagsJsonProvider;
import net.logstash.logback.composite.loggingevent.ThrowableClassNameJsonProvider;
import net.logstash.logback.composite.loggingevent.ThrowableMessageJsonProvider;
import net.logstash.logback.composite.loggingevent.ThrowableRootCauseClassNameJsonProvider;
import net.logstash.logback.composite.loggingevent.ThrowableRootCauseMessageJsonProvider;
import net.logstash.logback.decorate.JsonFactoryDecorator;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;

public class LogstashEncoderFactory
{
    private          boolean      includeContext                   = true;
    private          boolean      includeMdc                       = true;
    private          boolean      includeStructuredArguments       = true;
    private          boolean      includedNonStructuredArguments;
    private          boolean      includeTags                      = true;
    private          boolean      rootCauseFirst                   = true;
    private          List<String> truncateStackTracesAfterPrefixes = List.of(
            "^org\\.junit\\.platform\\.engine",
            "^org\\.junit\\.jupiter\\.engine");
    private          ObjectNode   customFields;
    private          boolean      prettyPrint;
    private @NotNull Include      serializationInclusion           = Include.NON_ABSENT;

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
    public ObjectNode getCustomFields()
    {
        return this.customFields;
    }

    @JsonProperty
    public void setCustomFields(ObjectNode customFields)
    {
        this.customFields = customFields;
    }

    @JsonProperty
    public boolean isRootCauseFirst()
    {
        return this.rootCauseFirst;
    }

    @JsonProperty
    public void setRootCauseFirst(boolean rootCauseFirst)
    {
        this.rootCauseFirst = rootCauseFirst;
    }

    @JsonProperty
    public List<String> getTruncateStackTracesAfterPrefixes()
    {
        return this.truncateStackTracesAfterPrefixes;
    }

    @JsonProperty
    public void setTruncateStackTracesAfterPrefixes(List<String> truncateStackTracesAfterPrefixes)
    {
        this.truncateStackTracesAfterPrefixes = truncateStackTracesAfterPrefixes;
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

    public Encoder<ILoggingEvent> build(boolean includeCallerData, TimeZone timeZone)
    {
        var encoder = new LoggingEventCompositeJsonEncoder();

        JsonProviders<ILoggingEvent> providers = this.getProviders(includeCallerData, timeZone);
        encoder.setProviders(providers);

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

    private JsonProviders<ILoggingEvent> getProviders(
            boolean includeCallerData,
            TimeZone timeZone)
    {
        JsonProviders<ILoggingEvent> providers = new JsonProviders<>();

        providers.addProvider(getTimestampProvider(timeZone));
        providers.addProvider(new MessageJsonProvider());
        providers.addProvider(new LoggerNameJsonProvider());
        providers.addProvider(new LoggingEventThreadNameJsonProvider());
        providers.addProvider(new LogLevelJsonProvider());

        if (includeCallerData)
        {
            providers.addProvider(nest("caller", getCallerDataProvider()));
        }

        if (this.includeContext)
        {
            providers.addProvider(new ContextJsonProvider<>());
        }

        if (this.includeMdc)
        {
            providers.addProvider(nest("mdc", new MdcJsonProvider()));
        }

        providers.addProvider(nest("arguments", this.getArgumentsJsonProvider()));

        if (this.includeTags)
        {
            providers.addProvider(new TagsJsonProvider());
        }

        providers.addProvider(new LogstashMarkersJsonProvider());

        if (this.customFields != null && !this.customFields.isEmpty())
        {
            providers.addProvider(this.getGlobalCustomFieldsProvider());
        }

        providers.addProvider(nest("error", this.getErrorProvider()));
        return providers;
    }

    private static LoggingEventFormattedTimestampJsonProvider getTimestampProvider(TimeZone timeZone)
    {
        var provider = new LoggingEventFormattedTimestampJsonProvider();
        provider.setTimeZone(timeZone.getID());
        return provider;
    }

    private static CallerDataJsonProvider getCallerDataProvider()
    {
        var provider = new CallerDataJsonProvider();
        provider.setClassFieldName("class_name");
        provider.setMethodFieldName("method_name");
        provider.setFileFieldName("file_name");
        provider.setLineFieldName("line_number");
        return provider;
    }

    private GlobalCustomFieldsJsonProvider<ILoggingEvent> getGlobalCustomFieldsProvider()
    {
        var provider = new GlobalCustomFieldsJsonProvider<ILoggingEvent>();
        provider.setCustomFieldsNode(this.customFields);
        return provider;
    }

    private JsonProviders<ILoggingEvent> getErrorProvider()
    {
        JsonProviders<ILoggingEvent> providers = new JsonProviders<>();
        providers.addProvider(this.getStackTraceProvider());
        providers.addProvider(getThrowableClassNameProvider());
        providers.addProvider(getThrowableRootCauseClassNameJson());
        providers.addProvider(new ThrowableMessageJsonProvider());
        providers.addProvider(new ThrowableRootCauseMessageJsonProvider());
        providers.addProvider(new StackHashJsonProvider());
        providers.addProvider(new RootStackTraceElementJsonProvider());

        return providers;
    }

    private StackTraceJsonProvider getStackTraceProvider()
    {
        var provider = new StackTraceJsonProvider();
        provider.setThrowableConverter(this.getThrowableConverter());
        return provider;
    }

    private static ThrowableClassNameJsonProvider getThrowableClassNameProvider()
    {
        var provider = new ThrowableClassNameJsonProvider();
        provider.setUseSimpleClassName(false);
        return provider;
    }

    private static ThrowableRootCauseClassNameJsonProvider getThrowableRootCauseClassNameJson()
    {
        var provider = new ThrowableRootCauseClassNameJsonProvider();
        provider.setUseSimpleClassName(false);
        return provider;
    }

    private ShortenedThrowableConverter getThrowableConverter()
    {
        var throwableConverter = new ShortenedThrowableConverter();
        if (this.rootCauseFirst)
        {
            throwableConverter.setRootCauseFirst(true);
        }
        this.truncateStackTracesAfterPrefixes.forEach(throwableConverter::addTruncateAfter);
        return throwableConverter;
    }

    private ArgumentsJsonProvider getArgumentsJsonProvider()
    {
        var provider = new ArgumentsJsonProvider();
        provider.setIncludeStructuredArguments(this.includeStructuredArguments);
        provider.setIncludeNonStructuredArguments(this.includedNonStructuredArguments);
        return provider;
    }

    private static LoggingEventNestedJsonProvider nest(
            String fieldName,
            JsonProvider<ILoggingEvent> delegateProvider)
    {
        var provider = new LoggingEventNestedJsonProvider();
        provider.setFieldName(fieldName);
        provider.setProviders(wrap(delegateProvider));
        return provider;
    }

    private static LoggingEventNestedJsonProvider nest(
            String fieldName,
            JsonProviders<ILoggingEvent> jsonProviders)
    {
        var provider = new LoggingEventNestedJsonProvider();
        provider.setFieldName(fieldName);
        provider.setProviders(jsonProviders);
        return provider;
    }

    private static JsonProviders<ILoggingEvent> wrap(JsonProvider<ILoggingEvent> delegateProvider)
    {
        JsonProviders<ILoggingEvent> providers = new JsonProviders<>();
        providers.addProvider(delegateProvider);
        return providers;
    }
}
