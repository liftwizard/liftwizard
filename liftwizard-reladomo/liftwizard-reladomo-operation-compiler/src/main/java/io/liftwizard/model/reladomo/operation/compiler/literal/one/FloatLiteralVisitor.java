package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class FloatLiteralVisitor extends AbstractLiteralVisitor<Float>
{
    public FloatLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Float";
    }

    @Override
    public Float visitFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        // TODO: Check for NumberFormatException
        return Float.valueOf(ctx.FloatingPointLiteral().getText());
    }
}
