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

package io.liftwizard.configuration.metrics.reporter.logback;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.metrics.ReporterFactory;
import io.liftwizard.configuration.metrics.reporter.slf4j.StructuredSlf4jReporterFactory;
import net.logstash.logback.argument.StructuredArguments;

@JsonTypeName("structured-logback")
@AutoService(ReporterFactory.class)
public class StructuredLogbackReporterFactory
        extends StructuredSlf4jReporterFactory
{
    @Override
    protected Function<Map<String, Object>, ?> getMapToStructuredObjectFunction()
    {
        return StructuredArguments::entries;
    }
}
