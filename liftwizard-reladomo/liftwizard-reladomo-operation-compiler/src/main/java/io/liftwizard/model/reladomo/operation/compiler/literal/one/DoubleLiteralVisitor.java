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
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class DoubleLiteralVisitor extends AbstractLiteralVisitor<Double> {

    public DoubleLiteralVisitor(RelatedFinder finder, String errorContext) {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType() {
        return "Double";
    }

    @Override
    public Double visitIntegerLiteral(IntegerLiteralContext ctx) {
        if (ctx.NullLiteral() != null) {
            return null;
        }

        String text = ctx.IntegerLiteral().getText();
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException e) {
            return this.throwTypeError(ctx);
        }
    }

    @Override
    public Double visitFloatingPointLiteral(FloatingPointLiteralContext ctx) {
        if (ctx.NullLiteral() != null) {
            return null;
        }

        String text = ctx.FloatingPointLiteral().getText();
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException e) {
            return this.throwTypeError(ctx);
        }
    }
}
