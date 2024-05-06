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

package io.liftwizard.dropwizard.configuration.logging.appender.console.logstash;

import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
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
import io.liftwizard.dropwizard.configuration.logging.logstash.LogstashEncoderFactory;

@JsonTypeName("console-logstash")
@AutoService(AppenderFactory.class)
public class LogstashConsoleAppenderFactory
        extends AbstractAppenderFactory<ILoggingEvent>
{
    @NotNull
    private ConsoleStream target = ConsoleStream.STDOUT;

    @NotNull
    private LogstashEncoderFactory encoderFactory = new LogstashEncoderFactory();

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
    public LogstashEncoderFactory getEncoder()
    {
        return this.encoderFactory;
    }

    @JsonProperty
    public void setEncoder(LogstashEncoderFactory newEncoderFactory)
    {
        this.encoderFactory = newEncoderFactory;
    }

    @Override
    public Appender<ILoggingEvent> build(
            LoggerContext context,
            String applicationName,
            LayoutFactory<ILoggingEvent> layoutFactory,
            LevelFilterFactory<ILoggingEvent> levelFilterFactory,
            AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory)
    {
        Encoder<ILoggingEvent>              encoder  = this.encoderFactory.build(this.isIncludeCallerData(), this.getTimeZone());
        OutputStreamAppender<ILoggingEvent> appender = this.appender(context);
        appender.setEncoder(encoder);
        encoder.start();

        appender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories().stream().map(FilterFactory::build).forEach(appender::addFilter);
        appender.start();
        return this.wrapAsync(appender, asyncAppenderFactory);
    }

    private OutputStreamAppender<ILoggingEvent> appender(LoggerContext context)
    {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setName("console-logstash-appender");
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
