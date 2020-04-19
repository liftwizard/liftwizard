package com.liftwizard.dropwizard.configuration.h2;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class H2Factory
{
    // Should usually be disabled in production
    private          boolean      enabled;
    private @NotNull String       servletName        = "H2Console";
    private @NotNull String       servletUrlMapping  = "/h2-console/*";
    private @NotNull String       propertiesLocation = "src/main/resources/";
    private @NotNull List<String> tcpServerArgs      = Arrays.asList(
            "-tcp",
            "-tcpAllowOthers",
            "-tcpDaemon",
            "-web",
            "-webAllowOthers",
            "-webDaemon",
            // https://stackoverflow.com/a/55368174/23572
            "-ifNotExists",
            "-baseDir",
            "./target/h2db");

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
}
