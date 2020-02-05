package com.liftwizard.dropwizard.task.reladomo.clear.cache;

import java.io.PrintWriter;

import com.google.common.collect.ImmutableMultimap;
import com.gs.fw.common.mithra.MithraManager;
import com.gs.fw.common.mithra.MithraManagerProvider;
import io.dropwizard.servlets.tasks.Task;

public class ReladomoClearCacheTask extends Task
{
    public ReladomoClearCacheTask()
    {
        super("reladomo-clear-cache");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
    {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.clearAllQueryCaches();
    }
}
