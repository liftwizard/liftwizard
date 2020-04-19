package com.liftwizard.dropwizard.configuration.ddl.executor;

import java.util.List;

public interface DdlExecutorFactoryProvider
{
    List<DdlExecutorFactory> getDdlExecutorFactories();
}
