package com.liftwizard.reladomo.simseq;

import com.gs.fw.common.mithra.MithraSequence;
import com.gs.fw.common.mithra.MithraSequenceObjectFactory;

public class ObjectSequenceObjectFactory implements MithraSequenceObjectFactory
{
    @Override
    public MithraSequence getMithraSequenceObject(String sequenceName, Object sourceAttribute, int initialValue)
    {
        ObjectSequence objectSequence = ObjectSequenceFinder.findByPrimaryKey(sequenceName);
        if (objectSequence != null)
        {
            return objectSequence;
        }

        ObjectSequence newObjectSequence = new ObjectSequence();
        newObjectSequence.setSequenceName(sequenceName);
        newObjectSequence.setNextId(initialValue);
        newObjectSequence.insert();
        return newObjectSequence;
    }
}
