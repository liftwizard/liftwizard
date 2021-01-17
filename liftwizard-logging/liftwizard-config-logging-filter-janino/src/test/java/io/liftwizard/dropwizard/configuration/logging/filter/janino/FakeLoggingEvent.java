/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.logging.filter.janino;

import java.util.Map;
import java.util.Objects;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

public class FakeLoggingEvent
        implements ILoggingEvent
{
    private final String message;

    public FakeLoggingEvent(String message)
    {
        this.message = Objects.requireNonNull(message);
    }

    @Override
    public String getThreadName()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getThreadName() not implemented yet");
    }

    @Override
    public Level getLevel()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".getLevel() not implemented yet");
    }

    @Override
    public String getMessage()
    {
        return this.message;
    }

    @Override
    public Object[] getArgumentArray()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getArgumentArray() not implemented yet");
    }

    @Override
    public String getFormattedMessage()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getFormattedMessage() not implemented yet");
    }

    @Override
    public String getLoggerName()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getLoggerName() not implemented yet");
    }

    @Override
    public LoggerContextVO getLoggerContextVO()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getLoggerContextVO() not implemented yet");
    }

    @Override
    public IThrowableProxy getThrowableProxy()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getThrowableProxy() not implemented yet");
    }

    @Override
    public StackTraceElement[] getCallerData()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getCallerData() not implemented yet");
    }

    @Override
    public boolean hasCallerData()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".hasCallerData() not implemented yet");
    }

    @Override
    public Marker getMarker()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".getMarker() not implemented yet");
    }

    @Override
    public Map<String, String> getMDCPropertyMap()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getMDCPropertyMap() not implemented yet");
    }

    @Override
    public Map<String, String> getMdc()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".getMdc() not implemented yet");
    }

    @Override
    public long getTimeStamp()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getTimeStamp() not implemented yet");
    }

    @Override
    public void prepareForDeferredProcessing()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".prepareForDeferredProcessing() not implemented yet");
    }
}
