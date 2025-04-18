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
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.None;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CompilationUnitContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ExistsOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.NavigationContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAllContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAndContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationBinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationExistenceContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationGroupContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationNoneContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationOrContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationUnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.UnaryOperatorContext;
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
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class ReladomoOperationBuilderVisitor<T> extends ReladomoOperationThrowingVisitor<Operation> {

    private final RelatedFinder<T> finder;
    private final CommonTokenStream tokenStream;

    public ReladomoOperationBuilderVisitor(RelatedFinder<T> finder, CommonTokenStream tokenStream) {
        this.finder = Objects.requireNonNull(finder);
        this.tokenStream = Objects.requireNonNull(tokenStream);
    }

    @Override
    public Operation visitCompilationUnit(CompilationUnitContext ctx) {
        return ctx.compositeOperation().accept(this);
    }

    @Override
    public Operation visitOperationNone(OperationNoneContext ctx) {
        // There's no method this.finder.none()
        // But this is similar to the internal implementation of all()
        return this.getNone();
    }

    @Override
    public Operation visitOperationAll(OperationAllContext ctx) {
        return this.finder.all();
    }

    @Override
    public Operation visitOperationAnd(OperationAndContext ctx) {
        return ListAdapter.adapt(ctx.compositeOperation())
            .collect(this::visit)
            .injectInto(this.finder.all(), Operation::and);
    }

    @Override
    public Operation visitOperationOr(OperationOrContext ctx) {
        return ListAdapter.adapt(ctx.compositeOperation())
            .collect(this::visit)
            .injectInto(this.getNone(), Operation::or);
    }

    @Override
    public Operation visitOperationGroup(OperationGroupContext ctx) {
        return this.visit(ctx.compositeOperation());
    }

    @Override
    public Operation visitOperationUnaryOperator(OperationUnaryOperatorContext ctx) {
        String contextString = this.getContextString(ctx);
        Attribute attribute = this.getAttribute(ctx.attribute(), contextString);
        return this.getUnaryOperation(ctx.unaryOperator(), attribute);
    }

    @Override
    public Operation visitOperationBinaryOperator(OperationBinaryOperatorContext ctx) {
        String contextString = this.getContextString(ctx);
        Attribute attribute = this.getAttribute(ctx.attribute(), contextString);
        ParameterCardinality parameterCardinality = this.getParameterCardinality(ctx);
        Object parameter = this.getParameter(ctx, attribute, parameterCardinality, contextString);
        return this.getBinaryOperation(ctx.binaryOperator(), attribute, parameterCardinality, parameter);
    }

    @Override
    public Operation visitOperationExistence(OperationExistenceContext ctx) {
        String contextString = this.getContextString(ctx);
        AbstractRelatedFinder navigation = this.getNavigation(ctx.navigation(), contextString);
        RelatedFinder relatedFinder = navigation.getMithraObjectPortal().getFinder();
        Operation notExistsOperation = this.getNotExistsOperation(ctx, relatedFinder);
        ExistsOperatorContext existsOperatorContext = ctx.existsOperator();
        return existsOperatorContext.accept(new ReladomoExistsOperatorVisitor(navigation, notExistsOperation));
    }

    private <T2> Operation getNotExistsOperation(OperationExistenceContext ctx, RelatedFinder<T2> relatedFinder) {
        if (ctx.notExistsOperation == null) {
            return null;
        }

        String notExistsOperationText = ctx.notExistsOperation.getText();
        var compiler = new ReladomoOperationCompiler();
        return compiler.compile(relatedFinder, notExistsOperationText);
    }

    private AbstractRelatedFinder getNavigation(NavigationContext ctx, String errorContext) {
        if (
            ctx.className() != null &&
            !Objects.equals(ctx.className().getText(), this.getExpectedClassName(this.finder))
        ) {
            String error =
                "Expected 'this' or <%s> but found: <%s> in %s".formatted(
                        this.getExpectedClassName(this.finder),
                        ctx.className().getText(),
                        errorContext
                    );
            throw new IllegalArgumentException(error);
        }

        RelatedFinder currentFinder = this.finder;
        MutableList<String> relationshipNames = ListAdapter.adapt(ctx.relationshipName()).collect(RuleContext::getText);
        for (String relationshipName : relationshipNames) {
            RelatedFinder nextFinder = currentFinder.getRelationshipFinderByName(relationshipName);
            if (nextFinder == null) {
                String error =
                    "Could not find relationship '%s' on type '%s' in %s".formatted(
                            relationshipName,
                            this.getExpectedClassName(currentFinder),
                            errorContext
                        );
                throw new IllegalArgumentException(error);
            }
            currentFinder = nextFinder;
        }

        return (AbstractRelatedFinder) currentFinder;
    }

    public String getExpectedClassName(RelatedFinder relatedFinder) {
        return relatedFinder.getMithraObjectPortal().getClassMetaData().getBusinessOrInterfaceClass().getSimpleName();
    }

    private ParameterCardinality getParameterCardinality(OperationBinaryOperatorContext ctx) {
        ParseTreeVisitor<ParameterCardinality> operatorVisitor = new ParameterCardinalityVisitor();
        return ctx.binaryOperator().accept(operatorVisitor);
    }

    private Attribute getAttribute(AttributeContext attributeContext, String errorContext) {
        var attributeVisitor = new ReladomoAttributeVisitor(this.finder, errorContext);
        return attributeContext.accept(attributeVisitor);
    }

    private Object getParameter(
        OperationBinaryOperatorContext ctx,
        Attribute attribute,
        ParameterCardinality parameterCardinality,
        String errorContext
    ) {
        ReladomoOperationVisitor<?> parameterVisitor =
            this.getParameterVisitor(attribute, parameterCardinality, errorContext);
        return ctx.parameter().accept(parameterVisitor);
    }

    private ReladomoOperationVisitor<?> getParameterVisitor(
        Attribute attribute,
        ParameterCardinality parameterCardinality,
        String errorContext
    ) {
        if (parameterCardinality == ParameterCardinality.ONE) {
            if (attribute instanceof StringAttribute) {
                return new StringLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof BooleanAttribute) {
                return new BooleanLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof IntegerAttribute) {
                return new IntegerLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof LongAttribute) {
                return new LongLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DoubleAttribute) {
                return new DoubleLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof FloatAttribute) {
                return new FloatLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DateAttribute) {
                return new LocalDateLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof TimestampAttribute || attribute instanceof AsOfAttribute) {
                return new InstantLiteralVisitor(this.finder, errorContext);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }

        if (parameterCardinality == ParameterCardinality.MANY) {
            if (attribute instanceof StringAttribute) {
                return new StringListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof BooleanAttribute) {
                return new BooleanListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof IntegerAttribute) {
                return new IntegerListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof LongAttribute) {
                return new LongListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DoubleAttribute) {
                return new DoubleListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof FloatAttribute) {
                return new FloatListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof DateAttribute) {
                return new LocalDateListLiteralVisitor(this.finder, errorContext);
            }
            if (attribute instanceof TimestampAttribute) {
                return new InstantListLiteralVisitor(this.finder, errorContext);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }
        throw new AssertionError(parameterCardinality);
    }

    private Operation getUnaryOperation(UnaryOperatorContext unaryOperatorContext, Attribute attribute) {
        ReladomoOperationVisitor<Operation> operatorVisitor = this.getUnaryOperatorVisitor(attribute);
        return unaryOperatorContext.accept(operatorVisitor);
    }

    private Operation getBinaryOperation(
        BinaryOperatorContext binaryOperatorContext,
        Attribute attribute,
        ParameterCardinality parameterCardinality,
        Object parameter
    ) {
        ReladomoOperationVisitor<Operation> operatorVisitor =
            this.getBinaryOperatorVisitor(attribute, parameterCardinality, parameter);
        return binaryOperatorContext.accept(operatorVisitor);
    }

    private ReladomoOperationVisitor<Operation> getUnaryOperatorVisitor(Attribute attribute) {
        return attribute instanceof AsOfAttribute asOfAttribute
            ? new AsOfUnaryOperatorVisitor(asOfAttribute)
            : new UnaryOperatorVisitor(attribute);
    }

    private ReladomoOperationVisitor<Operation> getBinaryOperatorVisitor(
        Attribute attribute,
        ParameterCardinality parameterCardinality,
        Object parameter
    ) {
        if (parameterCardinality == ParameterCardinality.ONE) {
            if (attribute instanceof StringAttribute stringAttribute) {
                return new StringBinaryOperatorVisitor(stringAttribute, (String) parameter);
            }
            if (attribute instanceof BooleanAttribute booleanAttribute) {
                return new BooleanBinaryOperatorVisitor(booleanAttribute, (Boolean) parameter);
            }
            if (attribute instanceof IntegerAttribute integerAttribute) {
                return new IntegerBinaryOperatorVisitor(integerAttribute, (Integer) parameter);
            }
            if (attribute instanceof LongAttribute longAttribute) {
                return new LongBinaryOperatorVisitor(longAttribute, (Long) parameter);
            }
            if (attribute instanceof DoubleAttribute doubleAttribute) {
                return new DoubleBinaryOperatorVisitor(doubleAttribute, (Double) parameter);
            }
            if (attribute instanceof FloatAttribute floatAttribute) {
                return new FloatBinaryOperatorVisitor(floatAttribute, (Float) parameter);
            }
            if (attribute instanceof DateAttribute dateAttribute) {
                return new LocalDateBinaryOperatorVisitor(dateAttribute, (LocalDate) parameter);
            }
            if (attribute instanceof TimestampAttribute timestampAttribute) {
                return new InstantBinaryOperatorVisitor(timestampAttribute, (Instant) parameter);
            }
            if (attribute instanceof AsOfAttribute asOfAttribute) {
                return new TemporalRangeBinaryOperatorVisitor(asOfAttribute, (Instant) parameter);
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }

        if (parameterCardinality == ParameterCardinality.MANY) {
            if (attribute instanceof StringAttribute stringAttribute) {
                return new StringListBinaryOperatorVisitor(stringAttribute, (ImmutableList<String>) parameter);
            }
            if (attribute instanceof BooleanAttribute booleanAttribute) {
                return new BooleanListBinaryOperatorVisitor(booleanAttribute, (ImmutableList<Boolean>) parameter);
            }
            if (attribute instanceof IntegerAttribute integerAttribute) {
                return new IntegerListBinaryOperatorVisitor(integerAttribute, (ImmutableList<Integer>) parameter);
            }
            if (attribute instanceof LongAttribute longAttribute) {
                return new LongListBinaryOperatorVisitor(longAttribute, (ImmutableList<Long>) parameter);
            }
            if (attribute instanceof DoubleAttribute doubleAttribute) {
                return new DoubleListBinaryOperatorVisitor(doubleAttribute, (ImmutableList<Double>) parameter);
            }
            if (attribute instanceof FloatAttribute floatAttribute) {
                return new FloatListBinaryOperatorVisitor(floatAttribute, (ImmutableList<Float>) parameter);
            }
            if (attribute instanceof DateAttribute dateAttribute) {
                return new LocalDateListBinaryOperatorVisitor(dateAttribute, (ImmutableList<LocalDate>) parameter);
            }
            if (attribute instanceof TimestampAttribute) {
                return new InstantListBinaryOperatorVisitor(
                    (TimestampAttribute) attribute,
                    (ImmutableList<Instant>) parameter
                );
            }
            if (attribute instanceof AsOfAttribute) {
                throw new AssertionError("AsOfAttribute should not be used with a list of parameters");
            }
            throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
        }
        throw new AssertionError(parameterCardinality);
    }

    private String getContextString(ParserRuleContext ctx) {
        return this.getContextString(ctx.getStart(), ctx.getStop());
    }

    private String getContextString(Token startToken, Token stopToken) {
        List<Token> tokens = this.tokenStream.get(startToken.getTokenIndex(), stopToken.getTokenIndex());

        return ListAdapter.adapt(tokens).collect(Token::getText).makeString("");
    }

    private None getNone() {
        return new None(this.finder.getPrimaryKeyAttributes()[0]);
    }
}
