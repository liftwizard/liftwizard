package io.liftwizard.model.reladomo.operation.compiler.literal.many;

import java.time.LocalDate;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.ReladomoOperationParser.StringListLiteralContext;
import io.liftwizard.model.reladomo.operation.compiler.literal.AbstractLiteralVisitor;
import io.liftwizard.model.reladomo.operation.compiler.literal.one.LocalDateLiteralVisitor;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class LocalDateListLiteralVisitor extends AbstractLiteralVisitor<ImmutableList<LocalDate>>
{
    private final LocalDateLiteralVisitor localDateLiteralVisitor;

    public LocalDateListLiteralVisitor(RelatedFinder finder, String errorContext)
    {
        super(finder, errorContext);
        this.localDateLiteralVisitor = new LocalDateLiteralVisitor(this.finder, this.errorContext);
    }

    @Override
    protected String getExpectedType()
    {
        return "LocalDate list";
    }

    @Override
    public ImmutableList<LocalDate> visitStringListLiteral(StringListLiteralContext ctx)
    {
        return ListAdapter.adapt(ctx.stringLiteral())
                .collect(each -> each.accept(this.localDateLiteralVisitor))
                .toImmutable();
    }
}
