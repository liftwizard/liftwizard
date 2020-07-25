package io.liftwizard.model.reladomo.operation.compiler.operator.binary;

import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;

public abstract class AbstractBinaryOperatorVisitor extends ReladomoOperationThrowingVisitor<Operation>
{
    @Override
    public Operation visitBinaryOperator(BinaryOperatorContext ctx)
    {
        return this.visitChildren(ctx);
    }
}
