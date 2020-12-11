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

package io.liftwizard.logging.slf4j.mdc;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class MultiMDCCloseable
        implements AutoCloseable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiMDCCloseable.class);

    private final Set<String> keys = new LinkedHashSet<>();

    public void put(String key, String value)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("key parameter cannot be null");
        }

        if (!this.keys.add(key))
        {
            throw new IllegalArgumentException(key);
        }

        if (value == null)
        {
            LOGGER.warn("Dropping null value for key: {}", key);
            return;
        }

        MDC.put(key, value);
    }

    @Override
    public void close()
    {
        this.keys.forEach(MDC::remove);
    }
}
