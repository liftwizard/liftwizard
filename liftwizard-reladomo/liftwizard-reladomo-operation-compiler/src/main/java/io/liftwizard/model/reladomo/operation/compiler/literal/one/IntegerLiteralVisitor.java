package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class IntegerLiteralVisitor extends AbstractLiteralVisitor<Integer>
{
    public IntegerLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Integer";
    }

    @Override
    public Integer visitIntegerLiteral(IntegerLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        // TODO: Check for NumberFormatException
        return Integer.valueOf(ctx.IntegerLiteral().getText());
    }
}
