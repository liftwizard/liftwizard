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

package io.liftwizard.generator.reladomo.code.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.generator.CoreMithraGenerator;
import io.liftwizard.filesystem.ManagedFileSystem;
import io.liftwizard.maven.reladomo.logger.MavenReladomoLogger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
        name = "generate-reladomo-pojos",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateReladomoCodeMojo
        extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property = "definitionsAndClassListDirectory", defaultValue = "reladomo")
    private String definitionsAndClassListDirectory;

    @Parameter(property = "classListFileName", defaultValue = "ReladomoClassList.xml")
    private String classListFileName;

    @Parameter(property = "generatedDir", defaultValue = "${project.build.directory}/generated-sources/reladomo")
    private File generatedDir;

    @Parameter(property = "nonGeneratedDir", defaultValue = "${project.build.sourceDirectory}")
    private File nonGeneratedDir;

    @Parameter(property = "generateConcreteClasses", defaultValue = "true")
    private final boolean generateConcreteClasses = true;
    @Parameter(property = "warnAboutConcreteClasses", defaultValue = "true")
    private final boolean warnAboutConcreteClasses = true;
    @Parameter(property = "generateEcListMethod", defaultValue = "true")
    private final boolean generateEcListMethod = true;

    @Override
    public void execute()
    {
        if (!this.generatedDir.exists())
        {
            this.generatedDir.mkdirs();
        }

        Path tempFile = this.getTempFile();
        if (!tempFile.toFile().exists())
        {
            throw new IllegalStateException("Could not find " + tempFile);
        }

        CoreMithraGenerator coreGenerator = this.getGenerator(tempFile);
        coreGenerator.execute();

        this.mavenProject.addCompileSourceRoot(this.generatedDir.getAbsolutePath());
    }

    @Nonnull
    private CoreMithraGenerator getGenerator(Path tempFile)
    {
        var coreGenerator = new CoreMithraGenerator();
        coreGenerator.setLogger(new MavenReladomoLogger(this.getLog()));
        coreGenerator.setXml(tempFile.toString());
        coreGenerator.setGeneratedDir(this.generatedDir.getAbsolutePath());
        coreGenerator.setNonGeneratedDir(this.nonGeneratedDir.getAbsolutePath());
        coreGenerator.setGenerateConcreteClasses(this.generateConcreteClasses);
        coreGenerator.setWarnAboutConcreteClasses(this.warnAboutConcreteClasses);
        coreGenerator.setGenerateEcListMethod(this.generateEcListMethod);
        return coreGenerator;
    }

    @Nonnull
    private Path getTempFile()
    {
        if (this.definitionsAndClassListDirectory.startsWith("/"))
        {
            throw new IllegalArgumentException("definitionsAndClassListDirectory must not start with a /");
        }

        try
        {
            URL resource = this.getClass().getResource('/' + this.definitionsAndClassListDirectory);
            Objects.requireNonNull(resource, () -> "Could not find /" + this.definitionsAndClassListDirectory);
            URI uri = resource.toURI();
            Path from = ManagedFileSystem.get(uri);
            Path to = Files
                    .createTempDirectory(this.getClass().getSimpleName())
                    .resolve(this.definitionsAndClassListDirectory);

            this.copyDirectory(from, to);
            return to.resolve(this.classListFileName);
        }
        catch (URISyntaxException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void copyDirectory(Path from, Path to)
            throws IOException
    {
        try (Stream<Path> sources = Files.walk(from))
        {
            sources.forEach(src -> this.handleOneFile(from, src, to));
        }
    }

    // Based on https://stackoverflow.com/a/29659925/
    private void handleOneFile(Path fileSystemRoot, Path fileSystemSource, Path target)
    {
        Path copyDestination = target.resolve(fileSystemRoot.relativize(fileSystemSource).toString());
        try
        {
            if (Files.isDirectory(fileSystemSource))
            {
                if (Files.notExists(copyDestination))
                {
                    this.getLog().info("Creating directory " + copyDestination);
                    Files.createDirectories(copyDestination);
                }
            }
            else
            {
                this.getLog().info("Copying resource " + fileSystemSource + " to " + copyDestination);
                Files.copy(fileSystemSource, copyDestination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed target unzip file.", e);
        }
    }
}
