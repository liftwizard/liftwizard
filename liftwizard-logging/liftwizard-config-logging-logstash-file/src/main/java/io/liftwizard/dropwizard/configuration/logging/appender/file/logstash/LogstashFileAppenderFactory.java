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

package io.liftwizard.dropwizard.configuration.logging.appender.file.logstash;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.util.Size;
import io.dropwizard.validation.MinSize;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.configuration.logging.logstash.LogstashEncoderFactory;

/**
 * A Dropwizard AppenderFactory that sets up a file appender that logs one json object per log statement. The json is formatted by <a href="https://github.com/logstash/logstash-logback-encoder">logstash-logback-encoder</a> and is ready to be parsed by logstash.
 *
 * @see <a href="https://liftwizard.io/docs/logging/logging-modules#logging-modules-logstash-encoder">https://liftwizard.io/docs/logging/logging-modules#logging-modules-logstash-encoder</a>
 */
@JsonTypeName("file-logstash")
@AutoService(AppenderFactory.class)
public class LogstashFileAppenderFactory
        extends AbstractAppenderFactory<ILoggingEvent>
{
    @Nullable
    private String currentLogFilename;

    private boolean archive = true;

    @Nullable
    private String archivedLogFilenamePattern;

    private @Min(0) int archivedFileCount = 5;

    @Nullable
    private Size maxFileSize;

    private @MinSize(1) Size bufferSize = Size.bytes(FileAppender.DEFAULT_BUFFER_SIZE);

    private boolean immediateFlush = true;

    @NotNull
    private LogstashEncoderFactory encoderFactory = new LogstashEncoderFactory();

    @JsonProperty
    @Nullable
    public String getCurrentLogFilename()
    {
        return this.currentLogFilename;
    }

    @JsonProperty
    public void setCurrentLogFilename(@Nullable String currentLogFilename)
    {
        this.currentLogFilename = currentLogFilename;
    }

    @JsonProperty
    public boolean isArchive()
    {
        return this.archive;
    }

    @JsonProperty
    public void setArchive(boolean archive)
    {
        this.archive = archive;
    }

    @JsonProperty
    @Nullable
    public String getArchivedLogFilenamePattern()
    {
        return this.archivedLogFilenamePattern;
    }

    @JsonProperty
    public void setArchivedLogFilenamePattern(String archivedLogFilenamePattern)
    {
        this.archivedLogFilenamePattern = archivedLogFilenamePattern;
    }

    @JsonProperty
    public int getArchivedFileCount()
    {
        return this.archivedFileCount;
    }

    @JsonProperty
    public void setArchivedFileCount(int archivedFileCount)
    {
        this.archivedFileCount = archivedFileCount;
    }

    @JsonProperty
    @Nullable
    public Size getMaxFileSize()
    {
        return this.maxFileSize;
    }

    @JsonProperty
    public void setMaxFileSize(Size maxFileSize)
    {
        this.maxFileSize = maxFileSize;
    }

    @JsonProperty
    public Size getBufferSize()
    {
        return this.bufferSize;
    }

    @JsonProperty
    public void setBufferSize(Size bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public boolean isImmediateFlush()
    {
        return this.immediateFlush;
    }

    @JsonProperty
    public void setImmediateFlush(boolean immediateFlush)
    {
        this.immediateFlush = immediateFlush;
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

    @JsonIgnore
    @ValidationMethod(message = "must have archivedLogFilenamePattern if archive is true")
    public boolean isValidArchiveConfiguration()
    {
        return !this.archive || this.archivedLogFilenamePattern != null;
    }

    @JsonIgnore
    @ValidationMethod(message = "when specifying maxFileSize, archivedLogFilenamePattern must contain %i")
    public boolean isValidForMaxFileSizeSetting()
    {
        return !this.archive
                || this.maxFileSize == null
                || this.isValidMaxFileSizePattern();
    }

    @JsonIgnore
    @ValidationMethod(message = "when archivedLogFilenamePattern contains %i, maxFileSize must be specified")
    public boolean isMaxFileSizeSettingSpecified()
    {
        return !this.archive
                || !this.isValidMaxFileSizePattern()
                || this.maxFileSize != null;
    }

    private boolean isValidMaxFileSizePattern()
    {
        return this.archivedLogFilenamePattern != null && this.archivedLogFilenamePattern.contains("%i");
    }

    @JsonIgnore
    @ValidationMethod(message = "currentLogFilename can only be null when archiving is enabled")
    public boolean isValidFileConfiguration()
    {
        return this.archive || this.currentLogFilename != null;
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
        FileAppender<ILoggingEvent> appender = this.buildAppender(context);
        appender.setName("file-logstash-appender");
        appender.setAppend(true);
        appender.setContext(context);
        appender.setImmediateFlush(this.immediateFlush);
        appender.setPrudent(false);
        return appender;
    }

    private FileAppender<ILoggingEvent> buildAppender(LoggerContext context)
    {
        if (!this.archive)
        {
            FileAppender<ILoggingEvent> appender = new FileAppender<>();
            this.configureAppender(appender, context);
            return appender;
        }

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        this.configureAppender(appender, context);

        return this.maxFileSize == null || Objects.requireNonNull(this.archivedLogFilenamePattern).contains("%d")
                ? this.configurePolicyWithDefaults(appender, context)
                : this.dateAndSizeSpecifiedPolicy(appender, context);
    }

    private RollingFileAppender<ILoggingEvent> configurePolicyWithDefaults(
            RollingFileAppender<ILoggingEvent> appender,
            LoggerContext context)
    {
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = this.maxFileSize == null
                ? this.getTimeBasedRollingPolicy(appender, context)
                : this.getSizeAndTimeBasedRollingPolicy();

        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(this.archivedLogFilenamePattern);
        rollingPolicy.setMaxHistory(this.archivedFileCount);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);
        return appender;
    }

    @Nonnull
    private TimeBasedRollingPolicy<ILoggingEvent> getSizeAndTimeBasedRollingPolicy()
    {
        // Creating a size and time policy does not need a separate triggering policy set on the appender
        // because this policy registers the trigger policy

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> sizeAndTimeBasedRollingPolicy =
                new SizeAndTimeBasedRollingPolicy<>();

        FileSize fileSize = new FileSize(this.maxFileSize.toBytes());
        sizeAndTimeBasedRollingPolicy.setMaxFileSize(fileSize);
        return sizeAndTimeBasedRollingPolicy;
    }

    @Nonnull
    private TimeBasedRollingPolicy<ILoggingEvent> getTimeBasedRollingPolicy(
            RollingFileAppender<ILoggingEvent> appender,
            LoggerContext context)
    {
        TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy =
                new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
        triggeringPolicy.setContext(context);

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(triggeringPolicy);
        return rollingPolicy;
    }

    private RollingFileAppender<ILoggingEvent> dateAndSizeSpecifiedPolicy(
            RollingFileAppender<ILoggingEvent> appender,
            LoggerContext context)
    {
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setMaxIndex(this.archivedFileCount);
        rollingPolicy.setFileNamePattern(this.archivedLogFilenamePattern);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();

        FileSize fileSize = new FileSize(this.maxFileSize.toBytes());
        triggeringPolicy.setMaxFileSize(fileSize);
        triggeringPolicy.setContext(context);
        triggeringPolicy.start();
        appender.setTriggeringPolicy(triggeringPolicy);
        return appender;
    }

    private void configureAppender(FileAppender<ILoggingEvent> appender, LoggerContext context)
    {
        FileSize fileSize = new FileSize(this.bufferSize.toBytes());

        appender.setContext(context);
        appender.setFile(this.currentLogFilename);
        appender.setBufferSize(fileSize);
    }
}
