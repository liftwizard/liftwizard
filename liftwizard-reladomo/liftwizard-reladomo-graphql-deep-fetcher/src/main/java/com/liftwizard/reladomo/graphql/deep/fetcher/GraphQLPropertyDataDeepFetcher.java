package com.liftwizard.reladomo.graphql.deep.fetcher;

import java.util.Objects;
import java.util.function.Function;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.finder.DomainList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class GraphQLPropertyDataDeepFetcher<Output>
        implements DataFetcher<DomainList<Output>>
{
    private final Function<Object, DomainList<Output>> function;
    private final RelatedFinder<Output>                finderInstance;

    public <Input> GraphQLPropertyDataDeepFetcher(
            Function<Input, DomainList<Output>> function,
            RelatedFinder<Output> finderInstance)
    {
        this.function       = (Function<Object, DomainList<Output>>) Objects.requireNonNull(function);
        this.finderInstance = Objects.requireNonNull(finderInstance);
    }

    @Override
    public DomainList<Output> get(DataFetchingEnvironment environment)
    {
        Object source = environment.getSource();
        if (source == null)
        {
            return null;
        }

        DomainList<Output> domainList = this.function.apply(source);
        GraphQLDeepFetcher.deepFetch(domainList, this.finderInstance, environment.getSelectionSet());
        return domainList;
    }
}
