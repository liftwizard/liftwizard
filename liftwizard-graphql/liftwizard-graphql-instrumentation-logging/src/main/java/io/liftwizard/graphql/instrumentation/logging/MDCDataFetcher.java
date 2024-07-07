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
import io.liftwizard.graphql.data.fetcher.async.LiftwizardAsyncDataFetcher;
import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;

public class MDCDataFetcher<T>
        implements DataFetcher<T>
{
    private final DataFetcher<T> dataFetcher;
    private final String         executionId;
    private final String         path;
    private final String         parentTypeName;
    private final String         fieldName;
    private final String         fieldTypeName;

    public MDCDataFetcher(
            DataFetcher<T> dataFetcher,
            String executionId,
            String path,
            String parentTypeName,
            String fieldName,
            String fieldTypeName)
    {
        this.dataFetcher    = Objects.requireNonNull(dataFetcher);
        this.executionId    = executionId;
        this.path           = path;
        this.parentTypeName = parentTypeName;
        this.fieldName      = fieldName;
        this.fieldTypeName  = fieldTypeName;
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception
    {
        String dataFetcherName = this.getDataFetcherName();

        try (MultiMDCCloseable mdc = new MultiMDCCloseable())
        {
            mdc.put("liftwizard.graphql.executionId", this.executionId);
            mdc.put("liftwizard.graphql.field.path", this.path);
            mdc.put("liftwizard.graphql.field.parentType", this.parentTypeName);
            mdc.put("liftwizard.graphql.field.name", this.fieldName);
            mdc.put("liftwizard.graphql.field.type", this.fieldTypeName);
            mdc.put("liftwizard.graphql.fetcher.type", dataFetcherName);
            return this.dataFetcher.get(environment);
        }
    }

    private String getDataFetcherName()
    {
        DataFetcher<T> wrappedDataFetcher = this.dataFetcher instanceof LiftwizardAsyncDataFetcher
                ? ((LiftwizardAsyncDataFetcher<T>) this.dataFetcher).getWrappedDataFetcher()
                : this.dataFetcher;
        String dataFetcherName = wrappedDataFetcher.getClass().getCanonicalName();
        return dataFetcherName;
    }
}
