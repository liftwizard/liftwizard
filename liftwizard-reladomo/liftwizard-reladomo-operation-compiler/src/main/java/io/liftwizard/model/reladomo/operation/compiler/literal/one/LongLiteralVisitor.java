package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class LongLiteralVisitor extends AbstractLiteralVisitor<Long>
{
    public LongLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Long";
    }

    @Override
    public Long visitIntegerLiteral(IntegerLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        return Long.valueOf(ctx.getText());
    }
}
