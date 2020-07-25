package io.liftwizard.model.reladomo.operation.compiler.operator.binary.one;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;

public class InstantBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final TimestampAttribute attribute;
    private final Instant            parameter;
    private final Timestamp          timestamp;

    public InstantBinaryOperatorVisitor(TimestampAttribute attribute, Instant parameter)
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

    @Override
    public Operation visitOperatorNotEq(OperatorNotEqContext ctx)
    {
        return this.attribute.notEq(this.timestamp);
    }

    @Override
    public Operation visitOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        return this.attribute.greaterThan(this.timestamp);
    }

    @Override
    public Operation visitOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        return this.attribute.greaterThanEquals(this.timestamp);
    }

    @Override
    public Operation visitOperatorLessThan(OperatorLessThanContext ctx)
    {
        return this.attribute.lessThan(this.timestamp);
    }

    @Override
    public Operation visitOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        return this.attribute.lessThanEquals(this.timestamp);
    }
}
