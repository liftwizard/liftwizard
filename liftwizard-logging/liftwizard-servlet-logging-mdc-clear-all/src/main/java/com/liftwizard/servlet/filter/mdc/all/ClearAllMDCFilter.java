package com.liftwizard.servlet.filter.mdc.all;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

// Priority must be less than the priority of StructuredArgumentLoggingFilter
@Provider
@Priority(Priorities.USER - 60)
public class ClearAllMDCFilter implements Filter
{
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
            MDC.clear();
        }
    }
}
