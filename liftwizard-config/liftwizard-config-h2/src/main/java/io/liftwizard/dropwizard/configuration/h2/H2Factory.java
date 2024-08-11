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

package io.liftwizard.dropwizard.configuration.h2;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

public class H2Factory
{
    // Should usually be disabled in production
    private boolean enabled;
    private @NotNull String servletName = "H2Console";
    private @NotNull String servletUrlMapping = "/h2-console/*";
    private @NotNull String propertiesLocation = "src/main/resources/";
    private @NotNull List<String> tcpServerArgs = Arrays.asList(
            "-tcp",
            "-tcpAllowOthers",
            "-tcpDaemon",
            "-web",
            "-webAllowOthers",
            "-webDaemon",
            // https://stackoverflow.com/a/55368174
            "-ifNotExists",
            "-baseDir",
            "./target/h2db");
    // 8082
    private Integer webPort;
    // 9092
    private Integer tcpPort;

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getServletName()
    {
        return this.servletName;
    }

    @JsonProperty
    public void setServletName(String servletName)
    {
        this.servletName = servletName;
    }

    public String getServletUrlMapping()
    {
        return this.servletUrlMapping;
    }

    @JsonProperty
    public void setServletUrlMapping(String servletUrlMapping)
    {
        this.servletUrlMapping = servletUrlMapping;
    }

    public String getPropertiesLocation()
    {
        return this.propertiesLocation;
    }

    @JsonProperty
    public void setPropertiesLocation(String propertiesLocation)
    {
        this.propertiesLocation = propertiesLocation;
    }

    public List<String> getTcpServerArgs()
    {
        return this.tcpServerArgs;
    }

    @JsonProperty
    public void setTcpServerArgs(List<String> tcpServerArgs)
    {
        this.tcpServerArgs = tcpServerArgs;
    }

    public Integer getWebPort()
    {
        return this.webPort;
    }

    @JsonProperty
    public void setWebPort(Integer webPort)
    {
        this.webPort = webPort;
    }

    public Integer getTcpPort()
    {
        return this.tcpPort;
    }

    @JsonProperty
    public void setTcpPort(Integer tcpPort)
    {
        this.tcpPort = tcpPort;
    }

    @ValidationMethod(message = "webPort and tcpPort must be non-null if enabled is true")
    public boolean isWebPortAndTcpPortDifferent()
    {
        return !this.enabled || this.webPort != null && this.tcpPort != null;
    }
}
