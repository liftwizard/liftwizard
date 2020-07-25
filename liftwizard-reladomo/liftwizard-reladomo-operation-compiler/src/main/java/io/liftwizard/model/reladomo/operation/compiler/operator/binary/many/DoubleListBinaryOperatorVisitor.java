package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.DoubleAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.primitive.ImmutableDoubleSet;
import org.eclipse.collections.impl.factory.primitive.DoubleSets;

public class DoubleListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final DoubleAttribute       attribute;
    private final ImmutableList<Double> parameter;
    private final ImmutableDoubleSet    doubleSet;

    public DoubleListBinaryOperatorVisitor(DoubleAttribute attribute, ImmutableList<Double> parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
        this.doubleSet = DoubleSets.immutable.withAll(this.parameter);
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.doubleSet);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.doubleSet);
    }
}
