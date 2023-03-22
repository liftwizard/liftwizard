/*
 * Copyright 2022 Craig Motlin
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

package io.liftwizard.graphql.scalar.temporal;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import graphql.Internal;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Derived from https://github.com/graphql-java/graphql-java-extended-scalars but for Instant instead of OffsetDateTime.
 */
@Internal
public class GraphQLTemporalScalar extends GraphQLScalarType
{
    public GraphQLTemporalScalar(@Nonnull String name)
    {
        super(name, "An RFC-3339 compliant " + name + " Scalar", new InstantCoercing());
    }

    private static class InstantCoercing implements Coercing<Instant, String>
    {
        @Nonnull
        private static String typeName(@Nullable Object input)
        {
            if (input == null)
            {
                return "null";
            }
            return input.getClass().getSimpleName();
        }

        @Nonnull
        @Override
        public String serialize(Object input)
        {
            Instant instant = InstantCoercing.getInstant(input);
            try
            {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        .withZone(ZoneOffset.UTC)
                        .format(instant);
            }
            catch (DateTimeException e)
            {
                throw new CoercingSerializeException(
                        "Unable to turn TemporalAccessor into OffsetDateTime because of : '" + e.getMessage() + "'.", e);
            }
        }

        private static Instant getInstant(Object input)
        {
            if (input instanceof Instant)
            {
                return (Instant) input;
            }

            if (input instanceof OffsetDateTime)
            {
                return ((OffsetDateTime) input).toInstant();
            }

            if (input instanceof ZonedDateTime)
            {
                return ((ZonedDateTime) input).toInstant();
            }

            if (input instanceof Date)
            {
                Date date = (Date) input;
                return date.toInstant();
            }

            if (input instanceof String)
            {
                String inputString = input.toString();
                OffsetDateTime parsedOffsetDateTime = InstantCoercing.parseOffsetDateTime(
                        inputString,
                        CoercingSerializeException::new);
                return parsedOffsetDateTime.toInstant();
            }

            String error = String.format(
                    "Expected something we can convert to 'java.time.OffsetDateTime' but was '%s'.",
                    InstantCoercing.typeName(input));
            throw new CoercingSerializeException(error);
        }

        @Override
        public Instant parseValue(Object input)
        {
            if (input instanceof Instant)
            {
                return (Instant) input;
            }

            if (input instanceof OffsetDateTime)
            {
                return ((OffsetDateTime) input).toInstant();
            }

            if (input instanceof ZonedDateTime)
            {
                return ((ZonedDateTime) input).toOffsetDateTime().toInstant();
            }

            if (input instanceof String)
            {
                String inputString = input.toString();
                OffsetDateTime parsedOffsetDateTime = InstantCoercing.parseOffsetDateTime(
                        inputString,
                        CoercingParseValueException::new);
                return parsedOffsetDateTime.toInstant();
            }

            String error = String.format("Expected a 'String' but was '%s'.", InstantCoercing.typeName(input));
            throw new CoercingParseValueException(error);
        }

        @Override
        public Instant parseLiteral(Object input)
        {
            if (!(input instanceof StringValue))
            {
                String error = String.format(
                        "Expected AST type 'StringValue' but was '%s'.",
                        InstantCoercing.typeName(input));
                throw new CoercingParseLiteralException(error);
            }
            return InstantCoercing.parseOffsetDateTime(
                    ((StringValue) input).getValue(),
                    CoercingParseLiteralException::new).toInstant();
        }

        private static OffsetDateTime parseOffsetDateTime(
                @Nonnull String s,
                @Nonnull Function<String, RuntimeException> exceptionMaker)
        {
            try
            {
                return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
            catch (DateTimeParseException e)
            {
                throw exceptionMaker.apply("Invalid RFC3339 value : '"
                        + s
                        + "'. because of : '"
                        + e.getMessage()
                        + "'");
            }
        }
    }
}
