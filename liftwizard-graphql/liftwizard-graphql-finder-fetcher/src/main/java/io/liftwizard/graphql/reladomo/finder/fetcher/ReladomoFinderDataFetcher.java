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

package io.liftwizard.graphql.reladomo.finder.fetcher;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.finder.DomainList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.liftwizard.graphql.exception.LiftwizardGraphQLException;
import io.liftwizard.reladomo.graphql.deep.fetcher.GraphQLDeepFetcher;
import io.liftwizard.reladomo.graphql.operation.GraphQLQueryToOperationConverter;
import io.liftwizard.reladomo.graphql.operation.LiftwizardGraphQLContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReladomoFinderDataFetcher<T>
        implements DataFetcher<List<T>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoFinderDataFetcher.class);

    private final RelatedFinder<T> finder;

    public ReladomoFinderDataFetcher(RelatedFinder<T> finder)
    {
        this.finder = Objects.requireNonNull(finder);
    }

    @Override
    public List<T> get(DataFetchingEnvironment environment)
    {
        Map<String, Object> arguments      = environment.getArguments();
        Object              inputOperation = arguments.get("operation");
        Operation           operation      = this.getOperation((Map<?, ?>) inputOperation);
        LOGGER.debug("Executing operation: {}", operation);
        DomainList<T> result = (DomainList<T>) this.finder.findMany(operation);
        GraphQLDeepFetcher.deepFetch(result, this.finder, environment.getSelectionSet());
        return result;
    }

    public Operation getOperation(Map<?, ?> inputOperation)
    {
        try
        {
            var converter = new GraphQLQueryToOperationConverter();
            return converter.convert(this.finder, inputOperation);
        }
        catch (LiftwizardGraphQLContextException e)
        {
            throw new LiftwizardGraphQLException(e.getMessage(), e.getContext());
        }
    }
}
