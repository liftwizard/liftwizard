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

package io.liftwizard.reladomo.graphql.operation;

import org.eclipse.collections.api.list.ImmutableList;

public class LiftwizardGraphQLContextException extends RuntimeException
{
    private final ImmutableList<String> context;

    public LiftwizardGraphQLContextException(String message, ImmutableList<String> context)
    {
        super(message);
        this.context = context;
    }

    public ImmutableList<String> getContext()
    {
        return this.context;
    }
}
