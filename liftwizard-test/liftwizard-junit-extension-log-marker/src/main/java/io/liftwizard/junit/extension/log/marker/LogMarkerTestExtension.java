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

package io.liftwizard.junit.extension.log.marker;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * A JUnit Extension that clears the buffer before all tests and flushes the buffer after failed tests. It does this by logging CLEAR and FLUSH markers.
 *
 * @see <a href="https://liftwizard.io/docs/logging/buffered-logging#buffered-logging-in-tests-logmarkertestrule">https://liftwizard.io/docs/logging/buffered-logging#buffered-logging-in-tests-logmarkertestrule</a>
 */
public class LogMarkerTestExtension
        implements BeforeEachCallback, AfterEachCallback, TestWatcher
{
    private static final Logger LOGGER       = LoggerFactory.getLogger(LogMarkerTestExtension.class);
    private static final Marker MARKER_CLEAR = MarkerFactory.getMarker("CLEAR");
    private static final Marker MARKER_FLUSH = MarkerFactory.getMarker("FLUSH");

    @Override
    public void beforeEach(ExtensionContext context)
    {
        MDC.put("liftwizard.junit.test.name", context.getDisplayName());
        LOGGER.info(MARKER_CLEAR, "Test starting. Logging the CLEAR marker to clear the buffer in BufferedAppender.");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause)
    {
        LOGGER.info(MARKER_FLUSH, "Test failed. Logging the FLUSH marker to flush the buffer in BufferedAppender.");
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        MDC.remove("liftwizard.junit.test.name");
    }
}
