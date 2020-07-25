package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;

public class InstantListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final TimestampAttribute     attribute;
    private final ImmutableList<Instant> parameter;
    private final Set<Timestamp>         timestamps;

    public InstantListBinaryOperatorVisitor(TimestampAttribute attribute, ImmutableList<Instant> parameter)
    {
        this.attribute  = Objects.requireNonNull(attribute);
        this.parameter  = Objects.requireNonNull(parameter);
        this.timestamps = new LinkedHashSet<>(parameter.collect(this::getTimestamp).castToList());
    }

    public Timestamp getTimestamp(Instant instant)
    {
        if (instant == null)
        {
            return null;
        }
        return Timestamp.from(instant);
    }

    @Override
    public Operation visitOperatorIn(OperatorInContext ctx)
    {
        return this.attribute.in(this.timestamps);
    }

    @Override
    public Operation visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return this.attribute.notIn(this.timestamps);
    }
}
