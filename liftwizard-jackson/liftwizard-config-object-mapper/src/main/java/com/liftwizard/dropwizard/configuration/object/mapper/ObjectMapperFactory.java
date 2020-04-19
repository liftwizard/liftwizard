package com.liftwizard.dropwizard.configuration.object.mapper;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectMapperFactory
{
    private          boolean enabled                = true;
    // Should usually be false in production
    private          boolean prettyPrint            = true;
    private @NotNull Include serializationInclusion = Include.NON_ABSENT;

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isPrettyPrint()
    {
        return this.prettyPrint;
    }

    @JsonProperty
    public void setPrettyPrint(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    public Include getSerializationInclusion()
    {
        return this.serializationInclusion;
    }

    @JsonProperty
    public void setSerializationInclusion(Include serializationInclusion)
    {
        this.serializationInclusion = serializationInclusion;
    }
}
