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

package io.liftwizard.reladomo.graphql.data.fetcher;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoInstantDataFetcher<Input>
        implements TrivialDataFetcher<Instant>
{
    private final TimestampAttribute<Input> timestampAttribute;

    public ReladomoInstantDataFetcher(TimestampAttribute<Input> timestampAttribute)
    {
        this.timestampAttribute = timestampAttribute;
    }

    @Nullable
    @Override
    public Instant get(@Nonnull DataFetchingEnvironment environment)
    {
        Input persistentInstance = environment.getSource();
        if (persistentInstance == null)
        {
            return null;
        }

        if (this.timestampAttribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        Timestamp     result        = this.timestampAttribute.valueOf(persistentInstance);
        Instant       instant       = result.toInstant();
        return instant;
    }
}
