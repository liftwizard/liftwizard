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

package com.liftwizard.reladomo.graphql.data.fetcher;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoTemporalRangeDataFetcher<Input> implements DataFetcher<Instant>
{
    private final AsOfAttribute<Input> asOfAttribute;

    public ReladomoTemporalRangeDataFetcher(AsOfAttribute<Input> asOfAttribute)
    {
        this.asOfAttribute = asOfAttribute;
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

        if (this.asOfAttribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        Timestamp result   = this.asOfAttribute.valueOf(persistentInstance);
        Timestamp infinity = this.asOfAttribute.getInfinityDate();
        if (infinity.equals(result))
        {
            return null;
        }

        // TODO: Consider handling here the case where validTo == systemTo + 1 day, but really means infinity
        // TODO: Alternately, just enable future dated rows to turn off this optimization
        return result.toInstant();
    }
}
