package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.LongLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class LongListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Long>>
{
    private final LongLiteralVisitor longLiteralVisitor;

    public LongListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.longLiteralVisitor = new LongLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Long list";
    }

    @Override
    public ImmutableList<Long> visitIntegerListLiteral(IntegerListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.integerLiteral())
                .collect(each -> each.accept(this.longLiteralVisitor))
                .toImmutable();
    }
}
