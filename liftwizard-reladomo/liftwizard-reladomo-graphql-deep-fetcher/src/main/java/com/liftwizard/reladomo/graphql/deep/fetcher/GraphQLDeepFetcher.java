package com.liftwizard.reladomo.graphql.deep.fetcher;

import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.finder.DomainList;
import com.gs.fw.finder.Navigation;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

public final class GraphQLDeepFetcher
{
    private GraphQLDeepFetcher()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static <T> void deepFetch(
            DomainList<T> result,
            RelatedFinder<T> finderInstance,
            DataFetchingFieldSelectionSet selectionSet)
    {
        for (SelectedField selectedField : selectionSet.getFields())
        {
            GraphQLDeepFetcher.deepFetchSelectedField(result, finderInstance, selectedField);
        }
    }

    private static <T> void deepFetchSelectedField(
            DomainList<T> result,
            RelatedFinder<T> finderInstance,
            SelectedField selectedField)
    {
        String              qualifiedName   = selectedField.getQualifiedName();
        MutableList<String> fieldNamesNames = ArrayAdapter.adapt(qualifiedName.split("/"));
        MutableList<String> navigationNames = fieldNamesNames.take(fieldNamesNames.size() - 1);
        if (navigationNames.isEmpty())
        {
            return;
        }

        RelatedFinder<T> currentFinder = finderInstance;
        for (String navigationName : navigationNames)
        {
            currentFinder = currentFinder.getRelationshipFinderByName(navigationName);
        }
        Navigation<T> navigation = (Navigation<T>) currentFinder;
        result.deepFetch(navigation);
    }
}
