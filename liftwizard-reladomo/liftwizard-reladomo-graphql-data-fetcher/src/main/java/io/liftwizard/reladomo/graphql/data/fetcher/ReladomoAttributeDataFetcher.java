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

import com.gs.fw.common.mithra.attribute.Attribute;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoAttributeDataFetcher<Input, T>
        implements TrivialDataFetcher<T>
{
    private final Attribute<Input, T> attribute;

    public ReladomoAttributeDataFetcher(Attribute<Input, T> attribute)
    {
        this.attribute = Objects.requireNonNull(attribute);
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

        if (this.attribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        String fullyQualifiedClassName = this.attribute.zGetTopOwnerClassName().replaceAll("/", ".");
        String canonicalName = persistentInstance.getClass().getCanonicalName();
        if (!fullyQualifiedClassName.equals(canonicalName))
        {
            String message = "Expected " + fullyQualifiedClassName + " but got " + canonicalName;
            throw new AssertionError(message);
        }

        return this.attribute.valueOf(persistentInstance);
    }
}
