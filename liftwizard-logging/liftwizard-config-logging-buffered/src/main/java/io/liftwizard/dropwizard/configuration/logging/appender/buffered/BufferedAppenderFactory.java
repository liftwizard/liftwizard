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

package io.liftwizard.dropwizard.configuration.logging.appender.buffered;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.liftwizard.logging.logback.appender.buffered.BufferedAppender;

@JsonTypeName("buffered")
@AutoService(AppenderFactory.class)
public class BufferedAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E>
{
    private String appenderName = "buffered-appender";

    @JsonProperty
    public String getAppenderName()
    {
        return this.appenderName;
    }

    @JsonProperty
    public void setAppenderName(String appenderName)
    {
        this.appenderName = appenderName;
    }

    @Override
    public Appender<E> build(
            LoggerContext loggerContext,
            String applicationName,
            LayoutFactory<E> layoutFactory,
            LevelFilterFactory<E> levelFilterFactory,
            AsyncAppenderFactory<E> asyncAppenderFactory)
    {
        var consoleAppender = new ConsoleAppender<E>();
        consoleAppender.setName(this.appenderName);
        consoleAppender.setContext(loggerContext);

        var layoutWrappingEncoder = new LayoutWrappingEncoder<E>();
        layoutWrappingEncoder.setLayout(this.buildLayout(loggerContext, layoutFactory));
        consoleAppender.setEncoder(layoutWrappingEncoder);

        consoleAppender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories().stream().map(FilterFactory::build).forEach(consoleAppender::addFilter);
        consoleAppender.start();

        BufferedAppender<E> bufferedAppender = new BufferedAppender<E>();
        bufferedAppender.setContext(consoleAppender.getContext());
        bufferedAppender.setName("buffered-" + this.appenderName);
        bufferedAppender.addAppender(consoleAppender);
        bufferedAppender.start();
        return bufferedAppender;
    }
}
