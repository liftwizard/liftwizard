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

package io.liftwizard.model.reladomo.operation.compiler;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ThrowingErrorListener extends BaseErrorListener
{
    private final String[] lines;

    public ThrowingErrorListener(String[] lines)
    {
        this.lines = lines;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e)
    {
        String sourceLine = this.getSourceLine(line);
        String error = String.format(
                "(%d) %s [%d:%d]%n%s",
                line,
                msg,
                line,
                charPositionInLine,
                sourceLine);
        throw new ParseCancellationException(error);
    }

    private String getSourceLine(int line)
    {
        if (line == 1)
        {
            return this.lines[line - 1];
        }

        return this.lines[line - 2] + "\n" + this.lines[line - 1];
    }
}
