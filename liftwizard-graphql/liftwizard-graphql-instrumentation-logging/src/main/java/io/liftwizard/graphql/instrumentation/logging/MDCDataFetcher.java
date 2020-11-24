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

package io.liftwizard.graphql.instrumentation.logging;

import java.util.Objects;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.MDC;

public class MDCDataFetcher<T>
        implements DataFetcher<T>
{
    private final DataFetcher<T> dataFetcher;

    public MDCDataFetcher(DataFetcher<T> dataFetcher)
    {
        this.dataFetcher = Objects.requireNonNull(dataFetcher);
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception
    {
        try (
                var ignored = MDC.putCloseable(
                        "liftwizard.graphql.fetcher.type",
                        this.dataFetcher.getClass().getCanonicalName()))
        {
            return this.dataFetcher.get(environment);
        }
    }
}
