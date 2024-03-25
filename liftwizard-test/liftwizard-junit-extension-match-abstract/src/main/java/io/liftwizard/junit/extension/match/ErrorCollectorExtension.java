/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.junit.extension.match;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.MultipleFailuresError;

public class ErrorCollectorExtension
        implements AfterEachCallback
{
    private final MutableList<Throwable> errors = Lists.mutable.empty();

    public void addError(AssertionError error)
    {
        if (error == null)
        {
            throw new NullPointerException("Error cannot be null");
        }
        this.errors.add(error);
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        if (this.errors.isEmpty())
        {
            return;
        }

        MultipleFailuresError multipleFailuresError = new MultipleFailuresError(null, this.errors);
        this.errors.forEach(multipleFailuresError::addSuppressed);
        throw multipleFailuresError;
    }
}
