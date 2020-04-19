package com.liftwizard.reladomo.graphql.data.fetcher;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ReladomoTemporalRangeDataFetcher<Input> implements DataFetcher<Instant>
{
    private final AsOfAttribute<Input> asOfAttribute;

    public ReladomoTemporalRangeDataFetcher(AsOfAttribute<Input> asOfAttribute)
    {
        this.asOfAttribute = asOfAttribute;
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

        if (this.asOfAttribute.isAttributeNull(persistentInstance))
        {
            return null;
        }

        Timestamp result   = this.asOfAttribute.valueOf(persistentInstance);
        Timestamp infinity = this.asOfAttribute.getInfinityDate();
        if (infinity.equals(result))
        {
            return null;
        }

        // TODO: Consider handling here the case where validTo == systemTo + 1 day, but really means infinity
        // TODO: Alternately, just enable future dated rows to turn off this optimization
        return result.toInstant();
    }
}
