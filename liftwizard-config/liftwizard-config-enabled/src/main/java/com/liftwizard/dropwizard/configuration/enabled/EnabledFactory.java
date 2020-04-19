package com.liftwizard.dropwizard.configuration.enabled;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnabledFactory
{
    private boolean enabled;

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
