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

package io.liftwizard.logging.metrics.structured.proxy;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class ErrorLoggerProxy
        extends AbstractLoggerProxy
{
    public ErrorLoggerProxy(Logger logger)
    {
        super(logger);
    }

    @Override
    public void log(Marker marker, String message, Object structuredObject)
    {
        this.logger.error(marker, message, structuredObject);
    }

    @Override
    public boolean isEnabled(Marker marker)
    {
        return this.logger.isErrorEnabled(marker);
    }
}
