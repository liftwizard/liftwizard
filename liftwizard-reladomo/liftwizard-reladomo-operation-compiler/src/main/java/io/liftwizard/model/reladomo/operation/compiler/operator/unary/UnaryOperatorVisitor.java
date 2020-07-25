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

package io.liftwizard.model.reladomo.operation.compiler.operator.unary;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNotNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.UnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;

public class UnaryOperatorVisitor extends ReladomoOperationThrowingVisitor<Operation>
{
    private final Attribute attribute;

    public UnaryOperatorVisitor(Attribute attribute)
    {
        this.attribute = Objects.requireNonNull(attribute);
    }

    @Override
    public Operation visitUnaryOperator(UnaryOperatorContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public Operation visitOperatorIsNull(OperatorIsNullContext ctx)
    {
        return this.attribute.isNull();
    }

    @Override
    public Operation visitOperatorIsNotNull(OperatorIsNotNullContext ctx)
    {
        return this.attribute.isNotNull();
    }
}
