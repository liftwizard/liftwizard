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

package io.liftwizard.dropwizard.configuration.logging.appender.logzio;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.common.AbstractAppenderFactory;
import io.dropwizard.logging.common.AppenderFactory;
import io.dropwizard.logging.common.async.AsyncAppenderFactory;
import io.dropwizard.logging.common.filter.FilterFactory;
import io.dropwizard.logging.common.filter.LevelFilterFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import io.logz.logback.LogzioLogbackAppender;

@JsonTypeName("logzio")
@AutoService(AppenderFactory.class)
public class LogzioAppenderFactory extends AbstractAppenderFactory<ILoggingEvent>
{
    private @Valid @NotNull String  logzioToken;
    private @Valid @NotNull String  logzioUrl;
    private @Valid @NotNull String  logzioType  = "java";
    private                 boolean addHostname = true;

    private final int     drainTimeoutSec                      = 5;
    private final int     fileSystemFullPercentThreshold       = 98;
    private       String  queueDir;
    private final int     connectTimeout                       = 10 * 1000;
    private final int     socketTimeout                        = 10 * 1000;
    private       boolean debug;
    private       boolean line;
    private       boolean compressRequests;
    private       boolean inMemoryQueue;
    private final long    inMemoryQueueCapacityBytes           = 100 * 1024 * 1024;
    // private long inMemoryLogsCountCapacity = DONT_LIMIT_CAPACITY;
    private final int     gcPersistedQueueFilesIntervalSeconds = 30;
    // private String format = FORMAT_TEXT;

    @Override
    public Appender<ILoggingEvent> build(
            LoggerContext context,
            String applicationName,
            LayoutFactory<ILoggingEvent> layoutFactory,
            LevelFilterFactory<ILoggingEvent> levelFilterFactory,
            AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory)
    {
        LogzioLogbackAppender appender = new LogzioLogbackAppender();
        appender.setName("logzio-appender");
        appender.setContext(context);

        appender.setToken(this.logzioToken);
        appender.setLogzioType(this.logzioType);
        appender.setLogzioUrl(this.logzioUrl);
        appender.setAddHostname(this.addHostname);

        appender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories()
                .stream()
                .map(FilterFactory::build)
                .forEach(appender::addFilter);
        appender.start();
        return this.wrapAsync(appender, asyncAppenderFactory);
    }

    @JsonProperty
    public String getLogzioToken()
    {
        return this.logzioToken;
    }

    @JsonProperty
    public void setLogzioToken(String logzioToken)
    {
        this.logzioToken = logzioToken;
    }

    @JsonProperty
    public String getLogzioUrl()
    {
        return this.logzioUrl;
    }

    @JsonProperty
    public void setLogzioUrl(String logzioUrl)
    {
        this.logzioUrl = logzioUrl;
    }

    @JsonProperty
    public String getLogzioType()
    {
        return this.logzioType;
    }

    @JsonProperty
    public void setLogzioType(String logzioType)
    {
        this.logzioType = logzioType;
    }

    @JsonProperty
    public boolean isAddHostname()
    {
        return this.addHostname;
    }

    @JsonProperty
    public void setAddHostname(boolean addHostname)
    {
        this.addHostname = addHostname;
    }
}
