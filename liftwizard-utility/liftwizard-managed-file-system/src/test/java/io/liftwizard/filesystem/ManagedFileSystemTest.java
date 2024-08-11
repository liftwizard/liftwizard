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

package io.liftwizard.filesystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LogMarkerTestExtension.class)
class ManagedFileSystemTest
{
    @Test
    public void smokeTest()
            throws URISyntaxException
    {
        Path path1 = ManagedFileSystem.get(new URI("file:///"));
        assertTrue(path1.isAbsolute());
        assertTrue(path1.toFile().exists());

        String tmpdir = System.getProperty("java.io.tmpdir");
        Path path2 = ManagedFileSystem.get(new URI("file://" + tmpdir));
        assertTrue(path2.isAbsolute());
        assertTrue(path2.toFile().exists());
    }
}
