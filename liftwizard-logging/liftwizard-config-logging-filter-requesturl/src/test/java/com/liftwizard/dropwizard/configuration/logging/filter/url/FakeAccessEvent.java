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

package com.liftwizard.dropwizard.configuration.logging.filter.url;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;

public class FakeAccessEvent implements IAccessEvent
{
    private final String requestURL;

    public FakeAccessEvent(String requestURL)
    {
        this.requestURL = Objects.requireNonNull(requestURL);
    }

    @Override
    public String getRequestURL()
    {
        return this.requestURL;
    }

    @Override
    public HttpServletRequest getRequest()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequest() not implemented yet");
    }

    @Override
    public HttpServletResponse getResponse()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getResponse() not implemented yet");
    }

    @Override
    public long getTimeStamp()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getTimeStamp() not implemented yet");
    }

    @Override
    public long getElapsedTime()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getElapsedTime() not implemented yet");
    }

    @Override
    public long getElapsedSeconds()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getElapsedSeconds() not implemented yet");
    }

    @Override
    public String getRequestURI()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestURI() not implemented yet");
    }

    @Override
    public String getRemoteHost()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRemoteHost() not implemented yet");
    }

    @Override
    public String getRemoteUser()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRemoteUser() not implemented yet");
    }

    @Override
    public String getProtocol()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getProtocol() not implemented yet");
    }

    @Override
    public String getMethod()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getMethod() not implemented yet");
    }

    @Override
    public String getServerName()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getServerName() not implemented yet");
    }

    @Override
    public String getSessionID()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getSessionID() not implemented yet");
    }

    @Override
    public void setThreadName(String threadName)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".setThreadName() not implemented yet");
    }

    @Override
    public String getThreadName()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getThreadName() not implemented yet");
    }

    @Override
    public String getQueryString()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getQueryString() not implemented yet");
    }

    @Override
    public String getRemoteAddr()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRemoteAddr() not implemented yet");
    }

    @Override
    public String getRequestHeader(String key)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestHeader() not implemented yet");
    }

    @Override
    public Enumeration<String> getRequestHeaderNames()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestHeaderNames() not implemented yet");
    }

    @Override
    public Map<String, String> getRequestHeaderMap()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestHeaderMap() not implemented yet");
    }

    @Override
    public Map<String, String[]> getRequestParameterMap()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestParameterMap() not implemented yet");
    }

    @Override
    public String getAttribute(String key)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getAttribute() not implemented yet");
    }

    @Override
    public String[] getRequestParameter(String key)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestParameter() not implemented yet");
    }

    @Override
    public String getCookie(String key)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getCookie() not implemented yet");
    }

    @Override
    public long getContentLength()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getContentLength() not implemented yet");
    }

    @Override
    public int getStatusCode()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getStatusCode() not implemented yet");
    }

    @Override
    public String getRequestContent()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getRequestContent() not implemented yet");
    }

    @Override
    public String getResponseContent()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getResponseContent() not implemented yet");
    }

    @Override
    public int getLocalPort()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getLocalPort() not implemented yet");
    }

    @Override
    public ServerAdapter getServerAdapter()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getServerAdapter() not implemented yet");
    }

    @Override
    public String getResponseHeader(String key)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getResponseHeader() not implemented yet");
    }

    @Override
    public Map<String, String> getResponseHeaderMap()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getResponseHeaderMap() not implemented yet");
    }

    @Override
    public List<String> getResponseHeaderNameList()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".getResponseHeaderNameList() not implemented yet");
    }

    @Override
    public void prepareForDeferredProcessing()
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".prepareForDeferredProcessing() not implemented yet");
    }
}
