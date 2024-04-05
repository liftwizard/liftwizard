/*
 * Copyright 2024 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.model.reladomo.operation.compiler;

import java.util.List;
import java.util.Objects;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.attribute.NumericAttribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionAbsoluteValueContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionDayOfMonthContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionMonthContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToLowerCaseContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToSubstringContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionUnknownContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionYearContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.SimpleAttributeContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;
import org.antlr.v4.runtime.RuleContext;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
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
            String error = "Function '%s' applies to StringAttributes but attribute '%s' is a %s in %s".formatted(
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
            String error = "Function 'substring' applies to StringAttributes but attribute '%s' is a %s in %s".formatted(
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
            String error = "Function '%s' applies to NumericAttributes but attribute '%s' is a %s in %s".formatted(
                    ctx.functionName.getText(),
                    attribute.getAttributeName(),
                    attribute.getClass().getSuperclass().getSimpleName(),
                    this.errorContext);

            throw new IllegalArgumentException(error);
        }

        return (Attribute) ((NumericAttribute) attribute).absoluteValue();
    }

    @Override
    public Attribute visitFunctionYear(FunctionYearContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (attribute instanceof TimestampAttribute timestampAttribute)
        {
            return timestampAttribute.year();
        }
        if (attribute instanceof DateAttribute dateAttribute)
        {
            return dateAttribute.year();
        }

        String error = "Function 'year' applies to TimestampAttributes and DateAttributes but attribute '%s' is a %s in %s".formatted(
                attribute.getAttributeName(),
                attribute.getClass().getSuperclass().getSimpleName(),
                this.errorContext);

        throw new IllegalArgumentException(error);
    }

    @Override
    public Attribute visitFunctionMonth(FunctionMonthContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (attribute instanceof TimestampAttribute timestampAttribute)
        {
            return timestampAttribute.month();
        }
        if (attribute instanceof DateAttribute dateAttribute)
        {
            return dateAttribute.month();
        }

        String error = "Function 'month' applies to TimestampAttributes and DateAttributes but attribute '%s' is a %s in %s".formatted(
                attribute.getAttributeName(),
                attribute.getClass().getSuperclass().getSimpleName(),
                this.errorContext);

        throw new IllegalArgumentException(error);
    }

    @Override
    public Attribute visitFunctionDayOfMonth(FunctionDayOfMonthContext ctx)
    {
        Attribute attribute = ctx.attribute().accept(this);

        if (attribute instanceof TimestampAttribute timestampAttribute)
        {
            return timestampAttribute.dayOfMonth();
        }
        if (attribute instanceof DateAttribute dateAttribute)
        {
            return dateAttribute.dayOfMonth();
        }

        String error = "Function 'dayOfMonth' applies to TimestampAttributes and DateAttributes but attribute '%s' is a %s in %s".formatted(
                attribute.getAttributeName(),
                attribute.getClass().getSuperclass().getSimpleName(),
                this.errorContext);

        throw new IllegalArgumentException(error);
    }

    @Override
    public Attribute visitFunctionUnknown(FunctionUnknownContext ctx)
    {
        String error = "Unknown function '%s' in %s".formatted(
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
            String error = "Expected 'this' or <%s> but found: <%s> in %s".formatted(
                    this.getExpectedClassName(this.finder),
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
                List<RelatedFinder> relationshipFinders = currentFinder.getRelationshipFinders();
                MutableList<String> validRelationshipNames = ListAdapter.adapt(relationshipFinders)
                        .selectInstancesOf(AbstractRelatedFinder.class)
                        .collect(AbstractRelatedFinder::getRelationshipName);

                String error = "Could not find relationship '%s' on type '%s' in %s. Valid relationships: %s".formatted(
                        relationshipName,
                        this.getExpectedClassName(currentFinder),
                        this.errorContext,
                        validRelationshipNames);
                throw new IllegalArgumentException(error);
            }
            currentFinder = nextFinder;
        }

        String    attributeName = ctx.attributeName().getText();
        Attribute attribute     = currentFinder.getAttributeByName(attributeName);
        if (attribute == null)
        {
            Attribute[] persistentAttributes = currentFinder.getMithraObjectPortal().getFinder().getPersistentAttributes();
            MutableList<String> validAttributeNames = ArrayAdapter.adapt(persistentAttributes)
                    .collect(Attribute::getAttributeName);
            String error = "Could not find attribute '%s' on type '%s' in %s. Valid attributes: %s".formatted(
                    attributeName,
                    this.getExpectedClassName(currentFinder),
                    this.errorContext,
                    validAttributeNames);
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
