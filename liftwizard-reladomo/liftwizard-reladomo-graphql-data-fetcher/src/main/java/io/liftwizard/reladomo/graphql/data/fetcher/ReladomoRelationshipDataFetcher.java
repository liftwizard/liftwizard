/*
 * Copyright 2023 Craig Motlin
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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.finder.DeepRelationshipAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoRelationshipDataFetcher<Input, T>
        implements DataFetcher<T>
{
    private final DeepRelationshipAttribute<Input, T> relationshipAttribute;

    public ReladomoRelationshipDataFetcher(DeepRelationshipAttribute<Input, T> relationshipAttribute)
    {
        this.relationshipAttribute = Objects.requireNonNull(relationshipAttribute);
    }

    @Nullable
    @Override
    public T get(@Nonnull DataFetchingEnvironment environment)
    {
        Input persistentInstance = environment.getSource();
        if (persistentInstance == null)
        {
            return null;
        }

        return this.relationshipAttribute.valueOf(persistentInstance);
    }
}
