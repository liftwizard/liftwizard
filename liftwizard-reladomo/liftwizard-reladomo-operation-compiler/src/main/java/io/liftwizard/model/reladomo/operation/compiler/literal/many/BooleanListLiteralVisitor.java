package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;

public class BooleanListLiteralVisitor extends AbstractLiteralVisitor<Boolean>
{
    public BooleanListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "Boolean list";
    }
}
