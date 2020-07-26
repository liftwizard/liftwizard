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

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class BooleanLiteralVisitor extends AbstractLiteralVisitor<Boolean>
{
    public BooleanLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Boolean";
    }

    @Override
    public Boolean visitBooleanLiteral(BooleanLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        if (ctx.BooleanLiteral().getText().equals("true"))
        {
            return true;
        }

        if (ctx.BooleanLiteral().getText().equals("false"))
        {
            return false;
        }

        return this.throwTypeError(ctx);
    }
}
