package io.liftwizard.model.reladomo.operation.compiler.operator.unary;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.EqualsEdgePointContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNotNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.UnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;

public class AsOfUnaryOperatorVisitor extends ReladomoOperationThrowingVisitor<Operation>
{
    private final AsOfAttribute attribute;

    public AsOfUnaryOperatorVisitor(AsOfAttribute attribute)
    {
        this.attribute = Objects.requireNonNull(attribute);
    }

    @Override
    public Operation visitUnaryOperator(UnaryOperatorContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public Operation visitEqualsEdgePoint(EqualsEdgePointContext ctx)
    {
        return this.attribute.equalsEdgePoint();
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
