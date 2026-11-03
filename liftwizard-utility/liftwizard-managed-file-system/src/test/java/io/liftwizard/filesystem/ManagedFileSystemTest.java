/*
 * Copyright 2026 Craig Motlin
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(LogMarkerTestExtension.class)
class ManagedFileSystemTest
{
	@Test
	void smokeTest()
		throws URISyntaxException
	{
		Path path1 = ManagedFileSystem.get(new URI("file:///"));
		assertThat(path1).isAbsolute();
		assertThat(path1.toFile()).exists();

		String tmpdir = System.getProperty("java.io.tmpdir");
		Path path2 = ManagedFileSystem.get(new URI("file://" + tmpdir));
		assertThat(path2).isAbsolute();
		assertThat(path2.toFile()).exists();
	}

	@Test
	void jarWithSpaceInPath(@TempDir Path tempDir)
		throws IOException
	{
		Path jarFile = createJar(tempDir.resolve("John Doe").resolve("definitions.jar"));
		URI uri = URI.create("jar:" + jarFile.toUri() + "!/defs");
		assertThat(uri.toString()).contains("John%20Doe");

		Path path = ManagedFileSystem.get(uri);
		assertThat(path).hasToString("/defs");
		assertThat(Files.readString(path.resolve("example.txt"))).isEqualTo("example contents");
	}

	@Test
	void jarWithoutSeparator(@TempDir Path tempDir)
		throws IOException
	{
		Path jarFile = createJar(tempDir.resolve("no separator").resolve("definitions.jar"));
		URI uri = URI.create("jar:" + jarFile.toUri());

		Path path = ManagedFileSystem.get(uri);
		assertThat(path).hasToString("/");
		assertThat(Files.readString(path.resolve("defs/example.txt"))).isEqualTo("example contents");
	}

	private static Path createJar(Path jarFile)
		throws IOException
	{
		Files.createDirectories(jarFile.getParent());
		try (
			OutputStream outputStream = Files.newOutputStream(jarFile);
			var zipOutputStream = new ZipOutputStream(outputStream)
		)
		{
			zipOutputStream.putNextEntry(new ZipEntry("defs/"));
			zipOutputStream.closeEntry();
			zipOutputStream.putNextEntry(new ZipEntry("defs/example.txt"));
			zipOutputStream.write("example contents".getBytes(StandardCharsets.UTF_8));
			zipOutputStream.closeEntry();
		}
		return jarFile;
	}
}
