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

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
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
import io.dropwizard.util.DataSize;
import io.dropwizard.validation.MinDataSize;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.configuration.logging.logstash.LogstashAccessEncoderFactory;

@JsonTypeName("file-access-logstash")
@AutoService(AppenderFactory.class)
public class LogstashAccessFileAppenderFactory
        extends AbstractAppenderFactory<IAccessEvent>
{
    @Nullable
    private String currentLogFilename;

    private boolean archive = true;

    @Nullable
    private String archivedLogFilenamePattern;

    private @Min(0) int archivedFileCount = 5;

    @Nullable
    private DataSize maxFileSize;

    private @MinDataSize(1) DataSize bufferSize = DataSize.bytes(FileAppender.DEFAULT_BUFFER_SIZE);

    private boolean immediateFlush = true;

    @NotNull
    private LogstashAccessEncoderFactory encoderFactory = new LogstashAccessEncoderFactory();

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
    public DataSize getMaxFileSize()
    {
        return this.maxFileSize;
    }

    @JsonProperty
    public void setMaxFileSize(DataSize maxFileSize)
    {
        this.maxFileSize = maxFileSize;
    }

    @JsonProperty
    public DataSize getBufferSize()
    {
        return this.bufferSize;
    }

    @JsonProperty
    public void setBufferSize(DataSize bufferSize)
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
    public LogstashAccessEncoderFactory getEncoder()
    {
        return this.encoderFactory;
    }

    @JsonProperty
    public void setEncoder(LogstashAccessEncoderFactory newEncoderFactory)
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
    public Appender<IAccessEvent> build(
            LoggerContext context,
            String applicationName,
            LayoutFactory<IAccessEvent> layoutFactory,
            LevelFilterFactory<IAccessEvent> levelFilterFactory,
            AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory)
    {
        Encoder<IAccessEvent>              encoder  = this.encoderFactory.build(this.getTimeZone());
        OutputStreamAppender<IAccessEvent> appender = this.appender(context);
        appender.setEncoder(encoder);
        encoder.start();

        appender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories().stream().map(FilterFactory::build).forEach(appender::addFilter);
        appender.start();
        return this.wrapAsync(appender, asyncAppenderFactory);
    }

    private FileAppender<IAccessEvent> appender(LoggerContext context)
    {
        FileAppender<IAccessEvent> appender = this.buildAppender(context);
        appender.setName("file-access-logstash-appender");
        appender.setAppend(true);
        appender.setContext(context);
        appender.setImmediateFlush(this.immediateFlush);
        appender.setPrudent(false);
        return appender;
    }

    private FileAppender<IAccessEvent> buildAppender(LoggerContext context)
    {
        if (!this.archive)
        {
            var appender = new FileAppender<IAccessEvent>();
            this.configureAppender(appender, context);
            return appender;
        }

        var appender = new RollingFileAppender<IAccessEvent>();
        this.configureAppender(appender, context);

        return this.maxFileSize == null || Objects.requireNonNull(this.archivedLogFilenamePattern).contains("%d")
                ? this.configurePolicyWithDefaults(appender, context)
                : this.dateAndSizeSpecifiedPolicy(appender, context);
    }

    private RollingFileAppender<IAccessEvent> configurePolicyWithDefaults(
            RollingFileAppender<IAccessEvent> appender,
            LoggerContext context)
    {
        TimeBasedRollingPolicy<IAccessEvent> rollingPolicy = this.maxFileSize == null
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
    private TimeBasedRollingPolicy<IAccessEvent> getSizeAndTimeBasedRollingPolicy()
    {
        // Creating a size and time policy does not need a separate triggering policy set on the appender
        // because this policy registers the trigger policy
        if (this.maxFileSize == null)
        {
            throw new AssertionError();
        }
        var fileSize                      = new FileSize(this.maxFileSize.toBytes());
        var sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy<IAccessEvent>();
        sizeAndTimeBasedRollingPolicy.setMaxFileSize(fileSize);
        return sizeAndTimeBasedRollingPolicy;
    }

    @Nonnull
    private TimeBasedRollingPolicy<IAccessEvent> getTimeBasedRollingPolicy(
            RollingFileAppender<IAccessEvent> appender,
            LoggerContext context)
    {
        TimeBasedFileNamingAndTriggeringPolicy<IAccessEvent> triggeringPolicy =
                new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
        triggeringPolicy.setContext(context);

        var rollingPolicy = new TimeBasedRollingPolicy<IAccessEvent>();
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(triggeringPolicy);
        return rollingPolicy;
    }

    private RollingFileAppender<IAccessEvent> dateAndSizeSpecifiedPolicy(
            RollingFileAppender<IAccessEvent> appender,
            LoggerContext context)
    {
        var rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setMaxIndex(this.archivedFileCount);
        rollingPolicy.setFileNamePattern(this.archivedLogFilenamePattern);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        if (this.maxFileSize == null)
        {
            throw new AssertionError();
        }
        var fileSize         = new FileSize(this.maxFileSize.toBytes());
        var triggeringPolicy = new SizeBasedTriggeringPolicy<IAccessEvent>();
        triggeringPolicy.setMaxFileSize(fileSize);
        triggeringPolicy.setContext(context);
        triggeringPolicy.start();
        appender.setTriggeringPolicy(triggeringPolicy);
        return appender;
    }

    private void configureAppender(FileAppender<IAccessEvent> appender, LoggerContext context)
    {
        var fileSize = new FileSize(this.bufferSize.toBytes());

        appender.setContext(context);
        appender.setFile(this.currentLogFilename);
        appender.setBufferSize(fileSize);
    }
}
