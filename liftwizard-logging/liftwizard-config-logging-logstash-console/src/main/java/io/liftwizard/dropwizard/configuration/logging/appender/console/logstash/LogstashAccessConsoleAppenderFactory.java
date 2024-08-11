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

package io.liftwizard.dropwizard.configuration.logging.appender.console.logstash;

import javax.validation.constraints.NotNull;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.liftwizard.dropwizard.configuration.logging.logstash.LogstashAccessEncoderFactory;

@JsonTypeName("console-access-logstash")
@AutoService(AppenderFactory.class)
public class LogstashAccessConsoleAppenderFactory
        extends AbstractAppenderFactory<IAccessEvent>
{
    @NotNull
    private ConsoleStream target = ConsoleStream.STDOUT;

    @NotNull
    private LogstashAccessEncoderFactory encoderFactory = new LogstashAccessEncoderFactory();

    @JsonProperty
    public ConsoleStream getTarget()
    {
        return this.target;
    }

    @JsonProperty
    public void setTarget(ConsoleStream target)
    {
        this.target = target;
    }

    @JsonProperty
    public LogstashAccessEncoderFactory getEncoder()
    {
        return this.encoderFactory;
    }

    @JsonProperty
    public void setEncoder(LogstashAccessEncoderFactory newEncoderFactory)
    {
        this.encoderFactory = newEncoderFactory;
    }

    @Override
    public Appender<IAccessEvent> build(
            LoggerContext context,
            String applicationName,
            LayoutFactory<IAccessEvent> layoutFactory,
            LevelFilterFactory<IAccessEvent> levelFilterFactory,
            AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory)
    {
        Encoder<IAccessEvent> encoder = this.encoderFactory.build(this.getTimeZone());
        OutputStreamAppender<IAccessEvent> appender = this.appender(context);
        appender.setEncoder(encoder);
        encoder.start();

        appender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories().stream().map(FilterFactory::build).forEach(appender::addFilter);
        appender.start();
        return this.wrapAsync(appender, asyncAppenderFactory);
    }

    private OutputStreamAppender<IAccessEvent> appender(Context context)
    {
        ConsoleAppender<IAccessEvent> appender = new ConsoleAppender<>();
        appender.setName("console-access-logstash-appender");
        appender.setContext(context);
        appender.setTarget(this.target.get());
        return appender;
    }

    @SuppressWarnings("UnusedDeclaration")
    public enum ConsoleStream
    {
        STDOUT("System.out"),
        STDERR("System.err");

        private final String value;

        ConsoleStream(String value)
        {
            this.value = value;
        }

        public String get()
        {
            return this.value;
        }
    }
}
