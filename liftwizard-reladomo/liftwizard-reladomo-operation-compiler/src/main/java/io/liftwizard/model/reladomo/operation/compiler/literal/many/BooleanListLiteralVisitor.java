package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.BooleanLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class BooleanListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Boolean>>
{
    private final BooleanLiteralVisitor booleanLiteralVisitor;

    public BooleanListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.booleanLiteralVisitor = new BooleanLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Boolean list";
    }

    @Override
    public ImmutableList<Boolean> visitBooleanListLiteral(BooleanListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.booleanLiteral())
                .collect(each -> each.accept(this.booleanLiteralVisitor))
                .toImmutable();
    }
}
