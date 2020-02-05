package com.liftwizard.dropwizard.configuration.ddl.executor;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

public class DdlExecutorFactory
{
    private @Valid @NotNull String dataSourceName;
    private @Valid @NotNull String ddlLocationPattern = ".*\\.ddl";
    private @Valid @NotNull String idxLocationPattern = ".*\\.idx";

    @JsonProperty
    public String getDataSourceName()
    {
        return this.dataSourceName;
    }

    @JsonProperty
    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    @JsonProperty
    public String getDdlLocationPattern()
    {
        return this.ddlLocationPattern;
    }

    @JsonProperty
    public void setDdlLocationPattern(String ddlLocationPattern)
    {
        this.ddlLocationPattern = ddlLocationPattern;
    }

    @JsonProperty
    public String getIdxLocationPattern()
    {
        return this.idxLocationPattern;
    }

    @JsonProperty
    public void setIdxLocationPattern(String idxLocationPattern)
    {
        this.idxLocationPattern = idxLocationPattern;
    }

    @ValidationMethod(message = "ddlLocationPattern must be a valid regex")
    @JsonIgnore
    public boolean isDdlLocationPatternValid()
    {
        try
        {
            Pattern.compile(this.ddlLocationPattern);
            return true;
        }
        catch (PatternSyntaxException e)
        {
            return false;
        }
    }

    @ValidationMethod(message = "idxLocationPattern must be a valid regex")
    @JsonIgnore
    public boolean isIdxLocationPatternValid()
    {
        try
        {
            Pattern.compile(this.idxLocationPattern);
            return true;
        }
        catch (PatternSyntaxException e)
        {
            return false;
        }
    }
}
