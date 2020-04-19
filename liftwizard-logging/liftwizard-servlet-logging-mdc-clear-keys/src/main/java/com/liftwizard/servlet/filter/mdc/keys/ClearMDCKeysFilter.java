package com.liftwizard.servlet.filter.mdc.keys;

import java.io.IOException;
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

import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.MDC;

// Priority must be less than the priority of StructuredArgumentLoggingFilter
@Provider
@Priority(Priorities.USER - 50)
public class ClearMDCKeysFilter implements Filter
{
    private final ImmutableList<String> mdcKeys;

    public ClearMDCKeysFilter(ImmutableList<String> mdcKeys)
    {
        this.mdcKeys = Objects.requireNonNull(mdcKeys);
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            for (String mdcKey : this.mdcKeys)
            {
                MDC.remove(mdcKey);
            }
        }
    }
}
