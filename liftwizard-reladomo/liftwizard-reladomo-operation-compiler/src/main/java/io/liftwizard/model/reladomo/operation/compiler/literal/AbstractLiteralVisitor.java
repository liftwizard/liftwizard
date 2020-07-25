package io.liftwizard.model.reladomo.operation.compiler.literal;

import java.util.Objects;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.BooleanLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.CharacterLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.FloatingPointLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.IntegerLiteralContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.ParameterContext;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringLiteralContext;
import io.liftwizard.model.reladomo.operation.visitor.ReladomoOperationThrowingVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

public abstract class AbstractLiteralVisitor<T> extends ReladomoOperationThrowingVisitor<T>
{
    protected final RelatedFinder finder;
    protected final String        errorContext;

    public AbstractLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        this.finder       = Objects.requireNonNull(finder);
        this.errorContext = Objects.requireNonNull(errorContext);
    }

    @Override
    public T visitParameter(ParameterContext ctx)
    {
        return this.visitChildren(ctx);
    }

    @Override
    public T visitStringLiteral(StringLiteralContext ctx)
    {
        return this.throwTypeError(ctx);
    }

    @Override
    public T visitBooleanLiteral(BooleanLiteralContext ctx)
    {
        return this.throwTypeError(ctx);
    }

    @Override
    public T visitCharacterLiteral(CharacterLiteralContext ctx)
    {
        return this.throwTypeError(ctx);
    }

    @Override
    public T visitIntegerLiteral(IntegerLiteralContext ctx)
    {
        return this.throwTypeError(ctx);
    }

    @Override
    public T visitFloatingPointLiteral(FloatingPointLiteralContext ctx)
    {
        return this.throwTypeError(ctx);
    }

    protected T throwTypeError(ParserRuleContext ctx)
    {
        var error = String.format(
                "Expected <" + this.getExpectedType() + "> but found: <%s> in %s",
                ctx.getText(),
                this.errorContext);
        throw new IllegalArgumentException(error);
    }

    protected abstract String getExpectedType();
}
