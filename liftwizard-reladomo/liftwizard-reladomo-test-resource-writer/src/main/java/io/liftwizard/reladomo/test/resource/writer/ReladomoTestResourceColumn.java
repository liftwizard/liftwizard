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

package io.liftwizard.reladomo.test.resource.writer;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

public class ReladomoTestResourceColumn
{
    private final Attribute                attribute;
    private final MutableList<Object>      values = Lists.mutable.empty();

    private FrozenReladomoTestResourceColumn frozen;

    public ReladomoTestResourceColumn(Attribute attribute)
    {
        this.attribute = Objects.requireNonNull(attribute);
    }

    public void addMithraObject(Object mithraObject)
    {
        Object value = this.attribute.valueOf(mithraObject);

        if (this.attribute.valueType() == Timestamp.class)
        {
            Timestamp timestamp         = (Timestamp) value;
            Timestamp adjustedTimestamp = this.adjustTimestamp(timestamp);
            this.values.add(adjustedTimestamp);
        }
        else
        {
            this.values.add(value);
        }
    }

    private Timestamp adjustTimestamp(Timestamp timestamp)
    {
        if (timestamp == null)
        {
            return null;
        }

        TimestampAttribute timestampAttribute = (TimestampAttribute) this.attribute;
        Timestamp          infinityTimestamp  = timestampAttribute.getAsOfAttributeInfinity();

        if (timestamp.equals(infinityTimestamp))
        {
            return infinityTimestamp;
        }

        Instant       instant       = timestamp.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return Timestamp.valueOf(localDateTime);
    }

    public void freeze()
    {
        if (this.frozen != null)
        {
            throw new AssertionError();
        }

        String   attributeName = this.attribute.getAttributeName();
        Class<?> valueType     = this.attribute.valueType();

        if (valueType == String.class)
        {
            ImmutableList<String> unpaddedValueStrings = this.values
                    .collect(String.class::cast)
                    .collect(ReladomoTestResourceColumn::quote)
                    .toImmutable();
            int maxValueLength = unpaddedValueStrings.asLazy()
                    .collectInt(String::length)
                    .max();
            int maxLength = Math.max(attributeName.length(), maxValueLength);

            String paddedHeader = ReladomoTestResourceColumn.padRight(attributeName, maxLength);

            ImmutableList<String> paddedValueStrings = unpaddedValueStrings
                    .collect(each -> ReladomoTestResourceColumn.padRight(each, maxLength));
            this.frozen = new FrozenReladomoTestResourceColumn(paddedHeader, paddedValueStrings);
        }
        else if (valueType == Long.class)
        {
            this.handlePrimitive(attributeName, Long.class);
        }
        else if (valueType == Integer.class)
        {
            this.handlePrimitive(attributeName, Integer.class);
        }
        else if (valueType == Double.class)
        {
            this.handlePrimitive(attributeName, Double.class);
        }
        else if (valueType == Float.class)
        {
            this.handlePrimitive(attributeName, Float.class);
        }
        else if (valueType == Boolean.class)
        {
            this.handlePrimitive(attributeName, Boolean.class);
        }
        else if (valueType == Timestamp.class)
        {
            ImmutableList<String> unpaddedValueStrings = this.values
                    .collect(Timestamp.class::cast)
                    .collect(ReladomoTestResourceColumn::quote)
                    .toImmutable();
            int maxValueLength = unpaddedValueStrings.asLazy()
                    .collectInt(String::length)
                    .max();
            int maxLength = Math.max(attributeName.length(), maxValueLength);

            String paddedHeader = ReladomoTestResourceColumn.padRight(attributeName, maxLength);

            ImmutableList<String> paddedValueStrings = unpaddedValueStrings
                    .collect(each -> ReladomoTestResourceColumn.padRight(each, maxLength));
            this.frozen = new FrozenReladomoTestResourceColumn(paddedHeader, paddedValueStrings);
        }
        else if (valueType == Date.class)
        {
            ImmutableList<String> unpaddedValueStrings = this.values
                    .collect(Date.class::cast)
                    .collect(ReladomoTestResourceColumn::quote)
                    .toImmutable();
            int maxValueLength = unpaddedValueStrings.asLazy()
                    .collectInt(String::length)
                    .max();
            int maxLength = Math.max(attributeName.length(), maxValueLength);

            String paddedHeader = ReladomoTestResourceColumn.padRight(attributeName, maxLength);

            ImmutableList<String> paddedValueStrings = unpaddedValueStrings
                    .collect(each -> ReladomoTestResourceColumn.padRight(each, maxLength));
            this.frozen = new FrozenReladomoTestResourceColumn(paddedHeader, paddedValueStrings);
        }
        else
        {
            throw new AssertionError(valueType);
        }
    }

    private void handlePrimitive(String attributeName, Class<?> aClass)
    {
        ImmutableList<String> unpaddedValueStrings = this.values
                .collect(aClass::cast)
                .collect(String::valueOf)
                .toImmutable();
        int maxValueLength = unpaddedValueStrings.asLazy()
                .collectInt(String::length)
                .max();
        int maxLength = Math.max(attributeName.length(), maxValueLength);

        String paddedHeader = ReladomoTestResourceColumn.padRight(attributeName, maxLength);
        ImmutableList<String> paddedValueStrings = unpaddedValueStrings
                .collect(each -> ReladomoTestResourceColumn.padLeft(each, maxLength));
        this.frozen = new FrozenReladomoTestResourceColumn(paddedHeader, paddedValueStrings);
    }

    public String getPaddedHeader()
    {
        return this.frozen.getPaddedHeader();
    }

    public String getPaddedValueString(int index)
    {
        return this.frozen.getPaddedValueStrings().get(index);
    }

    private static String quote(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        return "\"" + object + "\"";
    }

    // https://stackoverflow.com/a/391978
    private static String padRight(String string, int length)
    {
        return String.format("%-" + length + "s", string);
    }

    // https://stackoverflow.com/a/391978
    private static String padLeft(String string, int length)
    {
        return String.format("%" + length + "s", string);
    }

    @Override
    public String toString()
    {
        return this.attribute.toString();
    }
}
