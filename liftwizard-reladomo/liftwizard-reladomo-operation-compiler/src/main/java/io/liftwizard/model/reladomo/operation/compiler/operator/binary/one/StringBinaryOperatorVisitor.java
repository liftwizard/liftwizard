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

package io.liftwizard.model.reladomo.operation.compiler.operator.binary.one;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardNotEqualsContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;

public class StringBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final StringAttribute attribute;
    private final String          parameter;

    public StringBinaryOperatorVisitor(StringAttribute attribute, String parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
    }

    @Override
    public Operation visitOperatorEq(OperatorEqContext ctx)
    {
        return this.attribute.eq(this.parameter);
    }

    @Override
    public Operation visitOperatorNotEq(OperatorNotEqContext ctx)
    {
        return this.attribute.notEq(this.parameter);
    }

    @Override
    public Operation visitOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        return this.attribute.greaterThan(this.parameter);
    }

    @Override
    public Operation visitOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        return this.attribute.greaterThanEquals(this.parameter);
    }

    @Override
    public Operation visitOperatorLessThan(OperatorLessThanContext ctx)
    {
        return this.attribute.lessThan(this.parameter);
    }

    @Override
    public Operation visitOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        return this.attribute.lessThanEquals(this.parameter);
    }

    @Override
    public Operation visitOperatorStartsWith(OperatorStartsWithContext ctx)
    {
        return this.attribute.startsWith(this.parameter);
    }

    @Override
    public Operation visitOperatorNotStartsWith(OperatorNotStartsWithContext ctx)
    {
        return this.attribute.notStartsWith(this.parameter);
    }

    @Override
    public Operation visitOperatorEndsWith(OperatorEndsWithContext ctx)
    {
        return this.attribute.endsWith(this.parameter);
    }

    @Override
    public Operation visitOperatorNotEndsWith(OperatorNotEndsWithContext ctx)
    {
        return this.attribute.notEndsWith(this.parameter);
    }

    @Override
    public Operation visitOperatorContains(OperatorContainsContext ctx)
    {
        return this.attribute.contains(this.parameter);
    }

    @Override
    public Operation visitOperatorNotContains(OperatorNotContainsContext ctx)
    {
        return this.attribute.notContains(this.parameter);
    }

    @Override
    public Operation visitOperatorWildCardEquals(OperatorWildCardEqualsContext ctx)
    {
        return this.attribute.wildCardEq(this.parameter);
    }

    @Override
    public Operation visitOperatorWildCardNotEquals(OperatorWildCardNotEqualsContext ctx)
    {
        return this.attribute.wildCardNotEq(this.parameter);
    }
}
