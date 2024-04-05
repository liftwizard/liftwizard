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

package io.liftwizard.graphql.exception;

import java.util.List;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.eclipse.collections.api.list.ImmutableList;

public class LiftwizardGraphQLException
        extends RuntimeException
        implements GraphQLError
{
    public LiftwizardGraphQLException(String message, ImmutableList<String> context, RuntimeException e)
    {
        super("%s in %s".formatted(message, context.makeString(".")), e);
    }

    @Override
    public List<SourceLocation> getLocations()
    {
        return null;
    }

    @Override
    public ErrorClassification getErrorType()
    {
        return ErrorType.DataFetchingException;
    }
}
