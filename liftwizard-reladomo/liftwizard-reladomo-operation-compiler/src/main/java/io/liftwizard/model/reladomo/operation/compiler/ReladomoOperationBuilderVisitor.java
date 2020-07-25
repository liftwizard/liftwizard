/*
 * Copyright 2020 Craig Motlin
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.BooleanAttribute;
import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.attribute.DoubleAttribute;
import com.gs.fw.common.mithra.attribute.FloatAttribute;
import com.gs.fw.common.mithra.attribute.IntegerAttribute;
import com.gs.fw.common.mithra.attribute.LongAttribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.None;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CompilationUnitContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAllContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationBinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationNoneContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationUnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.BooleanListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.DoubleListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.FloatListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.InstantListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.IntegerListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.LocalDateListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.LongListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.many.StringListLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.BooleanLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.DoubleLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.FloatLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.InstantLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.IntegerLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.LocalDateLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.LongLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.StringLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.BooleanListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.DoubleListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.FloatListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.InstantListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.IntegerListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.LocalDateListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.LongListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.many.StringListBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.BooleanBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.DoubleBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.FloatBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.InstantBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.IntegerBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.LocalDateBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.LongBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.StringBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.binary.one.TemporalRangeBinaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.unary.AsOfUnaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.compiler.operator.unary.UnaryOperatorVisitor;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class ReladomoOperationBuilderVisitor extends ReladomoOperationThrowingVisitor<Operation>
{
    private final RelatedFinder     finder;
    private final CommonTokenStream tokenStream;

    public ReladomoOperationBuilderVisitor(RelatedFinder finder, CommonTokenStream tokenStream)
    {
        this.finder      = Objects.requireNonNull(finder);
        this.tokenStream = Objects.requireNonNull(tokenStream);
    }

    @Override
    public Operation visitCompilationUnit(CompilationUnitContext ctx)
    {
        return ctx.compositeOperation().accept(this);
    }

    @Override
    public Operation visitOperationNone(OperationNoneContext ctx)
    {
        // There's no method this.finder.none()
        // But this is similar to the internal implementation of all()
        return new None(this.finder.getPrimaryKeyAttributes()[0]);
    }

    @Override
    public Operation visitOperationAll(OperationAllContext ctx)
    {
        return this.finder.all();
    }

    @Override
    public Operation visitOperationUnaryOperator(OperationUnaryOperatorContext ctx)
    {
        String    contextString = this.getContextString(ctx);
        Attribute attribute     = this.getAttribute(ctx.attribute(), contextString);
        return this.getUnaryOperation(ctx, attribute);
    }

    @Override
    public Operation visitOperationBinaryOperator(OperationBinaryOperatorContext ctx)
    {
        String               contextString        = this.getContextString(ctx);
        Attribute            attribute            = this.getAttribute(ctx.attribute(), contextString);
        ParameterCardinality parameterCardinality = this.getParameterCardinality(ctx);
        Object parameter = this.getParameter(
                ctx,
                attribute,
                parameterCardinality,
                contextString);
        return this.getBinaryOperation(ctx, attribute, parameterCardinality, parameter);
    }

    private ParameterCardinality getParameterCardinality(OperationBinaryOperatorContext ctx)
    {
        ReladomoOperationVisitor<ParameterCardinality> operatorVisitor = new ParameterCardinalityVisitor();
        return ctx.binaryOperator().accept(operatorVisitor);
    }

    private Attribute getAttribute(AttributeContext attributeContext, String errorContext)
    {
        var attributeVisitor = new ReladomoAttributeVisitor(this.finder, errorContext);
        return attributeContext.accept(attributeVisitor);
    }

    private Object getParameter(
            OperationBinaryOperatorContext ctx,
            Attribute attribute,
            ParameterCardinality parameterCardinality,
            String errorContext)
    {
        ReladomoOperationVisitor<?> parameterVisitor = this.getParameterVisitor(
                attribute,
                parameterCardinality,
                errorContext);
        return ctx.parameter().accept(parameterVisitor);
    }

    private ReladomoOperationVisitor<?> getParameterVisitor(
            Attribute attribute,
            ParameterCardinality parameterCardinality,
            String errorContext)
    {
        if (parameterCardinality == ParameterCardinality.ONE)
        {
            if (attribute instanceof StringAttribute)
            {
                return new StringLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof BooleanAttribute)
            {
                return new BooleanLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof IntegerAttribute)
            {
                return new IntegerLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof LongAttribute)
            {
                return new LongLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DoubleAttribute)
            {
                return new DoubleLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof FloatAttribute)
            {
                return new FloatLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DateAttribute)
            {
                return new LocalDateLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof TimestampAttribute || attribute instanceof AsOfAttribute)
            {
                return new InstantLiteralVisitor(this.finder, errorContext);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }

        if (parameterCardinality == ParameterCardinality.MANY)
        {
            if (attribute instanceof StringAttribute)
            {
                return new StringListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof BooleanAttribute)
            {
                return new BooleanListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof IntegerAttribute)
            {
                return new IntegerListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof LongAttribute)
            {
                return new LongListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DoubleAttribute)
            {
                return new DoubleListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof FloatAttribute)
            {
                return new FloatListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DateAttribute)
            {
                return new LocalDateListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof TimestampAttribute)
            {
                return new InstantListLiteralVisitor(this.finder, errorContext);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }
        throw new AssertionError(parameterCardinality);
    }

    private Operation getUnaryOperation(OperationUnaryOperatorContext ctx, Attribute attribute)
    {
        ReladomoOperationVisitor<Operation> operatorVisitor = this.getUnaryOperatorVisitor(attribute);
        return ctx.unaryOperator().accept(operatorVisitor);
    }

    private Operation getBinaryOperation(
            OperationBinaryOperatorContext ctx,
            Attribute attribute,
            ParameterCardinality parameterCardinality,
            Object parameter)
    {
        ReladomoOperationVisitor<Operation> operatorVisitor = this.getBinaryOperatorVisitor(
                attribute,
                parameterCardinality,
                parameter);
        return ctx.binaryOperator().accept(operatorVisitor);
    }

    private ReladomoOperationVisitor<Operation> getUnaryOperatorVisitor(Attribute attribute)
    {
        if (attribute instanceof AsOfAttribute)
        {
            return new AsOfUnaryOperatorVisitor((AsOfAttribute) attribute);
        }
        return new UnaryOperatorVisitor(attribute);
    }

    private ReladomoOperationVisitor<Operation> getBinaryOperatorVisitor(
            Attribute attribute,
            ParameterCardinality parameterCardinality,
            Object parameter)
    {
        if (parameterCardinality == ParameterCardinality.ONE)
        {
            if (attribute instanceof StringAttribute)
            {
                return new StringBinaryOperatorVisitor((StringAttribute) attribute, (String) parameter);
            }
            if (attribute instanceof BooleanAttribute)
            {
                return new BooleanBinaryOperatorVisitor((BooleanAttribute) attribute, (Boolean) parameter);
            }
            if (attribute instanceof IntegerAttribute)
            {
                return new IntegerBinaryOperatorVisitor((IntegerAttribute) attribute, (Integer) parameter);
            }
            if (attribute instanceof LongAttribute)
            {
                return new LongBinaryOperatorVisitor((LongAttribute) attribute, (Long) parameter);
            }
            if (attribute instanceof DoubleAttribute)
            {
                return new DoubleBinaryOperatorVisitor((DoubleAttribute) attribute, (Double) parameter);
            }
            if (attribute instanceof FloatAttribute)
            {
                return new FloatBinaryOperatorVisitor((FloatAttribute) attribute, (Float) parameter);
            }
            if (attribute instanceof DateAttribute)
            {
                return new LocalDateBinaryOperatorVisitor((DateAttribute) attribute, (LocalDate) parameter);
            }
            if (attribute instanceof TimestampAttribute)
            {
                return new InstantBinaryOperatorVisitor((TimestampAttribute) attribute, (Instant) parameter);
            }
            if (attribute instanceof AsOfAttribute)
            {
                return new TemporalRangeBinaryOperatorVisitor((AsOfAttribute) attribute, (Instant) parameter);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }

        if (parameterCardinality == ParameterCardinality.MANY)
        {
            if (attribute instanceof StringAttribute)
            {
                return new StringListBinaryOperatorVisitor(
                        (StringAttribute) attribute,
                        (ImmutableList<String>) parameter);
            }
            if (attribute instanceof BooleanAttribute)
            {
                return new BooleanListBinaryOperatorVisitor(
                        (BooleanAttribute) attribute,
                        (ImmutableList<Boolean>) parameter);
            }
            if (attribute instanceof IntegerAttribute)
            {
                return new IntegerListBinaryOperatorVisitor(
                        (IntegerAttribute) attribute,
                        (ImmutableList<Integer>) parameter);
            }
            if (attribute instanceof LongAttribute)
            {
                return new LongListBinaryOperatorVisitor(
                        (LongAttribute) attribute,
                        (ImmutableList<Long>) parameter);
            }
            if (attribute instanceof DoubleAttribute)
            {
                return new DoubleListBinaryOperatorVisitor(
                        (DoubleAttribute) attribute,
                        (ImmutableList<Double>) parameter);
            }
            if (attribute instanceof FloatAttribute)
            {
                return new FloatListBinaryOperatorVisitor(
                        (FloatAttribute) attribute,
                        (ImmutableList<Float>) parameter);
            }
            if (attribute instanceof DateAttribute)
            {
                return new LocalDateListBinaryOperatorVisitor(
                        (DateAttribute) attribute,
                        (ImmutableList<LocalDate>) parameter);
            }
            if (attribute instanceof TimestampAttribute || attribute instanceof AsOfAttribute)
            {
                return new InstantListBinaryOperatorVisitor(
                        (TimestampAttribute) attribute,
                        (ImmutableList<Instant>) parameter);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }
        throw new AssertionError(parameterCardinality);
    }

    private String getContextString(ParserRuleContext ctx)
    {
        return this.getContextString(ctx.getStart(), ctx.getStop());
    }

    private String getContextString(Token startToken, Token stopToken)
    {
        List<Token> tokens = this.tokenStream.get(
                startToken.getTokenIndex(),
                stopToken.getTokenIndex());

        return ListAdapter.adapt(tokens)
                .collect(Token::getText)
                .makeString("");
    }
}
