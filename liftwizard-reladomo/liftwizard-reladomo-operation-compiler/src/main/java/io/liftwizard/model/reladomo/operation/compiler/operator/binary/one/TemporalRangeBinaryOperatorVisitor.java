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
