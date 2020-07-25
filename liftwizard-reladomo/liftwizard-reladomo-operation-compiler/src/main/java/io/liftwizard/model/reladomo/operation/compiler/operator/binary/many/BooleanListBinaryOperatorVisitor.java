package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.BooleanAttribute;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;

public class BooleanListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final BooleanAttribute       attribute;
    private final ImmutableList<Boolean> parameter;

    public BooleanListBinaryOperatorVisitor(BooleanAttribute attribute, ImmutableList<Boolean> parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
    }
}
