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
