/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.serialization.jackson.pretty;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.core.util.Separators.Spacing;

public class JsonPrettyPrinter
        extends DefaultPrettyPrinter
{
    public static final Separators      SEPARATORS = new Separators(
            Separators.DEFAULT_ROOT_VALUE_SEPARATOR,
            ':',
            Spacing.AFTER,
            ',',
            Spacing.NONE,
            "",
            ',',
            Spacing.NONE,
            "");

    public static final DefaultIndenter INDENTER   = new DefaultIndenter("  ", "\n");

    public JsonPrettyPrinter()
    {
        this(SEPARATORS, INDENTER, INDENTER);
    }

    public JsonPrettyPrinter(Separators separators, DefaultIndenter arrayIndenter, DefaultIndenter objectIndenter)
    {
        super(separators);
        this.indentArraysWith(arrayIndenter);
        this.indentObjectsWith(objectIndenter);
    }

    @Nonnull
    @Override
    public DefaultPrettyPrinter createInstance()
    {
        return this;
    }
}
