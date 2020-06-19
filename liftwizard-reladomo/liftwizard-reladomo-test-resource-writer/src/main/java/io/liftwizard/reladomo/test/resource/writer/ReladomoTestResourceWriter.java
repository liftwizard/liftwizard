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
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import org.eclipse.collections.impl.set.mutable.SetAdapter;

public final class ReladomoTestResourceWriter
{
    private ReladomoTestResourceWriter()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String generate()
    {
        Set<MithraRuntimeCacheController> runtimeCacheControllerSet = MithraManagerProvider
                .getMithraManager()
                .getRuntimeCacheControllerSet();

        return SetAdapter.adapt(runtimeCacheControllerSet)
                .toSortedListBy(MithraRuntimeCacheController::getClassName)
                .collect(ReladomoTestResourceWriter::getReladomoTestResourceGrid)
                .reject(ReladomoTestResourceGrid::isEmpty)
                .tap(ReladomoTestResourceGrid::freeze)
                .collect(Objects::toString)
                .makeString("");
    }

    private static ReladomoTestResourceGrid getReladomoTestResourceGrid(MithraRuntimeCacheController eachController)
    {
        RelatedFinder finderInstance = eachController.getFinderInstance();
        MithraList<?> mithraList     = finderInstance.findMany(finderInstance.all());

        ReladomoClassMetaData metaData = eachController.getMetaData();

        return new ReladomoTestResourceGrid(metaData, mithraList);
    }
}
