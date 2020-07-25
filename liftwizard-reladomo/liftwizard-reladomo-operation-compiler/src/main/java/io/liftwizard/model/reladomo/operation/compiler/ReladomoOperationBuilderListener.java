package io.liftwizard.model.reladomo.operation.compiler;

import java.util.Objects;

import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import io.liftwizard.model.reladomo.operation.listener.ReladomoOperationThrowingListener;

public class ReladomoOperationBuilderListener extends ReladomoOperationThrowingListener
{
    private final RelatedFinder finder;

    private Operation operation;

    public ReladomoOperationBuilderListener(RelatedFinder finder)
    {
        this.finder    = Objects.requireNonNull(finder);
        this.operation = finder.all();
    }

    public Operation getOperation()
    {
        return this.operation;
    }
}
