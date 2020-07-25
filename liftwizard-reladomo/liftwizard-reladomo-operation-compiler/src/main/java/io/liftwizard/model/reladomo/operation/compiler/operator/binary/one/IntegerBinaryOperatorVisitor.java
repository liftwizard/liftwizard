package io.liftwizard.model.reladomo.operation.compiler.operator.binary.one;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.IntegerAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;

public class IntegerBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final IntegerAttribute attribute;
    private final Integer          parameter;

    public IntegerBinaryOperatorVisitor(IntegerAttribute attribute, Integer parameter)
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
}
