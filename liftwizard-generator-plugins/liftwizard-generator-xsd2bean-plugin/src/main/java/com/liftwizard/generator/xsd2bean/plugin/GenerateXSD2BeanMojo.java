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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.gs.fw.common.freyaxml.generator.FreyaXmlException;
import com.gs.fw.common.freyaxml.generator.FreyaXmlGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.utility.Iterate;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Based on Antlr4Mojo in the antlr project.
 *
 * @see <a href="https://github.com/antlr/antlr4/blob/master/antlr4-maven-plugin/src/main/java/org/antlr/mojo/antlr4/Antlr4Mojo.java">Antlr4Mojo</a>
 */
@Mojo(
        name = "generate-xsd2bean",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateXSD2BeanMojo extends AbstractMojo
{
    @Component
    private BuildContext buildContext;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @SuppressWarnings("FieldMayBeFinal")
    @Parameter
    private Set<String> includes = Sets.mutable.empty();

    @SuppressWarnings("FieldMayBeFinal")
    @Nonnull
    @Parameter
    private Set<String> excludes = new HashSet<>();

    @Parameter(property = "freya.generateTestSources", defaultValue = "false")
    private boolean generateTestSources;

    @Parameter(defaultValue = "${basedir}/src/main/xsd")
    private File sourceDirectory;

    /**
     * Specify output directory where the Java files are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/freya")
    private File outputDirectory;

    @Parameter(property = "nonGeneratedSourcesDir",
               defaultValue = "${project.build.sourceDirectory}",
               required = true,
               readonly = true)
    private File nonGeneratedSourcesDir;

    @Parameter(property = "destinationPackage", required = true, readonly = true)
    private String destinationPackage;

    @Parameter(property = "parserName", required = true, readonly = true)
    private String parserName;

    @Parameter(property = "validateAttributes", readonly = true, defaultValue = "true")
    private boolean validateAttributes;
    @Parameter(property = "ignoreNonGeneratedAbstractClasses", readonly = true)
    private boolean ignoreNonGeneratedAbstractClasses;
    @Parameter(property = "ignorePackageNamingConvention", readonly = true)
    private boolean ignorePackageNamingConvention;
    @Parameter(property = "generateTopLevelSubstitutionElements", readonly = true)
    private boolean generateTopLevelSubstitutionElements;

    private void addSourceRoot(@Nonnull File outputDir)
    {
        String outputPath = outputDir.getPath();
        if (this.generateTestSources)
        {
            this.mavenProject.addTestCompileSourceRoot(outputPath);
        }
        else
        {
            this.mavenProject.addCompileSourceRoot(outputPath);
        }
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        if (!this.outputDirectory.exists())
        {
            this.outputDirectory.mkdirs();
        }

        if (!this.nonGeneratedSourcesDir.exists())
        {
            this.nonGeneratedSourcesDir.mkdirs();
        }

        if (!this.sourceDirectory.isDirectory())
        {
            throw new MojoExecutionException("No such directory " + this.sourceDirectory.getAbsolutePath());
        }

        File outputDir = this.outputDirectory;

        if (!outputDir.exists())
        {
            outputDir.mkdirs();
        }

        Set<File> schemaFiles = this.getSchemaFiles();

        this.getLog().debug("Output directory base will be " + this.outputDirectory.getAbsolutePath());
        this.getLog().info("Processing source directory " + this.sourceDirectory.getAbsolutePath());

        for (File schemaFile : schemaFiles)
        {
            FreyaXmlGenerator generator = new FreyaXmlGenerator();
            generator.setLogger(new FreyaMavenLogger(this.getLog()));

            generator.setDestinationPackage(this.destinationPackage);
            generator.setParserName(this.parserName);
            generator.setGeneratedDir(this.outputDirectory.getAbsolutePath());
            generator.setNonGeneratedDir(this.nonGeneratedSourcesDir.getAbsolutePath());
            generator.setXsd(schemaFile.getAbsolutePath());

            generator.setValidateAttributes(this.validateAttributes);
            generator.setIgnoreNonGeneratedAbstractClasses(this.ignoreNonGeneratedAbstractClasses);
            generator.setIgnorePackageNamingConvention(this.ignorePackageNamingConvention);
            generator.setGenerateTopLevelSubstitutionElements(this.generateTopLevelSubstitutionElements);

            try
            {
                generator.generate();
            }
            catch (@Nonnull FreyaXmlException | IOException e)
            {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        this.addSourceRoot(this.outputDirectory);
    }

    protected Set<File> getSchemaFiles() throws MojoExecutionException
    {
        try
        {
            Set<File> schemaFiles = this.scanSchemaFiles();
            this.processSchemaFiles(schemaFiles);
            return schemaFiles;
        }
        catch (@Nonnull MojoExecutionException | InclusionScanException e)
        {
            this.getLog().error(e);
            throw new MojoExecutionException(
                    "Fatal error occurred while evaluating the names of the xsd files to analyze",
                    e);
        }
    }

    private Set<File> scanSchemaFiles() throws InclusionScanException
    {
        Set<String>            includesPatterns = this.getIncludesPatterns();
        SourceInclusionScanner scan             = new SimpleSourceInclusionScanner(includesPatterns, this.excludes);
        SourceMapping          suffixMapping    = new SuffixMapping("xsd", Collections.emptySet());
        scan.addSourceMapping(suffixMapping);
        return scan.getIncludedSources(this.sourceDirectory, null);
    }

    public Set<String> getIncludesPatterns()
    {
        if (Iterate.isEmpty(this.includes))
        {
            return Sets.mutable.with("**/*.xsd");
        }
        return this.includes;
    }

    private void processSchemaFiles(@Nonnull Set<File> schemaFiles) throws MojoExecutionException
    {
        if (schemaFiles.isEmpty())
        {
            throw new MojoExecutionException("No schemas found.");
        }

        for (File schemaFile : schemaFiles)
        {
            this.buildContext.refresh(schemaFile);
            this.buildContext.removeMessages(schemaFile);
            this.getLog().debug("Schema file '" + schemaFile.getPath() + "' detected.");
        }
    }
}
