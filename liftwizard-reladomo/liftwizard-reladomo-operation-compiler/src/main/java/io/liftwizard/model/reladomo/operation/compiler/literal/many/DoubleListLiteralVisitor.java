package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.DoubleLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class DoubleListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Double>>
{
    private final DoubleLiteralVisitor doubleLiteralVisitor;

    public DoubleListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.doubleLiteralVisitor = new DoubleLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Double list";
    }

    @Override
    public ImmutableList<Double> visitFloatingPointListLiteral(FloatingPointListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.floatingPointLiteral())
                .collect(each -> each.accept(this.doubleLiteralVisitor))
                .toImmutable();
    }
}
