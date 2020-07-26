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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;

public class TemporalRangeBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final AsOfAttribute attribute;
    private final Instant       parameter;
    private final Timestamp     timestamp;

    public TemporalRangeBinaryOperatorVisitor(AsOfAttribute attribute, Instant parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
        this.timestamp = Timestamp.from(parameter);
    }

    @Override
    public Operation visitOperatorEq(OperatorEqContext ctx)
    {
        return this.attribute.eq(this.timestamp);
    }
}
