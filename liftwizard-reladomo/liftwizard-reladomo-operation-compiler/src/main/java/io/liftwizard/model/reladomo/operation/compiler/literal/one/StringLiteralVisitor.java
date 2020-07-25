package io.liftwizard.model.reladomo.operation.compiler.literal.one;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import org.apache.commons.text.StringEscapeUtils;

public class StringLiteralVisitor extends AbstractLiteralVisitor<String>
{
    public StringLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "String";
    }

    @Override
    public String visitStringLiteral(StringLiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return null;
        }
        String quotedText      = ctx.StringLiteral().getText();
        String unquotedText    = quotedText.substring(1, quotedText.length() - 1);
        String unescapedString = StringEscapeUtils.unescapeJava(unquotedText);
        return unescapedString;
    }
}
