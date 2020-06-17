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

package io.liftwizard.reladomo.test.resource.writer;

import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;

public class FrozenReladomoTestResourceColumn
{
    private final String                paddedHeader;
    private final ImmutableList<String> paddedValueStrings;

    public FrozenReladomoTestResourceColumn(String paddedHeader, ImmutableList<String> paddedValueStrings)
    {
        this.paddedHeader       = Objects.requireNonNull(paddedHeader);
        this.paddedValueStrings = Objects.requireNonNull(paddedValueStrings);

        paddedValueStrings.forEachWithIndex((string, index) ->
        {
            if (string.length() != paddedHeader.length())
            {
                throw new AssertionError(index + ": " + string);
            }
        });
    }

    public String getPaddedHeader()
    {
        return this.paddedHeader;
    }

    public ImmutableList<String> getPaddedValueStrings()
    {
        return this.paddedValueStrings;
    }
}
