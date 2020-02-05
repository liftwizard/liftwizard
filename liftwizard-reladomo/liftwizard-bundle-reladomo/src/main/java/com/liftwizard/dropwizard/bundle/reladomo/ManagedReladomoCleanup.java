package com.liftwizard.dropwizard.bundle.reladomo;

import com.gs.fw.common.mithra.MithraManagerProvider;
import io.dropwizard.lifecycle.Managed;

public class ManagedReladomoCleanup
        implements Managed
{
    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
        // TODO: Figure out which of these cleanups are necessary and which can be deleted
        MithraManagerProvider.getMithraManager().clearAllQueryCaches();
        MithraManagerProvider.getMithraManager().cleanUpPrimaryKeyGenerators();
        MithraManagerProvider.getMithraManager().cleanUpRuntimeCacheControllers();
        MithraManagerProvider.getMithraManager().getConfigManager().resetAllInitializedClasses();
    }

    @Override
    public String toString()
    {
        return ManagedReladomoCleanup.class.getSimpleName();
    }
}
