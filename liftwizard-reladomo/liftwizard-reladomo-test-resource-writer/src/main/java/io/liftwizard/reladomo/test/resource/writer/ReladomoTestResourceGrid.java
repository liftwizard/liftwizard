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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.list.Interval;
import org.eclipse.collections.impl.set.mutable.SetAdapter;

public class ReladomoTestResourceGrid
{
    private final ReladomoClassMetaData                     metaData;
    private final ImmutableList<ReladomoTestResourceColumn> columns;
    private final MithraList<?>                             mithraList;

    private boolean frozen;

    public ReladomoTestResourceGrid(ReladomoClassMetaData metaData, MithraList<?> mithraList)
    {
        this.metaData   = Objects.requireNonNull(metaData);
        this.mithraList = Objects.requireNonNull(mithraList);

        MutableSet<Attribute> attributes = SetAdapter.adapt(new LinkedHashSet<>());
        if (metaData.getAsOfAttributes() != null)
        {
            for (AsOfAttribute asOfAttribute : metaData.getAsOfAttributes())
            {
                attributes.add(asOfAttribute.getFromAttribute());
                attributes.add(asOfAttribute.getToAttribute());
            }
        }
        attributes.addAll(Arrays.asList(metaData.getPrimaryKeyAttributes()));
        attributes.addAll(Arrays.asList(metaData.getPersistentAttributes()));

        this.columns = attributes
                .toList()
                .collectWith(ReladomoTestResourceColumn::new, this)
                .toImmutable();

        Class<?> aClass = metaData.getBusinessOrInterfaceClass();
        for (Object mithraObject : mithraList)
        {
            for (ReladomoTestResourceColumn column : this.columns)
            {
                Object cast = aClass.cast(mithraObject);
                column.addMithraObject(cast);
            }
        }
    }

    public boolean isEmpty()
    {
        return this.mithraList.isEmpty();
    }

    public void freeze()
    {
        if (this.frozen)
        {
            throw new IllegalStateException();
        }

        this.columns.each(ReladomoTestResourceColumn::freeze);
        this.frozen = true;
    }

    @Override
    public String toString()
    {
        if (!this.frozen)
        {
            return "";
        }

        String classString     = "class " + this.metaData.getBusinessOrInterfaceClassName() + "\n";
        String headerRowString = this.columns.collect(ReladomoTestResourceColumn::getPaddedHeader).makeString() + "\n";
        LazyIterable<String> rowStrings = Interval.zeroTo(this.mithraList.size() - 1)
                .collect(this::getRowString);
        String bodyString = rowStrings.makeString("");
        return classString + headerRowString + bodyString;
    }

    private String getRowString(int index)
    {
        return this.columns.collect(each -> each.getPaddedValueString(index)).makeString() + "\n";
    }
}
