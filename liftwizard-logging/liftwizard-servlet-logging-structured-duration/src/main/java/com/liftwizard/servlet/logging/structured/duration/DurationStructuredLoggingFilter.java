package com.liftwizard.servlet.logging.structured.duration;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

// Priority must be less than the priority of StructuredArgumentLoggingFilter
@Provider
@Priority(Priorities.USER - 30)
public class DurationStructuredLoggingFilter implements Filter
{
    public static final String STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME = "structuredArguments";
    public static final String START_TIME_KEY                      = "liftwizard.time.startTime";

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d[HMS])(?!$)");

    private final Clock  clock;
    private final String structuredArgumentsAttributeName;
    private final String startTimeKey;

    public DurationStructuredLoggingFilter(Clock clock)
    {
        this(clock, STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME, START_TIME_KEY);
    }

    public DurationStructuredLoggingFilter(Clock clock, String structuredArgumentsAttributeName, String startTimeKey)
    {
        this.clock                            = Objects.requireNonNull(clock);
        this.structuredArgumentsAttributeName = Objects.requireNonNull(structuredArgumentsAttributeName);
        this.startTimeKey                     = Objects.requireNonNull(startTimeKey);
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            this.before(request);
            chain.doFilter(request, response);
        }
        finally
        {
            this.after(request);
        }
    }

    private void before(ServletRequest servletRequest)
    {
        Instant startTime = this.clock.instant();

        Object structuredArguments = servletRequest.getAttribute(this.structuredArgumentsAttributeName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        structuredArgumentsMap.put(this.startTimeKey, startTime);
    }

    private void after(ServletRequest servletRequest)
    {
        Object structuredArguments = servletRequest.getAttribute(this.structuredArgumentsAttributeName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        Instant  startTime      = (Instant) structuredArgumentsMap.get(this.startTimeKey);
        Instant  endTime        = this.clock.instant();
        Duration duration       = Duration.between(startTime, endTime);
        String   prettyDuration = DurationStructuredLoggingFilter.prettyPrintDuration(duration);

        structuredArgumentsMap.put("liftwizard.time.endTime", endTime);
        structuredArgumentsMap.put("liftwizard.time.duration.pretty", prettyDuration);
        structuredArgumentsMap.put("liftwizard.time.duration.ms", duration.toMillis());
        structuredArgumentsMap.put("liftwizard.time.duration.ns", duration.toNanos());
    }

    private static String prettyPrintDuration(Duration duration)
    {
        String  trimmedString = duration.toString().substring(2);
        Matcher matcher       = DURATION_PATTERN.matcher(trimmedString);
        return matcher.replaceAll("$1 ").toLowerCase();
    }
}
