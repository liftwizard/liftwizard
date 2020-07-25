package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class InstantLiteralVisitor extends AbstractLiteralVisitor<Instant>
{
    public InstantLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Instant";
    }

    @Override
    public Instant visitStringLiteral(StringLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        String quotedText   = ctx.StringLiteral().getText();
        String unquotedText = quotedText.substring(1, quotedText.length() - 1);
        try
        {
            return Instant.parse(unquotedText);
        }
        catch (DateTimeParseException e)
        {
            return this.throwTypeError(ctx);
        }
    }
}
