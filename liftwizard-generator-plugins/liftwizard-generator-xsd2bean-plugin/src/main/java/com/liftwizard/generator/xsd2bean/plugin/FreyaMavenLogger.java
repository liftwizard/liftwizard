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

package com.liftwizard.generator.xsd2bean.plugin;

import com.gs.fw.common.freyaxml.generator.Logger;
import org.apache.maven.plugin.logging.Log;

public class FreyaMavenLogger implements Logger
{
    private final Log log;

    public FreyaMavenLogger(Log log)
    {
        this.log = log;
    }

    @Override
    public void debug(String content)
    {
        this.log.debug(content);
    }

    @Override
    public void debug(String content, Throwable error)
    {
        this.log.debug(content, error);
    }

    @Override
    public void info(String content)
    {
        this.log.info(content);
    }

    @Override
    public void info(String content, Throwable error)
    {
        this.log.info(content, error);
    }

    @Override
    public void warn(String content)
    {
        this.log.warn(content);
    }

    @Override
    public void warn(String content, Throwable error)
    {
        this.log.warn(content, error);
    }

    @Override
    public void error(String content)
    {
        this.log.error(content);
    }

    @Override
    public void error(String content, Throwable error)
    {
        this.log.error(content, error);
    }
}
