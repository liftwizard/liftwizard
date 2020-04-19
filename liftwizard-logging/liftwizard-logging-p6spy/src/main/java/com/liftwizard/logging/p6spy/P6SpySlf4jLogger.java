package com.liftwizard.logging.p6spy;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.FormattedLogger;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P6SpySlf4jLogger
        extends FormattedLogger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(P6SpySlf4jLogger.class);

    @Override
    public void logException(Exception e)
    {
        LOGGER.debug("", e);
    }

    @Override
    public void logText(String text)
    {
        LOGGER.debug(text);
    }

    @Override
    public void logSQL(
            int connectionId,
            String now,
            long elapsed,
            Category category,
            String prepared,
            String sql,
            String url)
    {
        long    epochMilli = Long.parseLong(now);
        Instant instant    = Instant.ofEpochMilli(epochMilli);

        Map<String, Object> structuredArgumentsMap = new LinkedHashMap<>();
        structuredArgumentsMap.put("liftwizard.p6spy.connectionId", connectionId);
        structuredArgumentsMap.put("liftwizard.p6spy.now", instant);
        structuredArgumentsMap.put("liftwizard.p6spy.elapsed", elapsed);
        structuredArgumentsMap.put("liftwizard.p6spy.category", category.getName());
        structuredArgumentsMap.put("liftwizard.p6spy.prepared", prepared);
        structuredArgumentsMap.put("liftwizard.p6spy.sql", sql);
        structuredArgumentsMap.put("liftwizard.p6spy.url", url);

        if (category.equals(Category.ERROR))
        {
            LOGGER.error(Markers.appendEntries(structuredArgumentsMap), sql);
        }
        else if (category.equals(Category.WARN))
        {
            LOGGER.warn(Markers.appendEntries(structuredArgumentsMap), sql);
        }
        else if (category.equals(Category.DEBUG))
        {
            LOGGER.debug(Markers.appendEntries(structuredArgumentsMap), sql);
        }
        else
        {
            LOGGER.info(Markers.appendEntries(structuredArgumentsMap), sql);
        }
    }

    @Override
    public boolean isCategoryEnabled(Category category)
    {
        if (category.equals(Category.ERROR))
        {
            return LOGGER.isErrorEnabled();
        }

        if (category.equals(Category.WARN))
        {
            return LOGGER.isWarnEnabled();
        }

        if (category.equals(Category.DEBUG))
        {
            return LOGGER.isDebugEnabled();
        }

        return LOGGER.isInfoEnabled();
    }
}
