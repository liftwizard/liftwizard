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

package io.liftwizard.model.reladomo.operation.listener;

import io.liftwizard.model.reladomo.operation.ReladomoOperationListener;
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
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionAbsoluteValueContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToLowerCaseContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionToSubstringContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FunctionUnknownContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerListLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAllContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationAndContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationBinaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationGroupContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationNoneContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationOrContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperationUnaryOperatorContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorContainsContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEndsWithContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.OperatorEqContext;
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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

// Deliberately not abstract
// Implements every method of ReladomoOperationListener by throwing
public class ReladomoOperationThrowingListener implements ReladomoOperationListener
{
    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterCompilationUnit() not implemented yet");
    }

    @Override
    public void exitCompilationUnit(CompilationUnitContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitCompilationUnit() not implemented yet");
    }

    @Override
    public void enterOperationAnd(OperationAndContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationAnd() not implemented yet");
    }

    @Override
    public void exitOperationAnd(OperationAndContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationAnd() not implemented yet");
    }

    @Override
    public void enterOperationOr(OperationOrContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationOr() not implemented yet");
    }

    @Override
    public void exitOperationOr(OperationOrContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationOr() not implemented yet");
    }

    @Override
    public void enterOperationGroup(OperationGroupContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationGroup() not implemented yet");
    }

    @Override
    public void exitOperationGroup(OperationGroupContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationGroup() not implemented yet");
    }

    @Override
    public void enterOperationAll(OperationAllContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationAll() not implemented yet");
    }

    @Override
    public void exitOperationAll(OperationAllContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationAll() not implemented yet");
    }

    @Override
    public void enterOperationNone(OperationNoneContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationNone() not implemented yet");
    }

    @Override
    public void exitOperationNone(OperationNoneContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationNone() not implemented yet");
    }

    @Override
    public void enterOperationUnaryOperator(OperationUnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationUnaryOperator() not implemented yet");
    }

    @Override
    public void exitOperationUnaryOperator(OperationUnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationUnaryOperator() not implemented yet");
    }

    @Override
    public void enterOperationBinaryOperator(OperationBinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperationBinaryOperator() not implemented yet");
    }

    @Override
    public void exitOperationBinaryOperator(OperationBinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperationBinaryOperator() not implemented yet");
    }

    @Override
    public void enterBinaryOperator(BinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterBinaryOperator() not implemented yet");
    }

    @Override
    public void exitBinaryOperator(BinaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitBinaryOperator() not implemented yet");
    }

    @Override
    public void enterUnaryOperator(UnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterUnaryOperator() not implemented yet");
    }

    @Override
    public void exitUnaryOperator(UnaryOperatorContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitUnaryOperator() not implemented yet");
    }

    @Override
    public void enterEqualsEdgePoint(EqualsEdgePointContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterEqualsEdgePoint() not implemented yet");
    }

    @Override
    public void exitEqualsEdgePoint(EqualsEdgePointContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitEqualsEdgePoint() not implemented yet");
    }

    @Override
    public void enterFunctionToLowerCase(FunctionToLowerCaseContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFunctionToLowerCase() not implemented yet");
    }

    @Override
    public void exitFunctionToLowerCase(FunctionToLowerCaseContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFunctionToLowerCase() not implemented yet");
    }

    @Override
    public void enterFunctionToSubstring(FunctionToSubstringContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFunctionToSubstring() not implemented yet");
    }

    @Override
    public void exitFunctionToSubstring(FunctionToSubstringContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFunctionToSubstring() not implemented yet");
    }

    @Override
    public void enterFunctionAbsoluteValue(FunctionAbsoluteValueContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFunctionAbsoluteValue() not implemented yet");
    }

    @Override
    public void exitFunctionAbsoluteValue(FunctionAbsoluteValueContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFunctionAbsoluteValue() not implemented yet");
    }

    @Override
    public void enterFunctionUnknown(FunctionUnknownContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFunctionUnknown() not implemented yet");
    }

    @Override
    public void exitFunctionUnknown(FunctionUnknownContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFunctionUnknown() not implemented yet");
    }

    @Override
    public void enterSimpleAttribute(SimpleAttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterSimpleAttribute() not implemented yet");
    }

    @Override
    public void exitSimpleAttribute(SimpleAttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitSimpleAttribute() not implemented yet");
    }

    @Override
    public void enterAttribute(AttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterAttribute() not implemented yet");
    }

    @Override
    public void exitAttribute(AttributeContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitAttribute() not implemented yet");
    }

    @Override
    public void enterClassName(ClassNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterClassName() not implemented yet");
    }

    @Override
    public void exitClassName(ClassNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitClassName() not implemented yet");
    }

    @Override
    public void enterRelationshipName(RelationshipNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterRelationshipName() not implemented yet");
    }

    @Override
    public void exitRelationshipName(RelationshipNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitRelationshipName() not implemented yet");
    }

    @Override
    public void enterAttributeName(AttributeNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterAttributeName() not implemented yet");
    }

    @Override
    public void exitAttributeName(AttributeNameContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitAttributeName() not implemented yet");
    }

    @Override
    public void enterParameter(ParameterContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterParameter() not implemented yet");
    }

    @Override
    public void exitParameter(ParameterContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitParameter() not implemented yet");
    }

    @Override
    public void visitTerminal(TerminalNode node)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitTerminal() not implemented yet");
    }

    @Override
    public void visitErrorNode(ErrorNode node)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".visitErrorNode() not implemented yet");
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterEveryRule() not implemented yet");
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitEveryRule() not implemented yet");
    }

    @Override
    public void enterStringLiteral(StringLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterStringLiteral() not implemented yet");
    }

    @Override
    public void exitStringLiteral(StringLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitStringLiteral() not implemented yet");
    }

    @Override
    public void enterBooleanLiteral(BooleanLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterBooleanLiteral() not implemented yet");
    }

    @Override
    public void exitBooleanLiteral(BooleanLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitBooleanLiteral() not implemented yet");
    }

    @Override
    public void enterCharacterLiteral(CharacterLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterCharacterLiteral() not implemented yet");
    }

    @Override
    public void exitCharacterLiteral(CharacterLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitCharacterLiteral() not implemented yet");
    }

    @Override
    public void enterIntegerLiteral(IntegerLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterIntegerLiteral() not implemented yet");
    }

    @Override
    public void exitIntegerLiteral(IntegerLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitIntegerLiteral() not implemented yet");
    }

    @Override
    public void enterFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFloatingPointLiteral() not implemented yet");
    }

    @Override
    public void exitFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFloatingPointLiteral() not implemented yet");
    }

    @Override
    public void enterStringListLiteral(StringListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterStringListLiteral() not implemented yet");
    }

    @Override
    public void exitStringListLiteral(StringListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitStringListLiteral() not implemented yet");
    }

    @Override
    public void enterBooleanListLiteral(BooleanListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterBooleanListLiteral() not implemented yet");
    }

    @Override
    public void exitBooleanListLiteral(BooleanListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitBooleanListLiteral() not implemented yet");
    }

    @Override
    public void enterCharacterListLiteral(CharacterListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterCharacterListLiteral() not implemented yet");
    }

    @Override
    public void exitCharacterListLiteral(CharacterListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitCharacterListLiteral() not implemented yet");
    }

    @Override
    public void enterIntegerListLiteral(IntegerListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterIntegerListLiteral() not implemented yet");
    }

    @Override
    public void exitIntegerListLiteral(IntegerListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitIntegerListLiteral() not implemented yet");
    }

    @Override
    public void enterFloatingPointListLiteral(FloatingPointListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterFloatingPointListLiteral() not implemented yet");
    }

    @Override
    public void exitFloatingPointListLiteral(FloatingPointListLiteralContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitFloatingPointListLiteral() not implemented yet");
    }

    @Override
    public void enterOperatorEq(OperatorEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorEq() not implemented yet");
    }

    @Override
    public void enterOperatorIsNull(OperatorIsNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorIsNull() not implemented yet");
    }

    @Override
    public void exitOperatorIsNull(OperatorIsNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorIsNull() not implemented yet");
    }

    @Override
    public void enterOperatorIsNotNull(OperatorIsNotNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorIsNotNull() not implemented yet");
    }

    @Override
    public void exitOperatorIsNotNull(OperatorIsNotNullContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorIsNotNull() not implemented yet");
    }

    @Override
    public void exitOperatorEq(OperatorEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorEq() not implemented yet");
    }

    @Override
    public void enterOperatorNotEq(OperatorNotEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorNotEq() not implemented yet");
    }

    @Override
    public void exitOperatorNotEq(OperatorNotEqContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorNotEq() not implemented yet");
    }

    @Override
    public void enterOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorGreaterThan() not implemented yet");
    }

    @Override
    public void exitOperatorGreaterThan(OperatorGreaterThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorGreaterThan() not implemented yet");
    }

    @Override
    public void enterOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorGreaterThanEquals() not implemented yet");
    }

    @Override
    public void exitOperatorGreaterThanEquals(OperatorGreaterThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorGreaterThanEquals() not implemented yet");
    }

    @Override
    public void enterOperatorLessThan(OperatorLessThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorLessThan() not implemented yet");
    }

    @Override
    public void exitOperatorLessThan(OperatorLessThanContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorLessThan() not implemented yet");
    }

    @Override
    public void enterOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorLessThanEquals() not implemented yet");
    }

    @Override
    public void exitOperatorLessThanEquals(OperatorLessThanEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorLessThanEquals() not implemented yet");
    }

    @Override
    public void enterOperatorIn(OperatorInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorIn() not implemented yet");
    }

    @Override
    public void exitOperatorIn(OperatorInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorIn() not implemented yet");
    }

    @Override
    public void enterOperatorNotIn(OperatorNotInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorNotIn() not implemented yet");
    }

    @Override
    public void exitOperatorNotIn(OperatorNotInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorNotIn() not implemented yet");
    }

    @Override
    public void enterOperatorStartsWith(OperatorStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorStartsWith() not implemented yet");
    }

    @Override
    public void exitOperatorStartsWith(OperatorStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorStartsWith() not implemented yet");
    }

    @Override
    public void enterOperatorNotStartsWith(OperatorNotStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorNotStartsWith() not implemented yet");
    }

    @Override
    public void exitOperatorNotStartsWith(OperatorNotStartsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorNotStartsWith() not implemented yet");
    }

    @Override
    public void enterOperatorEndsWith(OperatorEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorEndsWith() not implemented yet");
    }

    @Override
    public void exitOperatorEndsWith(OperatorEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorEndsWith() not implemented yet");
    }

    @Override
    public void enterOperatorNotEndsWith(OperatorNotEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorNotEndsWith() not implemented yet");
    }

    @Override
    public void exitOperatorNotEndsWith(OperatorNotEndsWithContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorNotEndsWith() not implemented yet");
    }

    @Override
    public void enterOperatorNotContains(OperatorNotContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorNotContains() not implemented yet");
    }

    @Override
    public void exitOperatorNotContains(OperatorNotContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorNotContains() not implemented yet");
    }

    @Override
    public void enterOperatorWildCardEquals(OperatorWildCardEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorWildCardEquals() not implemented yet");
    }

    @Override
    public void exitOperatorWildCardEquals(OperatorWildCardEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorWildCardEquals() not implemented yet");
    }

    @Override
    public void enterOperatorWildCardNotEquals(OperatorWildCardNotEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorWildCardNotEquals() not implemented yet");
    }

    @Override
    public void exitOperatorWildCardNotEquals(OperatorWildCardNotEqualsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorWildCardNotEquals() not implemented yet");
    }

    @Override
    public void enterOperatorWildCardIn(OperatorWildCardInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorWildCardIn() not implemented yet");
    }

    @Override
    public void exitOperatorWildCardIn(OperatorWildCardInContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorWildCardIn() not implemented yet");
    }

    @Override
    public void enterOperatorContains(OperatorContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".enterOperatorContains() not implemented yet");
    }

    @Override
    public void exitOperatorContains(OperatorContainsContext ctx)
    {
        throw new UnsupportedOperationException(this.getClass().getSimpleName()
                + ".exitOperatorContains() not implemented yet");
    }
}
