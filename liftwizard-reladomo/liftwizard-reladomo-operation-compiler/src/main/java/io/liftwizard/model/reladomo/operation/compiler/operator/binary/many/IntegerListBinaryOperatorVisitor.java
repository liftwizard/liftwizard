package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.IntegerAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;

public class IntegerListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final IntegerAttribute       attribute;
    private final ImmutableList<Integer> parameter;
    private final ImmutableIntSet        intSet;

    public IntegerListBinaryOperatorVisitor(IntegerAttribute attribute, ImmutableList<Integer> parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
        this.intSet    = IntSets.immutable.withAll(this.parameter);
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.intSet);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.intSet);
    }
}
