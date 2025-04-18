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

package io.liftwizard.junit.extension.match;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

import javax.annotation.Nonnull;

public final class FileSlurper {

    private FileSlurper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String slurp(@Nonnull String resourceClassPathLocation, @Nonnull Class<?> callingClass) {
        return slurp(resourceClassPathLocation, callingClass, StandardCharsets.UTF_8);
    }

    public static String slurp(
        @Nonnull String resourceClassPathLocation,
        @Nonnull Class<?> callingClass,
        Charset charset
    ) {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        Objects.requireNonNull(inputStream, resourceClassPathLocation);
        return slurp(inputStream, charset);
    }

    public static String slurp(@Nonnull InputStream inputStream, Charset charset) {
        try (Scanner scanner = new Scanner(inputStream, charset)) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}
