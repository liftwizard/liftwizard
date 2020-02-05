package com.liftwizard.reladomo.graphql.data.fetcher;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoInstantDataFetcher<Input> implements DataFetcher<Instant>
{
    private final TimestampAttribute<Input> timestampAttribute;

    public ReladomoInstantDataFetcher(TimestampAttribute<Input> timestampAttribute)
    {
        this.timestampAttribute = timestampAttribute;
    }

    @Nullable
    @Override
    public Instant get(@Nonnull DataFetchingEnvironment environment)
    {
        Input persistentInstance = environment.getSource();
        if (persistentInstance == null)
        {
            return null;
        }

        if (this.timestampAttribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        Timestamp     result        = this.timestampAttribute.valueOf(persistentInstance);
        LocalDateTime localDateTime = result.toLocalDateTime();
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
