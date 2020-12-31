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

package io.liftwizard.dropwizard.configuration.executor;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;

public class ExecutorServiceFactory
{
    @Valid
    @NotEmpty
    private String   nameFormat;
    @Min(0)
    private int      minThreads;
    @Min(1)
    private int      maxThreads    = 1;
    private boolean  allowCoreThreadTimeOut;
    @NotNull
    @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS, inclusive = false)
    private Duration keepAliveTime = Duration.seconds(60);
    @NotNull
    @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS, inclusive = false)
    private Duration shutdownTime  = Duration.seconds(5);

    // Method that must return true for the object to be valid
    @ValidationMethod(message = "maxThreads < minThreads")
    @JsonIgnore
    public boolean isValidPoolSize()
    {
        return this.maxThreads < this.minThreads;
    }

    @JsonIgnore
    public ExecutorServiceBuilder build(Environment environment)
    {
        return this.build(environment.lifecycle());
    }

    @JsonIgnore
    public ExecutorServiceBuilder build(LifecycleEnvironment environment)
    {
        return environment
                .executorService(this.nameFormat)
                .minThreads(this.minThreads)
                .maxThreads(this.maxThreads)
                .allowCoreThreadTimeOut(this.allowCoreThreadTimeOut)
                .keepAliveTime(this.keepAliveTime)
                .shutdownTime(this.shutdownTime);
    }

    @JsonProperty
    public String getNameFormat()
    {
        return this.nameFormat;
    }

    @JsonProperty
    public void setNameFormat(String nameFormat)
    {
        this.nameFormat = nameFormat;
    }

    @JsonProperty
    public int getMinThreads()
    {
        return this.minThreads;
    }

    @JsonProperty
    public void setMinThreads(int minThreads)
    {
        this.minThreads = minThreads;
    }

    @JsonProperty
    public int getMaxThreads()
    {
        return this.maxThreads;
    }

    @JsonProperty
    public void setMaxThreads(int maxThreads)
    {
        this.maxThreads = maxThreads;
    }

    @JsonProperty
    public boolean isAllowCoreThreadTimeOut()
    {
        return this.allowCoreThreadTimeOut;
    }

    @JsonProperty
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut)
    {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    @JsonProperty
    public Duration getKeepAliveTime()
    {
        return this.keepAliveTime;
    }

    @JsonProperty
    public void setKeepAliveTime(Duration keepAliveTime)
    {
        this.keepAliveTime = keepAliveTime;
    }

    @JsonProperty
    public Duration getShutdownTime()
    {
        return this.shutdownTime;
    }

    @JsonProperty
    public void setShutdownTime(Duration shutdownTime)
    {
        this.shutdownTime = shutdownTime;
    }
}
