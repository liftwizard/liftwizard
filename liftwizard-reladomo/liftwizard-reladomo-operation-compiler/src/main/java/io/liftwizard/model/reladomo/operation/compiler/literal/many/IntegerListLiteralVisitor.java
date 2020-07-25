package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.IntegerLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class IntegerListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Integer>>
{
    private final IntegerLiteralVisitor integerLiteralVisitor;

    public IntegerListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.integerLiteralVisitor = new IntegerLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Integer list";
    }

    @Override
    public ImmutableList<Integer> visitIntegerListLiteral(IntegerListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.integerLiteral())
                .collect(each -> each.accept(this.integerLiteralVisitor))
                .toImmutable();
    }
}
