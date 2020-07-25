package io.liftwizard.model.reladomo.operation.compiler.operator.binary.one;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.BooleanAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;

public class BooleanBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final BooleanAttribute attribute;
    private final Boolean          parameter;

    public BooleanBinaryOperatorVisitor(BooleanAttribute attribute, Boolean parameter)
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
}
