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

package io.liftwizard.logging.slf4j.uncaught.exception.handler;

import org.junit.jupiter.api.Test;

class Slf4jUncaughtExceptionHandlerTest
{
    @Test
    void testUncaughtException()
    {
        CauseException causeException = new CauseException("example cause");
        RootException rootException = new RootException("example root", causeException);

        new Slf4jUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), rootException);
    }

    private static class CauseException
            extends RuntimeException
    {
        CauseException(String message)
        {
            super(message);
        }
    }

    private static class RootException
            extends RuntimeException
    {
        RootException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
