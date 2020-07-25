package io.liftwizard.model.reladomo.operation.compiler.operator.binary.many;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.AbstractBinaryOperatorVisitor;
import org.eclipse.collections.api.list.ImmutableList;

public class LocalDateListBinaryOperatorVisitor extends AbstractBinaryOperatorVisitor
{
    private final DateAttribute            attribute;
    private final ImmutableList<LocalDate> parameter;
    private final Set<Timestamp>           timestamps;

    public LocalDateListBinaryOperatorVisitor(DateAttribute attribute, ImmutableList<LocalDate> parameter)
    {
        this.attribute  = Objects.requireNonNull(attribute);
        this.parameter  = Objects.requireNonNull(parameter);
        this.timestamps = new LinkedHashSet<>(parameter.collect(this::getTimestamp).castToList());
    }

    public Timestamp getTimestamp(LocalDate each)
    {
        if (each == null)
        {
            return null;
        }
        return Timestamp.valueOf(each.atStartOfDay());
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
