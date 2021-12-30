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

package io.liftwizard.servlet.logging.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsClient;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsPath;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequestHttp;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsResponseHttp;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsServer;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.map.mutable.MapAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class ServerLoggingFilter
        implements Filter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLoggingFilter.class);

    private final LoggingConfig                 loggingConfig;
    private final Consumer<StructuredArguments> structuredLogger;

    public ServerLoggingFilter(
            LoggingConfig loggingConfig,
            Consumer<StructuredArguments> structuredLogger)
    {
        this.loggingConfig    = Objects.requireNonNull(loggingConfig);
        this.structuredLogger = Objects.requireNonNull(structuredLogger);
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
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
        {
            chain.doFilter(request, response);
            return;
        }

        StructuredArguments structuredArguments = new StructuredArguments();
        request.setAttribute("structuredArguments", structuredArguments);

        HttpServletRequest  httpServletRequest  = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        this.addInitialRequestAttributes(structuredArguments, httpServletRequest);

        var requestWrapper  = new ContentCachingRequestWrapper(httpServletRequest);
        var responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
        try
        {
            chain.doFilter(requestWrapper, responseWrapper);
        }
        finally
        {
            this.addFinalRequestAttributes(structuredArguments, requestWrapper);
            this.addFinalResponseAttributes(structuredArguments, responseWrapper, httpServletResponse);
            this.structuredLogger.accept(structuredArguments);
        }
    }

    private void addInitialRequestAttributes(
            StructuredArguments structuredArguments,
            HttpServletRequest httpServletRequest)
    {
        String authType = httpServletRequest.getAuthType();
        if (authType != null)
        {
            LOGGER.info(authType);
        }

        String contextPath = httpServletRequest.getContextPath();
        if (!contextPath.isEmpty())
        {
            LOGGER.info(contextPath);
        }

        StructuredArgumentsRequestHttp http   = structuredArguments.getRequest().getHttp();
        String                         method = httpServletRequest.getMethod();
        http.setMethod(method);

        String pathTranslated = httpServletRequest.getPathTranslated();
        if (pathTranslated != null)
        {
            LOGGER.info(pathTranslated);
        }
        String queryString = httpServletRequest.getQueryString();
        if (queryString != null)
        {
            LOGGER.info(queryString);
        }
        String remoteUser = httpServletRequest.getRemoteUser();
        if (remoteUser != null)
        {
            LOGGER.info(remoteUser);
        }
        String requestedSessionId = httpServletRequest.getRequestedSessionId();
        if (requestedSessionId != null)
        {
            LOGGER.info(requestedSessionId);
        }
        Principal userPrincipal = httpServletRequest.getUserPrincipal();
        if (userPrincipal != null)
        {
            LOGGER.info(userPrincipal.getName());
        }

        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
        if (parameterNames.hasMoreElements())
        {
            LOGGER.info(parameterNames.toString());
        }

        StructuredArgumentsPath path = new StructuredArgumentsPath(
                httpServletRequest.getRequestURL().toString(),
                httpServletRequest.getRequestURI());
        http.setPath(path);

        StructuredArgumentsClient client = new StructuredArgumentsClient(
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getRemoteHost(),
                httpServletRequest.getRemotePort());
        http.setClient(client);
        StructuredArgumentsServer server = new StructuredArgumentsServer(
                httpServletRequest.getScheme(),
                httpServletRequest.getServerName(),
                httpServletRequest.getServerPort());
        http.setServer(server);

        MutableMap<String, String> newHeaders = MapAdapter.adapt(new LinkedHashMap<>());
        MutableList<String> newExcludedHeaders = this.loggingConfig.isLogRequestHeaderNames()
                ? Lists.mutable.empty()
                : null;

        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        headerNames.asIterator().forEachRemaining(headerName ->
        {
            String headerValue = httpServletRequest.getHeader(headerName);

            if (this.loggingConfig.getIncludedRequestHeaders().contains(headerName))
            {
                newHeaders.put(headerName, headerValue);
            }
            else if (this.loggingConfig.isLogRequestHeaderNames())
            {
                newExcludedHeaders.add(headerName);
            }
        });

        if (this.loggingConfig.isLogRequestHeaderNames())
        {
            http.setHeaders(newHeaders);
        }
        if (this.loggingConfig.isLogRequestHeaderNames())
        {
            http.setExcludedHeaders(newExcludedHeaders.toImmutable());
        }
    }

    private void addFinalRequestAttributes(
            StructuredArguments structuredArguments,
            ContentCachingRequestWrapper requestWrapper)
    {
        if (!this.loggingConfig.isLogRequestBodies()
                || requestWrapper == null
                || requestWrapper.getContentAsByteArray().length <= 0)
        {
            return;
        }

        String payload = this.getPayloadFromByteArray(
                requestWrapper.getContentAsByteArray(),
                requestWrapper.getCharacterEncoding());

        String truncatedPayload = this.getTruncatedPayload(payload);
        structuredArguments.getRequest().getHttp().setBody(truncatedPayload);
    }

    private void addFinalResponseAttributes(
            StructuredArguments structuredArguments,
            @Nonnull ContentCachingResponseWrapper responseWrapper,
            HttpServletResponse httpServletResponse)
            throws IOException
    {
        StructuredArgumentsResponseHttp http = structuredArguments.getResponse().getHttp();

        String contentType = httpServletResponse.getContentType();
        if (contentType != null)
        {
            http.setContentType(contentType);
        }

        http.getStatus().setCode(httpServletResponse.getStatus());

        this.addResponseHeaders(httpServletResponse, http);

        if (this.loggingConfig.isLogResponseBodies() && responseWrapper.getContentAsByteArray().length > 0)
        {
            String payload = this.getPayloadFromByteArray(
                    responseWrapper.getContentAsByteArray(),
                    responseWrapper.getCharacterEncoding());

            String truncatedPayload = this.getTruncatedPayload(payload);
            http.setBody(truncatedPayload);
        }

        responseWrapper.copyBodyToResponse();
    }

    @Nonnull
    private String getTruncatedPayload(String payload)
    {
        int maxEntitySize = this.loggingConfig.getMaxEntitySize();
        if (payload.length() <= maxEntitySize)
        {
            return payload;
        }

        return payload.substring(0, maxEntitySize) + "...more...";
    }

    private void addResponseHeaders(HttpServletResponse httpServletResponse, StructuredArgumentsResponseHttp http)
    {
        MutableMap<String, String> newHeaders = MapAdapter.adapt(new LinkedHashMap<>());
        MutableList<String> newExcludedHeaders = this.loggingConfig.isLogResponseHeaderNames()
                ? Lists.mutable.empty()
                : null;

        for (String headerName : httpServletResponse.getHeaderNames())
        {
            String headerValue = httpServletResponse.getHeader(headerName);

            if (this.loggingConfig.getIncludedResponseHeaders().contains(headerName))
            {
                newHeaders.put(headerName, headerValue);
            }
            else if (this.loggingConfig.isLogResponseHeaderNames())
            {
                newExcludedHeaders.add(headerName);
            }
        }

        if (this.loggingConfig.isLogResponseHeaderNames())
        {
            http.setHeaders(newHeaders);
        }
        if (this.loggingConfig.isLogResponseHeaderNames())
        {
            http.setExcludedHeaders(newExcludedHeaders.toImmutable());
        }
    }

    private String getPayloadFromByteArray(byte[] requestBuffer, String charEncoding)
    {
        try
        {
            return new String(requestBuffer, charEncoding);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException)
        {
            return "Unsupported-Encoding";
        }
    }
}
