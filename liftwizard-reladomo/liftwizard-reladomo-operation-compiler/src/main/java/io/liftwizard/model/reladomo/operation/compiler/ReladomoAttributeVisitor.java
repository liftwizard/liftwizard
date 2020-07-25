package io.liftwizard.model.reladomo.operation.compiler;

import java.util.Objects;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.NumericAttribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionAbsoluteValueContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToLowerCaseContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToSubstringContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionUnknownContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.SimpleAttributeContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;
import org.antlr.v4.runtime.RuleContext;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class ReladomoAttributeVisitor extends ReladomoOperationThrowingVisitor<Attribute>
{
    private final RelatedFinder finder;
    private final String        errorContext;

    public ReladomoAttributeVisitor(RelatedFinder finder, String errorContext)
    {
        this.finder       = Objects.requireNonNull(finder);
        this.errorContext = Objects.requireNonNull(errorContext);
    }

    @Override
    public Attribute visitAttribute(AttributeContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public Attribute visitFunctionToLowerCase(FunctionToLowerCaseContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (!(attribute instanceof StringAttribute))
        {
            var error = String.format(
                    "Function '%s' applies to StringAttributes but attribute '%s' is a %s in %s",
                    ctx.functionName.getText(),
                    attribute.getAttributeName(),
                    attribute.getClass().getSuperclass().getSimpleName(),
                    this.errorContext);

            throw new IllegalArgumentException(error);
        }

        return ((StringAttribute) attribute).toLowerCase();
    }

    @Override
    public Attribute visitFunctionToSubstring(FunctionToSubstringContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (!(attribute instanceof StringAttribute))
        {
            var error = String.format(
                    "Function 'substring' applies to StringAttributes but attribute '%s' is a %s in %s",
                    attribute.getAttributeName(),
                    attribute.getClass().getSuperclass().getSimpleName(),
                    this.errorContext);

            throw new IllegalArgumentException(error);
        }

        String  startString = ctx.IntegerLiteral(0).getText();
        String  endString   = ctx.IntegerLiteral(1).getText();
        Integer start       = Integer.valueOf(startString);
        Integer end         = Integer.valueOf(endString);
        return ((StringAttribute) attribute).substring(start, end);
    }

    @Override
    public Attribute visitFunctionAbsoluteValue(FunctionAbsoluteValueContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (!(attribute instanceof NumericAttribute))
        {
            var error = String.format(
                    "Function '%s' applies to NumericAttributes but attribute '%s' is a %s in %s",
                    ctx.functionName.getText(),
                    attribute.getAttributeName(),
                    attribute.getClass().getSuperclass().getSimpleName(),
                    this.errorContext);

            throw new IllegalArgumentException(error);
        }

        return (Attribute) ((NumericAttribute) attribute).absoluteValue();
    }

    @Override
    public Attribute visitFunctionUnknown(FunctionUnknownContext ctx)
    {
        var error = String.format(
                "Unknown function '%s' in %s",
                ctx.functionName.getText(),
                this.errorContext);

        throw new IllegalArgumentException(error);
    }

    @Override
    public Attribute visitSimpleAttribute(SimpleAttributeContext ctx)
    {
        if (ctx.className() != null
                && !Objects.equals(ctx.className().getText(), this.getExpectedClassName(this.finder)))
        {
            var error = String.format(
                    "Expected 'this' or <" + this.getExpectedClassName(this.finder) + "> but found: <%s> in %s",
                    ctx.className().getText(),
                    this.errorContext);
            throw new IllegalArgumentException(error);
        }

        RelatedFinder currentFinder = this.finder;
        MutableList<String> relationshipNames = ListAdapter.adapt(ctx.relationshipName())
                .collect(RuleContext::getText);
        for (String relationshipName : relationshipNames)
        {
            RelatedFinder nextFinder = currentFinder.getRelationshipFinderByName(relationshipName);
            if (nextFinder == null)
            {
                var error = String.format(
                        "Could not find relationship '%s' on type '%s' in %s",
                        relationshipName,
                        this.getExpectedClassName(currentFinder),
                        this.errorContext);
                throw new IllegalArgumentException(error);
            }
            currentFinder = nextFinder;
        }

        String    attributeName = ctx.attributeName().getText();
        Attribute attribute     = currentFinder.getAttributeByName(attributeName);
        if (attribute == null)
        {
            var error = String.format(
                    "Could not find attribute '%s' on type '%s' in %s",
                    attributeName,
                    this.getExpectedClassName(currentFinder),
                    this.errorContext);
            throw new IllegalArgumentException(error);
        }
        return attribute;
    }

    public String getExpectedClassName(RelatedFinder relatedFinder)
    {
        return relatedFinder
                .getMithraObjectPortal()
                .getClassMetaData()
                .getBusinessOrInterfaceClass()
                .getSimpleName();
    }
}
