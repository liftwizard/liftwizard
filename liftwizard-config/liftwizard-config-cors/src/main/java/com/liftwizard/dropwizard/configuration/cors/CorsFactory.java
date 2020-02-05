package com.liftwizard.dropwizard.configuration.cors;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorsFactory
{
    // Default to disabled since default settings are dangerous
    private                 boolean      enabled;
    private @Valid @NotNull String       filterName       = "CORS";
    // allowedOrigins = "*" is convenient during development but must be changed in production
    private @Valid @NotNull String       allowedOrigins   = "*";
    private @Valid @NotNull String       allowedHeaders   = "X-Requested-With,Content-Type,Accept,Origin,Authorization";
    private @Valid @NotNull String       allowedMethods   = "OPTIONS,GET,PUT,POST,DELETE,HEAD";
    private @Valid @NotNull String       allowCredentials = "true";
    private @Valid @NotNull List<String> urlPatterns      = Arrays.asList("/*");

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getFilterName()
    {
        return this.filterName;
    }

    @JsonProperty
    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getAllowedOrigins()
    {
        return this.allowedOrigins;
    }

    @JsonProperty
    public void setAllowedOrigins(String allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedHeaders()
    {
        return this.allowedHeaders;
    }

    @JsonProperty
    public void setAllowedHeaders(String allowedHeaders)
    {
        this.allowedHeaders = allowedHeaders;
    }

    public String getAllowedMethods()
    {
        return this.allowedMethods;
    }

    @JsonProperty
    public void setAllowedMethods(String allowedMethods)
    {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowCredentials()
    {
        return this.allowCredentials;
    }

    @JsonProperty
    public void setAllowCredentials(String allowCredentials)
    {
        this.allowCredentials = allowCredentials;
    }

    public List<String> getUrlPatterns()
    {
        return this.urlPatterns;
    }

    @JsonProperty
    public void setUrlPatterns(List<String> urlPatterns)
    {
        this.urlPatterns = urlPatterns;
    }
}
