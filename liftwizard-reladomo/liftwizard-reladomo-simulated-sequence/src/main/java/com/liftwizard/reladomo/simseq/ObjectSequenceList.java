package com.liftwizard.reladomo.simseq;

import java.util.Collection;

import com.gs.fw.finder.Operation;

public class ObjectSequenceList extends ObjectSequenceListAbstract
{
    public ObjectSequenceList()
    {
    }

    public ObjectSequenceList(int initialSize)
    {
        super(initialSize);
    }

    public ObjectSequenceList(Collection<?> collection)
    {
        super(collection);
    }

    public ObjectSequenceList(Operation operation)
    {
        super(operation);
    }
}
