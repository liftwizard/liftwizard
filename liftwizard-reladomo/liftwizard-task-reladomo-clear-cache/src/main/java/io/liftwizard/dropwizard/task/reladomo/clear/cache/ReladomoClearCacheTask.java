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

package io.liftwizard.dropwizard.task.reladomo.clear.cache;

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
