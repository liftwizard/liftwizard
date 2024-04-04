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

import java.sql.Date;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.DateAttribute;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoLocalDateDataFetcher<Input>
        implements TrivialDataFetcher<LocalDate>
{
    private final DateAttribute<Input> dateAttribute;

    public ReladomoLocalDateDataFetcher(DateAttribute<Input> dateAttribute)
    {
        this.dateAttribute = dateAttribute;
    }

    @Nullable
    @Override
    public LocalDate get(@Nonnull DataFetchingEnvironment environment)
    {
        Input persistentInstance = environment.getSource();
        if (persistentInstance == null)
        {
            return null;
        }

        if (this.dateAttribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        Date result = (Date) this.dateAttribute.valueOf(persistentInstance);
        return result.toLocalDate();
    }
}
