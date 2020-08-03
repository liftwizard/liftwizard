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

package io.liftwizard.model.reladomo.operation.visitor;

import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.AttributeNameContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CharacterListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CharacterLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ClassNameContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CompilationUnitContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.EqualsEdgePointContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ExistsOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionAbsoluteValueContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionDayOfMonthContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionMonthContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToLowerCaseContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToSubstringContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionUnknownContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionYearContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.NavigationContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAllContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAndContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationBinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationExistenceContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationGroupContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationNoneContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationOrContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationUnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorExistsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorGreaterThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNotNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorIsNullContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorLessThanEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotEqContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotExistsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorNotStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorStartsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardInContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorWildCardNotEqualsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ParameterContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.RelationshipNameContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.SimpleAttributeContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.UnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationVisitor;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

// Deliberately not abstract
// Implements every method of ReladomoOperationVisitor by throwing
public class ReladomoOperationThrowingVisitor<T>
        extends AbstractParseTreeVisitor<T>
        implements ReladomoOperationVisitor<T>
{
    @Override
    public T visitCompilationUnit(CompilationUnitContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitCompilationUnit() not implemented yet");
    }

    @Override
    public T visitOperationGroup(OperationGroupContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationGroup() not implemented yet");
    }

    @Override
    public T visitOperationAnd(OperationAndContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationAnd() not implemented yet");
    }

    @Override
    public T visitOperationNone(OperationNoneContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationNone() not implemented yet");
    }

    @Override
    public T visitOperationUnaryOperator(OperationUnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationUnaryOperator() not implemented yet");
    }

    @Override
    public T visitOperationExistence(OperationExistenceContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationExistence() not implemented yet");
    }

    @Override
    public T visitOperationBinaryOperator(OperationBinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationBinaryOperator() not implemented yet");
    }

    @Override
    public T visitOperationAll(OperationAllContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationAll() not implemented yet");
    }

    @Override
    public T visitOperationOr(OperationOrContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperationOr() not implemented yet");
    }

    @Override
    public T visitAttribute(AttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitAttribute() not implemented yet");
    }

    @Override
    public T visitSimpleAttribute(SimpleAttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitSimpleAttribute() not implemented yet");
    }

    @Override
    public T visitNavigation(NavigationContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitNavigation() not implemented yet");
    }

    @Override
    public T visitFunctionToLowerCase(FunctionToLowerCaseContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionToLowerCase() not implemented yet");
    }

    @Override
    public T visitFunctionToSubstring(FunctionToSubstringContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionToSubstring() not implemented yet");
    }

    @Override
    public T visitFunctionAbsoluteValue(FunctionAbsoluteValueContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionAbsoluteValue() not implemented yet");
    }

    @Override
    public T visitFunctionYear(FunctionYearContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionYear() not implemented yet");
    }

    @Override
    public T visitFunctionMonth(FunctionMonthContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionMonth() not implemented yet");
    }

    @Override
    public T visitFunctionDayOfMonth(FunctionDayOfMonthContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionDayOfMonth() not implemented yet");
    }

    @Override
    public T visitFunctionUnknown(FunctionUnknownContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFunctionUnknown() not implemented yet");
    }

    @Override
    public T visitBinaryOperator(BinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitBinaryOperator() not implemented yet");
    }

    @Override
    public T visitOperatorEq(OperatorEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorEq() not implemented yet");
    }

    @Override
    public T visitOperatorNotEq(OperatorNotEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotEq() not implemented yet");
    }

    @Override
    public T visitOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorGreaterThan() not implemented yet");
    }

    @Override
    public T visitOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorGreaterThanEquals() not implemented yet");
    }

    @Override
    public T visitOperatorLessThan(OperatorLessThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorLessThan() not implemented yet");
    }

    @Override
    public T visitOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorLessThanEquals() not implemented yet");
    }

    @Override
    public T visitOperatorIn(OperatorInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorIn() not implemented yet");
    }

    @Override
    public T visitOperatorNotIn(OperatorNotInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotIn() not implemented yet");
    }

    @Override
    public T visitOperatorStartsWith(OperatorStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorStartsWith() not implemented yet");
    }

    @Override
    public T visitOperatorNotStartsWith(OperatorNotStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotStartsWith() not implemented yet");
    }

    @Override
    public T visitOperatorEndsWith(OperatorEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorEndsWith() not implemented yet");
    }

    @Override
    public T visitOperatorNotEndsWith(OperatorNotEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotEndsWith() not implemented yet");
    }

    @Override
    public T visitOperatorContains(OperatorContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorContains() not implemented yet");
    }

    @Override
    public T visitOperatorNotContains(OperatorNotContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotContains() not implemented yet");
    }

    @Override
    public T visitOperatorWildCardEquals(OperatorWildCardEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorWildCardEquals() not implemented yet");
    }

    @Override
    public T visitOperatorWildCardNotEquals(OperatorWildCardNotEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorWildCardNotEquals() not implemented yet");
    }

    @Override
    public T visitOperatorWildCardIn(OperatorWildCardInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorWildCardIn() not implemented yet");
    }

    @Override
    public T visitUnaryOperator(UnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitUnaryOperator() not implemented yet");
    }

    @Override
    public T visitOperatorIsNull(OperatorIsNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorIsNull() not implemented yet");
    }

    @Override
    public T visitOperatorIsNotNull(OperatorIsNotNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorIsNotNull() not implemented yet");
    }

    @Override
    public T visitEqualsEdgePoint(EqualsEdgePointContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitEqualsEdgePoint() not implemented yet");
    }

    @Override
    public T visitExistsOperator(ExistsOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitExistsOperator() not implemented yet");
    }

    @Override
    public T visitOperatorExists(OperatorExistsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorExists() not implemented yet");
    }

    @Override
    public T visitOperatorNotExists(OperatorNotExistsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitOperatorNotExists() not implemented yet");
    }

    @Override
    public T visitParameter(ParameterContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitParameter() not implemented yet");
    }

    @Override
    public T visitStringLiteral(StringLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitStringLiteral() not implemented yet");
    }

    @Override
    public T visitBooleanLiteral(BooleanLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitBooleanLiteral() not implemented yet");
    }

    @Override
    public T visitCharacterLiteral(CharacterLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitCharacterLiteral() not implemented yet");
    }

    @Override
    public T visitIntegerLiteral(IntegerLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitIntegerLiteral() not implemented yet");
    }

    @Override
    public T visitFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFloatingPointLiteral() not implemented yet");
    }

    @Override
    public T visitStringListLiteral(StringListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitStringListLiteral() not implemented yet");
    }

    @Override
    public T visitBooleanListLiteral(BooleanListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitBooleanListLiteral() not implemented yet");
    }

    @Override
    public T visitCharacterListLiteral(CharacterListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitCharacterListLiteral() not implemented yet");
    }

    @Override
    public T visitIntegerListLiteral(IntegerListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitIntegerListLiteral() not implemented yet");
    }

    @Override
    public T visitFloatingPointListLiteral(FloatingPointListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitFloatingPointListLiteral() not implemented yet");
    }

    @Override
    public T visitClassName(ClassNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitClassName() not implemented yet");
    }

    @Override
    public T visitRelationshipName(RelationshipNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitRelationshipName() not implemented yet");
    }

    @Override
    public T visitAttributeName(AttributeNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitAttributeName() not implemented yet");
    }
}
