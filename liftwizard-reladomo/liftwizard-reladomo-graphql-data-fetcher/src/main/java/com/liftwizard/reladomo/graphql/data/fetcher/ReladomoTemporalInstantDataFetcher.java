package com.liftwizard.reladomo.graphql.data.fetcher;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoTemporalInstantDataFetcher<Input> implements DataFetcher<Instant>
{
    private final TimestampAttribute<Input> timestampAttribute;

    public ReladomoTemporalInstantDataFetcher(TimestampAttribute<Input> timestampAttribute)
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

        Timestamp result   = this.timestampAttribute.valueOf(persistentInstance);
        Timestamp infinity = this.timestampAttribute.getAsOfAttributeInfinity();
        if (infinity.equals(result))
        {
            return null;
        }

        // TODO: Consider handling here the case where validTo == systemTo + 1 day, but really means infinity
        // TODO: Alternately, just enable future dated rows to turn off this optimization
        return result.toInstant();
    }
}
