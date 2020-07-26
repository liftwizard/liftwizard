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

package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.BooleanLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class BooleanListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Boolean>>
{
    private final BooleanLiteralVisitor booleanLiteralVisitor;

    public BooleanListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.booleanLiteralVisitor = new BooleanLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Boolean list";
    }

    @Override
    public ImmutableList<Boolean> visitBooleanListLiteral(BooleanListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.booleanLiteral())
                .collect(each -> each.accept(this.booleanLiteralVisitor))
                .toImmutable();
    }
}
