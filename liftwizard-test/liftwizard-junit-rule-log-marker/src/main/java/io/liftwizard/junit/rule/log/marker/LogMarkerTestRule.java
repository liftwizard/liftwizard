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

package io.liftwizard.junit.rule.log.marker;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * A JUnit {@link Rule} that clears the buffer before all tests and flushes the buffer after failed tests. It does this by logging CLEAR and FLUSH markers.
 *
 * @see <a href="https://liftwizard.io/docs/logging/buffered-logging#buffered-logging-in-tests-logmarkertestrule">https://liftwizard.io/docs/logging/buffered-logging#buffered-logging-in-tests-logmarkertestrule</a>
 */
public class LogMarkerTestRule
        extends TestWatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMarkerTestRule.class);
    private static final Marker MARKER_CLEAR = MarkerFactory.getMarker("CLEAR");
    private static final Marker MARKER_FLUSH = MarkerFactory.getMarker("FLUSH");

    @Override
    protected void starting(Description description)
    {
        MDC.put("liftwizard.junit.test.name", description.getDisplayName());
        LOGGER.info(MARKER_CLEAR, "Test starting. Logging the CLEAR marker to clear the buffer in BufferedAppender.");
    }

    @Override
    protected void failed(Throwable e, Description description)
    {
        LOGGER.info(MARKER_FLUSH, "Test failed. Logging the FLUSH marker to flush the buffer in BufferedAppender.");
    }

    @Override
    protected void finished(Description description)
    {
        MDC.remove("liftwizard.junit.test.name");
    }
}
