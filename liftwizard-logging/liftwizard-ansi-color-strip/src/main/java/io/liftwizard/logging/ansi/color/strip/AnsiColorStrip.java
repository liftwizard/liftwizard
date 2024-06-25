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

package io.liftwizard.logging.ansi.color.strip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiMode;
import org.fusesource.jansi.AnsiType;
import org.fusesource.jansi.io.AnsiOutputStream;

/**
 * Based on the Jansi test <a href="https://github.com/fusesource/jansi/blob/master/src/test/java/org/fusesource/jansi/io/AnsiOutputStreamTest.java">AnsiOutputStreamTest</a>
 */
public final class AnsiColorStrip
{
    private AnsiColorStrip()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String strip(String input)
    {
        return strip(input, StandardCharsets.UTF_8);
    }

    public static String strip(String input, Charset charset)
    {
        byte[] bytes = input.getBytes(charset);
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                AnsiOutputStream ansiOutputStream = new AnsiOutputStream(
                        byteArrayOutputStream,
                        null,
                        AnsiMode.Strip,
                        null,
                        AnsiType.Emulation,
                        AnsiColors.TrueColor,
                        charset,
                        null,
                        null,
                        false))
        {
            ansiOutputStream.write(bytes);
            return byteArrayOutputStream.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
