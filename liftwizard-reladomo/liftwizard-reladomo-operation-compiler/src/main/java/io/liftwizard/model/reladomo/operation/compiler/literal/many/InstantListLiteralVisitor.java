package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import java.time.Instant;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.InstantLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class InstantListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Instant>>
{
    private final InstantLiteralVisitor instantLiteralVisitor;

    public InstantListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.instantLiteralVisitor = new InstantLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Instant list";
    }

    @Override
    public ImmutableList<Instant> visitStringListLiteral(StringListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.stringLiteral())
                .collect(each -> each.accept(this.instantLiteralVisitor))
                .toImmutable();
    }
}
