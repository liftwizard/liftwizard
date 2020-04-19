package com.liftwizard.dropwizard.configuration.graphql;

import com.smoketurner.dropwizard.graphql.GraphQLFactory;

public interface GraphQLFactoryProvider
{
    GraphQLFactory getGraphQLFactory();
}
