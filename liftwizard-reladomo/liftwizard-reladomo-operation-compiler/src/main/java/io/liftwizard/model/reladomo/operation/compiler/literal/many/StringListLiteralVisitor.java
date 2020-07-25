package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.StringLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class StringListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<String>>
{
    private final StringLiteralVisitor stringLiteralVisitor;

    public StringListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.stringLiteralVisitor = new StringLiteralVisitor(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "String list";
    }

    @Override
    public ImmutableList<String> visitStringListLiteral(StringListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.stringLiteral())
                .collect(each -> each.accept(this.stringLiteralVisitor))
                .toImmutable();
    }
}
