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

package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;

public class LocalDateListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final DateAttribute  attribute;
    private final Set<Timestamp> timestamps;

    public LocalDateListBinaryOperatorVisitor(DateAttribute attribute, ImmutableList<LocalDate> parameter)
    {
        this.attribute  = Objects.requireNonNull(attribute);
        this.timestamps = new LinkedHashSet<>(parameter.collect(this::getTimestamp).castToList());
    }

    public Timestamp getTimestamp(LocalDate each)
    {
        if (each == null)
        {
            return null;
        }
        return Timestamp.valueOf(each.atStartOfDay());
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.timestamps);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.timestamps);
    }
}
