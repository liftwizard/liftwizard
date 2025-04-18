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

package io.liftwizard.dropwizard.bundle.config.logging;

import com.fasterxml.jackson.databind.introspect.ClassIntrospector.MixInResolver;

public class JsonIncludeNonDefaultMixInResolver implements MixInResolver {

    @Override
    public Class<?> findMixInClassFor(Class<?> cls) {
        return JsonIncludeNonDefaultMixIn.class;
    }

    @Override
    public MixInResolver copy() {
        return this;
    }
}
