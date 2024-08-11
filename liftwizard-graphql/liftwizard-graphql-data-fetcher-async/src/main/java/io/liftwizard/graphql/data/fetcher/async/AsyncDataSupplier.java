/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.graphql.data.fetcher.async;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.MDC;

public class AsyncDataSupplier<T>
        implements Supplier<T>
{
    private final DataFetcher<T> dataFetcher;
    private final DataFetchingEnvironment environment;
    private final Map<String, String> copyOfContextMap;

    AsyncDataSupplier(
            DataFetcher<T> dataFetcher,
            DataFetchingEnvironment environment)
    {
        this.dataFetcher = Objects.requireNonNull(dataFetcher);
        this.environment = Objects.requireNonNull(environment);
        this.copyOfContextMap = AsyncDataSupplier.getCopyOfContextMap();
    }

    @Override
    public T get()
    {
        Map<String, String> oldContextMap = AsyncDataSupplier.getCopyOfContextMap();
        MDC.setContextMap(this.copyOfContextMap);
        try
        {
            return this.dataFetcher.get(this.environment);
        }
        catch (Exception e)
        {
            if (e instanceof RuntimeException runtimeException)
            {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        }
        finally
        {
            MDC.setContextMap(oldContextMap);
        }
    }

    private static Map<String, String> getCopyOfContextMap()
    {
        Map<String, String> result = MDC.getCopyOfContextMap();
        return result == null ? Map.of() : result;
    }
}
