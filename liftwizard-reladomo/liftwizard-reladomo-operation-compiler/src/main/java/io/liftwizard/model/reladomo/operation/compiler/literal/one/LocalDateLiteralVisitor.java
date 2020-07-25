package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class LocalDateLiteralVisitor extends AbstractLiteralVisitor<LocalDate>
{
    public LocalDateLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "LocalDate";
    }

    @Override
    public LocalDate visitStringLiteral(StringLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }

        String quotedText   = ctx.StringLiteral().getText();
        String unquotedText = quotedText.substring(1, quotedText.length() - 1);
        try
        {
            return LocalDate.parse(unquotedText);
        }
        catch (DateTimeParseException e)
        {
            return this.throwTypeError(ctx);
        }
    }
}
