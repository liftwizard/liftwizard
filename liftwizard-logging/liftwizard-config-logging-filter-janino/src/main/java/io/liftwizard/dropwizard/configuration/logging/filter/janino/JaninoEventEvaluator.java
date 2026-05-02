/*
 * Copyright 2026 Craig Motlin
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import org.codehaus.janino.ScriptEvaluator;
import org.slf4j.Marker;

/**
 * A Janino-based event evaluator for Logback that evaluates Java boolean expressions
 * against logging events.
 *
 * <p>This class reimplements the functionality of {@code ch.qos.logback.classic.boolex.JaninoEventEvaluator}
 * which was removed in Logback 1.3.x. It uses {@code org.codehaus.janino.ScriptEvaluator}
 * directly to compile and evaluate Java expressions at runtime.</p>
 *
 * <p>The following variables are available in expressions:</p>
 * <ul>
 *   <li>{@code DEBUG}, {@code INFO}, {@code WARN}, {@code ERROR} - int level constants</li>
 *   <li>{@code event} - the {@link ILoggingEvent}</li>
 *   <li>{@code message} - the raw message string</li>
 *   <li>{@code formattedMessage} - the formatted message string</li>
 *   <li>{@code logger} - the logger name</li>
 *   <li>{@code loggerContext} - the {@link LoggerContextVO}</li>
 *   <li>{@code level} - the log level as an int</li>
 *   <li>{@code timeStamp} - the event timestamp as a long</li>
 *   <li>{@code marker} - the {@link Marker}</li>
 *   <li>{@code mdc} - the MDC property map</li>
 *   <li>{@code throwableProxy} - the {@link IThrowableProxy}</li>
 *   <li>{@code throwable} - the {@link Throwable}</li>
 * </ul>
 */
public class JaninoEventEvaluator extends EventEvaluatorBase<ILoggingEvent>
{
    private static final String IMPORT_LEVEL = "import ch.qos.logback.classic.Level;\r\n";

    private static final Class<?>[] THROWN_EXCEPTIONS = {EvaluationException.class};

    private static final String[] PARAMETER_NAMES = {
        "DEBUG",
        "INFO",
        "WARN",
        "ERROR",
        "event",
        "message",
        "formattedMessage",
        "logger",
        "loggerContext",
        "level",
        "timeStamp",
        "marker",
        "mdc",
        "throwableProxy",
        "throwable",
    };

    @SuppressWarnings("rawtypes")
    private static final Class<?>[] PARAMETER_TYPES = {
        int.class,
        int.class,
        int.class,
        int.class,
        ILoggingEvent.class,
        String.class,
        String.class,
        String.class,
        LoggerContextVO.class,
        int.class,
        long.class,
        Marker.class,
        Map.class,
        IThrowableProxy.class,
        Throwable.class,
    };

    private static final int ERROR_THRESHOLD = 4;

    private String expression;
    private ScriptEvaluator scriptEvaluator;
    private int errorCount;

    @Override
    public void start()
    {
        try
        {
            String decoratedExpression = this.getDecoratedExpression();
            this.scriptEvaluator = new ScriptEvaluator(
                decoratedExpression,
                boolean.class,
                PARAMETER_NAMES,
                PARAMETER_TYPES,
                THROWN_EXCEPTIONS
            );
            super.start();
        }
        catch (Exception e)
        {
            this.addError("Could not start evaluator with expression [" + this.expression + "]", e);
        }
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws EvaluationException
    {
        if (!this.isStarted())
        {
            throw new IllegalStateException("Evaluator [" + this.getName() + "] was called in stopped state");
        }
        try
        {
            Boolean result = (Boolean) this.scriptEvaluator.evaluate(this.getParameterValues(event));
            return result;
        }
        catch (Exception ex)
        {
            this.errorCount++;
            if (this.errorCount >= ERROR_THRESHOLD)
            {
                this.stop();
            }
            throw new EvaluationException("Evaluator [" + this.getName() + "] caused an exception", ex);
        }
    }

    private String getDecoratedExpression()
    {
        String expr = this.expression;
        if (!expr.contains("return"))
        {
            expr = "return " + expr + ";";
            this.addInfo(
                "Adding [return] prefix and a semicolon suffix. Expression becomes [" + expr + "]"
            );
            this.addInfo("See also " + CoreConstants.CODES_URL + "#block");
        }
        return IMPORT_LEVEL + expr;
    }

    private Object[] getParameterValues(ILoggingEvent loggingEvent)
    {
        IThrowableProxy iThrowableProxy = loggingEvent.getThrowableProxy();
        Throwable throwable = null;
        if (iThrowableProxy instanceof ThrowableProxy throwableProxy)
        {
            throwable = throwableProxy.getThrowable();
        }

        return new Object[] {
            Level.DEBUG_INTEGER,
            Level.INFO_INTEGER,
            Level.WARN_INTEGER,
            Level.ERROR_INTEGER,
            loggingEvent,
            loggingEvent.getMessage(),
            loggingEvent.getFormattedMessage(),
            loggingEvent.getLoggerName(),
            loggingEvent.getLoggerContextVO(),
            loggingEvent.getLevel().toInteger(),
            loggingEvent.getTimeStamp(),
            loggingEvent.getMarker(),
            loggingEvent.getMDCPropertyMap(),
            iThrowableProxy,
            throwable,
        };
    }

    public String getExpression()
    {
        return this.expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }
}
