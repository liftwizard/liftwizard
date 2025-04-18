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

package io.liftwizard.servlet.logging.typesafe;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredArgumentsRequestHttp extends StructuredArgumentsHttp {

    private String method;
    private String contextPath;
    private String remoteUser;
    private String authenticationScheme;
    private Map<String, Object> principal;
    private StructuredArgumentsPath path;
    private StructuredArgumentsParameters parameters;
    private StructuredArgumentsClient client;
    private StructuredArgumentsServer server;

    @JsonProperty
    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        if (this.method != null) {
            throw new AssertionError(this.method);
        }
        this.method = Objects.requireNonNull(method);
    }

    @JsonProperty
    public String getContextPath() {
        return this.contextPath;
    }

    public void setContextPath(String contextPath) {
        if (this.contextPath != null) {
            throw new AssertionError(this.contextPath);
        }
        if (!contextPath.isEmpty()) {
            this.contextPath = contextPath;
        }
    }

    @JsonProperty
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        if (this.remoteUser != null) {
            throw new AssertionError(this.remoteUser);
        }
        this.remoteUser = remoteUser;
    }

    @JsonProperty
    public Map<String, Object> getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(Map<String, Object> principal) {
        if (this.principal != null) {
            throw new AssertionError(this.principal);
        }
        this.principal = Objects.requireNonNull(principal);
    }

    @JsonProperty
    public String getAuthenticationScheme() {
        return this.authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        if (this.authenticationScheme != null) {
            throw new AssertionError(this.authenticationScheme);
        }
        this.authenticationScheme = authenticationScheme;
    }

    @JsonProperty
    public StructuredArgumentsPath getPath() {
        return this.path;
    }

    public void setPath(StructuredArgumentsPath path) {
        if (this.path != null) {
            throw new AssertionError(this.path);
        }
        this.path = Objects.requireNonNull(path);
    }

    @JsonProperty
    public StructuredArgumentsParameters getParameters() {
        return this.parameters;
    }

    public void setParameters(StructuredArgumentsParameters parameters) {
        if (this.parameters != null) {
            throw new AssertionError(this.parameters);
        }
        this.parameters = Objects.requireNonNull(parameters);
    }

    @JsonProperty
    public StructuredArgumentsClient getClient() {
        return this.client;
    }

    public void setClient(StructuredArgumentsClient client) {
        if (this.client != null) {
            throw new AssertionError(this.client);
        }
        this.client = Objects.requireNonNull(client);
    }

    @JsonProperty
    public StructuredArgumentsServer getServer() {
        return this.server;
    }

    public void setServer(StructuredArgumentsServer server) {
        if (this.server != null) {
            throw new AssertionError(this.server);
        }
        this.server = Objects.requireNonNull(server);
    }
}
