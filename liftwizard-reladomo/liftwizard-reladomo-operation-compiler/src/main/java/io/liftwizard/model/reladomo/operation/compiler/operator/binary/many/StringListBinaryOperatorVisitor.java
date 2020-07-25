package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;

public class StringListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final StringAttribute       attribute;
    private final ImmutableList<String> parameter;
    private final Set<String>           stringSet;

    public StringListBinaryOperatorVisitor(StringAttribute attribute, ImmutableList<String> parameter)
    {
        this.attribute = Objects.requireNonNull(attribute);
        this.parameter = Objects.requireNonNull(parameter);
        this.stringSet = new LinkedHashSet<>(parameter.castToList());
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.stringSet);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.stringSet);
    }

    @Override
    public Operation visitOperatorWildCardIn(OperatorWildCardInContext ctx)
    {
        return this.attribute.wildCardIn(this.stringSet);
    }
}
