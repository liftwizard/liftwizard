package com.liftwizard.reladomo.timestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.annotation.Nonnull;

public final class ReladomoTimestampConverter
{
    private ReladomoTimestampConverter()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    @Nonnull
    public static Timestamp fromInstant(@Nonnull Instant instant)
    {
        return Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }
}
