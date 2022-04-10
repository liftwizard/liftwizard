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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
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
 * Derived from https://github.com/graphql-java/graphql-java-extended-scalars but named LocalDate instead of Date.
 */
@Internal
public class GraphQLLocalDateScalar extends GraphQLScalarType
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public GraphQLLocalDateScalar()
    {
        super("LocalDate", "An RFC-3339 compliant LocalDate Scalar", new LocalDateCoercing());
    }

    private static class LocalDateCoercing implements Coercing<LocalDate, String>
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
            TemporalAccessor temporalAccessor = this.getTemporalAccessorSerialize(input);
            try
            {
                return DATE_FORMATTER.format(temporalAccessor);
            }
            catch (DateTimeException e)
            {
                throw new CoercingSerializeException("Unable to turn TemporalAccessor into LocalDate because of : '" + e.getMessage() + "'.");
            }
        }

        private TemporalAccessor getTemporalAccessorSerialize(Object input)
        {
            if (input instanceof TemporalAccessor)
            {
                return (TemporalAccessor) input;
            }

            if (input instanceof String)
            {
                return this.parseLocalDate(input.toString(), CoercingSerializeException::new);
            }

            String error = String.format(
                    "Expected a 'String' or 'java.time.temporal.TemporalAccessor' but was '%s'.",
                    LocalDateCoercing.typeName(input));
            throw new CoercingSerializeException(error);
        }

        @Override
        public LocalDate parseValue(Object input)
        {
            TemporalAccessor temporalAccessor = this.getTemporalAccessorParse(input);
            try
            {
                return LocalDate.from(temporalAccessor);
            }
            catch (DateTimeException e)
            {
                throw new CoercingParseValueException("Unable to turn TemporalAccessor into full date because of : '" + e.getMessage() + "'.");
            }
        }

        private TemporalAccessor getTemporalAccessorParse(Object input)
        {
            if (input instanceof TemporalAccessor)
            {
                return (TemporalAccessor) input;
            }

            if (input instanceof String)
            {
                return this.parseLocalDate(input.toString(), CoercingParseValueException::new);
            }

            String error = String.format(
                    "Expected a 'String' or 'java.time.temporal.TemporalAccessor' but was '%s'.",
                    LocalDateCoercing.typeName(input));
            throw new CoercingParseValueException(error);
        }

        @Override
        public LocalDate parseLiteral(Object input)
        {
            if (!(input instanceof StringValue))
            {
                throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '"
                        + LocalDateCoercing.typeName(input)
                        + "'.");
            }
            return this.parseLocalDate(((StringValue) input).getValue(), CoercingParseLiteralException::new);
        }

        private LocalDate parseLocalDate(String s, Function<String, RuntimeException> exceptionMaker)
        {
            try
            {
                TemporalAccessor temporalAccessor = DATE_FORMATTER.parse(s);
                return LocalDate.from(temporalAccessor);
            }
            catch (DateTimeParseException e)
            {
                throw exceptionMaker.apply("Invalid RFC3339 full date value : '" + s + "'. because of : '" + e.getMessage() + "'");
            }
        }
    }
}
