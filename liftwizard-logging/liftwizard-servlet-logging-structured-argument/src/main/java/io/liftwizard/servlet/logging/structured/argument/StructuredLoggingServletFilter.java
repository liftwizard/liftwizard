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

package io.liftwizard.servlet.logging.structured.argument;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.USER - 40)
public class StructuredLoggingServletFilter
        implements Filter
{
    public static final String STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME = "structuredArguments";
    public static final String MDC_ATTRIBUTE_NAME                  = "mdc";

    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredLoggingServletFilter.class);

    private final String structuredArgumentsAttributeName;
    private final String mdcAttributeName;

    public StructuredLoggingServletFilter()
    {
        this(STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME, MDC_ATTRIBUTE_NAME);
    }

    public StructuredLoggingServletFilter(String structuredArgumentsAttributeName, String mdcAttributeName)
    {
        this.structuredArgumentsAttributeName = Objects.requireNonNull(structuredArgumentsAttributeName);
        this.mdcAttributeName                 = Objects.requireNonNull(mdcAttributeName);
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
            this.initialize(request);
            chain.doFilter(request, response);
        }
        finally
        {
            this.log(request);
        }
    }

    private void initialize(ServletRequest servletRequest)
    {
        servletRequest.setAttribute(this.structuredArgumentsAttributeName, new LinkedHashMap<>());
        servletRequest.setAttribute(this.mdcAttributeName, new MultiMDCCloseable());
    }

    private void log(ServletRequest servletRequest)
    {
        Object structuredArguments = servletRequest.getAttribute(this.structuredArgumentsAttributeName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        LOGGER.info(Markers.appendEntries(structuredArgumentsMap), "Completed response.");

        MultiMDCCloseable mdc = (MultiMDCCloseable) servletRequest.getAttribute(this.mdcAttributeName);
        mdc.close();
    }
}
