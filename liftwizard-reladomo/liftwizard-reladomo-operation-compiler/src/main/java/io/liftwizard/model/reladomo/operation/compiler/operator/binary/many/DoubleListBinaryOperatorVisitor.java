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

package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.DoubleAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.primitive.ImmutableDoubleSet;
import org.eclipse.collections.impl.factory.primitive.DoubleSets;

public class DoubleListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final DoubleAttribute       attribute;
    private final ImmutableList<Double> parameter;
    private final ImmutableDoubleSet    doubleSet;

    public DoubleListBinaryOperatorVisitor(DoubleAttribute attribute, ImmutableList<Double> parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
        this.doubleSet = DoubleSets.immutable.withAll(this.parameter);
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.doubleSet);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.doubleSet);
    }
}
