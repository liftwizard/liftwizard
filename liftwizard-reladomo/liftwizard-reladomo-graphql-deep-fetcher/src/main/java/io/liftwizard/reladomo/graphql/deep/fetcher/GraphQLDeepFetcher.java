/*
 * Copyright 2020 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.reladomo.graphql.deep.fetcher;

import java.util.Objects;

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
        MutableList<String> navigationNames = getNavigationNames(selectedField);
        if (navigationNames.isEmpty())
        {
            return;
        }

        RelatedFinder<T> currentFinder = finderInstance;
        for (String navigationName : navigationNames)
        {
            currentFinder = currentFinder.getRelationshipFinderByName(navigationName);
            Objects.requireNonNull(currentFinder);
        }
        Navigation<T> navigation = (Navigation<T>) currentFinder;
        result.deepFetch(navigation);
    }

    private static MutableList<String> getNavigationNames(SelectedField selectedField)
    {
        String qualifiedName = selectedField.getQualifiedName();
        MutableList<String> fieldNamesNames = ArrayAdapter.adapt(qualifiedName.split("/"));
        MutableList<String> navigationNames = fieldNamesNames.take(fieldNamesNames.size() - 1);
        return navigationNames;
    }
}
