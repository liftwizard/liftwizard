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

package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class InstantLiteralVisitor extends AbstractLiteralVisitor<Instant>
{
    public InstantLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Instant";
    }

    @Override
    public Instant visitStringLiteral(StringLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        String quotedText   = ctx.StringLiteral().getText();
        String unquotedText = quotedText.substring(1, quotedText.length() - 1);
        try
        {
            return Instant.parse(unquotedText);
        }
        catch (DateTimeParseException e)
        {
            return this.throwTypeError(ctx);
        }
    }
}
