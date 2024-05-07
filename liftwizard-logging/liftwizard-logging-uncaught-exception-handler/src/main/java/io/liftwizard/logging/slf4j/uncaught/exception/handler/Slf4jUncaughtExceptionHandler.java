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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jUncaughtExceptionHandler
        implements UncaughtExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread thread, Throwable throwable)
    {
        try (MultiMDCCloseable mdc = new MultiMDCCloseable())
        {
            mdc.put("threadName", thread.getName());
            mdc.put("exceptionClass", throwable.getClass().getCanonicalName());
            mdc.put("exceptionMessage", throwable.getMessage());

            mdc.put("liftwizard.error.thread", thread.getName());
            mdc.put("liftwizard.error.kind", throwable.getClass().getCanonicalName());
            mdc.put("liftwizard.error.message", throwable.getMessage());

            String message = "Exception in thread \"" + thread.getName() + "\"";
            LOGGER.warn(message, throwable);

            StringWriter stringWriter         = new StringWriter();
            PrintWriter  printWriter = new PrintWriter(stringWriter, true);
            throwable.printStackTrace(printWriter);

            System.err.print(message + " " + stringWriter);
        }
    }
}
