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

package io.liftwizard.graphql.reladomo.operation.fetcher;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.finder.DomainList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.liftwizard.graphql.exception.LiftwizardGraphQLException;
import io.liftwizard.model.reladomo.operation.compiler.ReladomoOperationCompiler;
import io.liftwizard.reladomo.graphql.deep.fetcher.GraphQLDeepFetcher;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReladomoOperationDataFetcher<T>
        implements DataFetcher<List<T>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoOperationDataFetcher.class);

    private final RelatedFinder<T> finder;

    public ReladomoOperationDataFetcher(RelatedFinder<T> finder)
    {
        this.finder = Objects.requireNonNull(finder);
    }

    @Timed
    @ExceptionMetered
    @Override
    public List<T> get(DataFetchingEnvironment environment)
    {
        Map<String, Object> arguments      = environment.getArguments();
        String              inputOperation = (String) arguments.get("operation");
        Operation           operation      = this.compileOperation(this.finder, inputOperation);
        LOGGER.debug("Executing operation: {}", operation);
        DomainList<T> result = (DomainList<T>) this.finder.findMany(operation);
        GraphQLDeepFetcher.deepFetch(result, this.finder, environment.getSelectionSet());
        return result;
    }

    private Operation compileOperation(RelatedFinder<T> relatedFinder, String inputOperation)
    {
        try
        {
            var compiler = new ReladomoOperationCompiler();
            return compiler.compile(relatedFinder, inputOperation);
        }
        catch (RuntimeException e)
        {
            throw new LiftwizardGraphQLException(e.getMessage(), Lists.immutable.with(inputOperation));
        }
    }
}
