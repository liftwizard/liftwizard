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

package io.liftwizard.reladomo.test.rule;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.fw.finder.TemporalTransactionalDomainList;
import com.gs.fw.finder.TransactionalDomainList;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ReladomoPurgeAllTestRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new PurgeAllStatement(base);
    }

    private static final class PurgeAllStatement extends Statement {

        private final Statement base;

        private PurgeAllStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            this.before();
            try {
                this.base.evaluate();
            } finally {
                this.after();
            }
        }

        private void before() {
            this.purgeTypes();
        }

        private void after() {
            this.purgeTypes();
        }

        private void purgeTypes() {
            MithraManagerProvider.getMithraManager()
                .executeTransactionalCommand(tx -> {
                    MithraManagerProvider.getMithraManager().getRuntimeCacheControllerSet().forEach(this::purgeType);

                    return null;
                });
        }

        private void purgeType(MithraRuntimeCacheController mithraRuntimeCacheController) {
            ReladomoClassMetaData metaData = mithraRuntimeCacheController.getMetaData();
            RelatedFinder finderInstance = metaData.getFinderInstance();
            ListIterable<AsOfAttribute> asOfAttributes = metaData.getAsOfAttributes() == null
                ? Lists.immutable.empty()
                : ArrayAdapter.adapt(metaData.getAsOfAttributes());
            Operation operation = asOfAttributes
                .collect(AsOfAttribute::equalsEdgePoint)
                .reduce(Operation::and)
                .orElseGet(finderInstance::all);
            MithraList<?> mithraList = finderInstance.findMany(operation);

            if (mithraList instanceof TemporalTransactionalDomainList<?> temporalTransactionalDomainList) {
                temporalTransactionalDomainList.purgeAll();
            } else if (mithraList instanceof TransactionalDomainList<?> transactionalDomainList) {
                transactionalDomainList.deleteAll();
            } else {
                throw new AssertionError(mithraList.getClass().getCanonicalName());
            }
        }
    }
}
