package com.liftwizard.reladomo.graphql.data.fetcher;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.DateAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoLocalDateDataFetcher<Input> implements DataFetcher<LocalDate>
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

        java.sql.Date result = (java.sql.Date) this.dateAttribute.valueOf(persistentInstance);
        return result.toLocalDate();
    }
}
