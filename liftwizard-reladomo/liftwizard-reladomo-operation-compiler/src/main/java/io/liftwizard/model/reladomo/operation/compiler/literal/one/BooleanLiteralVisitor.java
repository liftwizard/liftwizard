package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class BooleanLiteralVisitor extends AbstractLiteralVisitor<Boolean>
{
    public BooleanLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Boolean";
    }

    @Override
    public Boolean visitBooleanLiteral(BooleanLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        if (ctx.BooleanLiteral().getText().equals("true"))
        {
            return true;
        }

        if (ctx.BooleanLiteral().getText().equals("false"))
        {
            return false;
        }

        return this.throwTypeError(ctx);
    }
}
