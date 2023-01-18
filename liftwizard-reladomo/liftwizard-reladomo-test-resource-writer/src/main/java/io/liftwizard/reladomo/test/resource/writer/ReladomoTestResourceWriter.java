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

package io.liftwizard.reladomo.test.resource.writer;

import java.util.Objects;
import java.util.Set;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public final class ReladomoTestResourceWriter
{
    private ReladomoTestResourceWriter()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String generate()
    {
        return generate(Lists.immutable.empty());
    }

    public static String generate(ImmutableList<String> classNamesInOrder)
    {
        Set<MithraRuntimeCacheController> runtimeCacheControllerSet = MithraManagerProvider
                .getMithraManager()
                .getRuntimeCacheControllerSet();
        ImmutableList<MithraRuntimeCacheController> mithraRuntimeCacheControllers = Lists.immutable.withAll(
                runtimeCacheControllerSet);

        // Sort runtimeCacheControllerSet by the order of their names in classNamesInOrder.
        // If a name is not in classNamesInOrder, it is sorted to the end.
        MutableList<MithraRuntimeCacheController> mithraRuntimeCacheControllersSorted = mithraRuntimeCacheControllers
                .toSortedListBy(controller ->
                {
                    String businessClassName = controller.getMithraObjectPortal().getBusinessClassName();
                    int    result            = classNamesInOrder.indexOf(businessClassName);
                    return result == -1 ? Integer.MAX_VALUE : result;
                });

        return mithraRuntimeCacheControllersSorted
                .collect(ReladomoTestResourceWriter::getReladomoTestResourceGrid)
                .reject(ReladomoTestResourceGrid::isEmpty)
                .tap(ReladomoTestResourceGrid::freeze)
                .collect(Objects::toString)
                .makeString("\n");
    }

    private static ReladomoTestResourceGrid getReladomoTestResourceGrid(MithraRuntimeCacheController eachController)
    {
        RelatedFinder   finderInstance = eachController.getFinderInstance();
        AsOfAttribute[] asOfAttributes = finderInstance.getAsOfAttributes();
        Operation       operation      = finderInstance.all();
        if (asOfAttributes != null)
        {
            for (AsOfAttribute asOfAttribute : asOfAttributes)
            {
                Operation equalsEdgePoint = asOfAttribute.equalsEdgePoint();
                operation = operation.and(equalsEdgePoint);
            }
        }
        MithraList<?>         mithraList = finderInstance.findMany(operation);
        ReladomoClassMetaData metaData   = eachController.getMetaData();
        return new ReladomoTestResourceGrid(metaData, mithraList);
    }
}
