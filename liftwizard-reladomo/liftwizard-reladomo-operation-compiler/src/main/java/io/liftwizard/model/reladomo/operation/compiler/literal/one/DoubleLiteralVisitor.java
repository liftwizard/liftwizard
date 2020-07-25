package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class DoubleLiteralVisitor extends AbstractLiteralVisitor<Double>
{
    public DoubleLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Double";
    }

    @Override
    public Double visitFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        return Double.valueOf(ctx.FloatingPointLiteral().getText());
    }
}
