package io.liftwizard.model.reladomo.operation.compiler;

import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardNotEqualsContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;

public class ParameterCardinalityVisitor
        extends ReladomoOperationThrowingVisitor<ParameterCardinality>
{
    @Override
    public ParameterCardinality visitBinaryOperator(BinaryOperatorContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public ParameterCardinality visitOperatorEq(OperatorEqContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorNotEq(OperatorNotEqContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorLessThan(OperatorLessThanContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorIn(OperatorInContext ctx)
    {
        return ParameterCardinality.MANY;
    }

    @Override
    public ParameterCardinality visitOperatorNotIn(OperatorNotInContext ctx)
    {
        return ParameterCardinality.MANY;
    }

    @Override
    public ParameterCardinality visitOperatorStartsWith(OperatorStartsWithContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorNotStartsWith(OperatorNotStartsWithContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorEndsWith(OperatorEndsWithContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorNotEndsWith(OperatorNotEndsWithContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorContains(OperatorContainsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorNotContains(OperatorNotContainsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorWildCardEquals(OperatorWildCardEqualsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorWildCardNotEquals(OperatorWildCardNotEqualsContext ctx)
    {
        return ParameterCardinality.ONE;
    }

    @Override
    public ParameterCardinality visitOperatorWildCardIn(OperatorWildCardInContext ctx)
    {
        return ParameterCardinality.ONE;
    }
}
