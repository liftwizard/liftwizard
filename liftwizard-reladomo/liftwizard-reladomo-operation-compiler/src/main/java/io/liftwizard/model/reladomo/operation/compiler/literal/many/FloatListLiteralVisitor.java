package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.FloatLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class FloatListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<Float>>
{
    private final FloatLiteralVisitor floatLiteralVisitor;

    public FloatListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.floatLiteralVisitor = new FloatLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Float list";
    }

    @Override
    public ImmutableList<Float> visitFloatingPointListLiteral(FloatingPointListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.floatingPointLiteral())
                .collect(each -> each.accept(this.floatLiteralVisitor))
                .toImmutable();
    }
}
