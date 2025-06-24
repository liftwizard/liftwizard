/*
 * Copyright 2025 Craig Motlin
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

package io.liftwizard.generator.reladomo.database.plugin;

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

import com.gs.fw.common.mithra.generator.dbgenerator.CoreMithraDbDefinitionGenerator;
import io.liftwizard.filesystem.ManagedFileSystem;
import io.liftwizard.maven.reladomo.logger.MavenReladomoLogger;
import io.liftwizard.tempdir.ManagedTempDirectory;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(
    name = "generate-reladomo-database",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.RUNTIME
)
public class GenerateReladomoDatabaseMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateReladomoDatabaseMojo.class);

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property = "definitionsAndClassListDirectory", defaultValue = "reladomo")
    private String definitionsAndClassListDirectory;

    @Parameter(property = "classListFileName", defaultValue = "ReladomoClassList.xml")
    private String classListFileName;

    @Parameter(
        property = "generatedResourcesDirectory",
        defaultValue = "${project.build.directory}/generated-resources"
    )
    private File generatedResourcesDirectory;

    @Parameter(property = "outputDirectory", defaultValue = "sql")
    private String outputDirectory;

    @Parameter(property = "databaseType", defaultValue = "postgres")
    private String databaseType;

    @Parameter(property = "ignoreNonGeneratedAbstractClasses", defaultValue = "false")
    private boolean ignoreNonGeneratedAbstractClasses;

    @Parameter(property = "ignoreTransactionalMethods", defaultValue = "false")
    private boolean ignoreTransactionalMethods;

    @Parameter(property = "ignorePackageNamingConvention", defaultValue = "false")
    private boolean ignorePackageNamingConvention;

    @Parameter(property = "defaultFinalGetters", defaultValue = "false")
    private boolean defaultFinalGetters;

    @Parameter(property = "forceOffHeap", defaultValue = "false")
    private boolean forceOffHeap;

    @Parameter(property = "generateFileHeaders", defaultValue = "false")
    private boolean generateFileHeaders;

    @Override
    public void execute() {
        File generatedOutputDirectory = new File(this.generatedResourcesDirectory, this.outputDirectory);

        if (!generatedOutputDirectory.exists()) {
            generatedOutputDirectory.mkdirs();
        }

        try (var tempFile = this.getTempFile()) {
            CoreMithraDbDefinitionGenerator coreGenerator =
                this.getGenerator(tempFile.getPath(), generatedOutputDirectory);
            coreGenerator.execute();

            Resource resource = new Resource();
            resource.setDirectory(this.generatedResourcesDirectory.getAbsolutePath());

            LOGGER.debug("Adding resource directory: {}", this.generatedResourcesDirectory.getAbsolutePath());
            this.mavenProject.addResource(resource);
        }
    }

    @Nonnull
    private CoreMithraDbDefinitionGenerator getGenerator(Path tempFile, File generatedOutputDirectory) {
        Path classListFile = tempFile.resolve(this.definitionsAndClassListDirectory).resolve(this.classListFileName);
        var coreGenerator = new CoreMithraDbDefinitionGenerator();
        coreGenerator.setLogger(new MavenReladomoLogger(this.getLog()));
        coreGenerator.setXml(classListFile.toString());
        coreGenerator.setGeneratedDir(generatedOutputDirectory.getAbsolutePath());
        coreGenerator.setDatabaseType(this.databaseType);
        coreGenerator.setIgnoreNonGeneratedAbstractClasses(this.ignoreNonGeneratedAbstractClasses);
        coreGenerator.setIgnoreTransactionalMethods(this.ignoreTransactionalMethods);
        coreGenerator.setIgnorePackageNamingConvention(this.ignorePackageNamingConvention);
        coreGenerator.setDefaultFinalGetters(this.defaultFinalGetters);
        coreGenerator.setForceOffHeap(this.forceOffHeap);
        coreGenerator.setGenerateFileHeaders(this.generateFileHeaders);
        return coreGenerator;
    }

    @Nonnull
    private ManagedTempDirectory getTempFile() {
        if (this.definitionsAndClassListDirectory.startsWith("/")) {
            throw new IllegalArgumentException("definitionsAndClassListDirectory must not start with a /");
        }

        try {
            URL resource = this.getClass().getResource("/" + this.definitionsAndClassListDirectory);
            Objects.requireNonNull(resource, () -> "Could not find /" + this.definitionsAndClassListDirectory);
            URI uri = resource.toURI();
            Path from = ManagedFileSystem.get(uri);
            ManagedTempDirectory managedTempDirectory = ManagedTempDirectory.create(this.getClass().getSimpleName());
            Path to = managedTempDirectory.getPath();

            this.copyDirectory(from, to);
            return managedTempDirectory;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDirectory(Path from, Path to) throws IOException {
        try (Stream<Path> sources = Files.walk(from.resolve(this.definitionsAndClassListDirectory))) {
            sources.forEach(src -> GenerateReladomoDatabaseMojo.handleOneFile(from, src, to));
        }
    }

    // Based on https://stackoverflow.com/a/29659925/
    private static void handleOneFile(Path fileSystemRoot, Path fileSystemSource, Path target) {
        Path copyDestination = target.resolve(fileSystemRoot.relativize(fileSystemSource).toString());
        try {
            if (Files.isDirectory(fileSystemSource)) {
                if (Files.notExists(copyDestination)) {
                    LOGGER.info("Creating directory {}", copyDestination);
                    Files.createDirectories(copyDestination);
                }
            } else {
                LOGGER.info("Copying resource {} to {}", fileSystemSource, copyDestination);
                Files.copy(fileSystemSource, copyDestination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed target unzip file.", e);
        }
    }
}
