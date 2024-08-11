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

package io.liftwizard.model.reladomo.operation.compiler;

import java.util.Objects;

import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ExistsOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorExistsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotExistsContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;

public class ReladomoExistsOperatorVisitor
        extends ReladomoOperationThrowingVisitor<Operation>
{
    private final AbstractRelatedFinder navigation;
    private final Operation notExistsOperation;

    public ReladomoExistsOperatorVisitor(
            AbstractRelatedFinder navigation,
            Operation notExistsOperation)
    {
        this.navigation = Objects.requireNonNull(navigation);
        this.notExistsOperation = notExistsOperation;
    }

    @Override
    public Operation visitExistsOperator(ExistsOperatorContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public Operation visitOperatorExists(OperatorExistsContext ctx)
    {
        if (this.notExistsOperation != null)
        {
            throw new AssertionError();
        }
        return this.navigation.exists();
    }

    @Override
    public Operation visitOperatorNotExists(OperatorNotExistsContext ctx)
    {
        return this.notExistsOperation == null
                ? this.navigation.notExists()
                : this.navigation.notExists(this.notExistsOperation);
    }
}
