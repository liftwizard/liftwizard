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

package io.liftwizard.reladomo.graphql.deep.fetcher;

import java.util.Objects;
import java.util.function.Function;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.finder.DomainList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class GraphQLPropertyDataDeepFetcher<Output>
        implements DataFetcher<DomainList<Output>>
{
    private final Function<Object, DomainList<Output>> function;
    private final RelatedFinder<Output> finderInstance;

    public <Input> GraphQLPropertyDataDeepFetcher(
            Function<Input, DomainList<Output>> function,
            RelatedFinder<Output> finderInstance)
    {
        this.function = (Function<Object, DomainList<Output>>) Objects.requireNonNull(function);
        this.finderInstance = Objects.requireNonNull(finderInstance);
    }

    @Override
    public DomainList<Output> get(DataFetchingEnvironment environment)
    {
        Object source = environment.getSource();
        if (source == null)
        {
            return null;
        }

        DomainList<Output> domainList = this.function.apply(source);
        GraphQLDeepFetcher.deepFetch(domainList, this.finderInstance, environment.getSelectionSet());
        return domainList;
    }
}
