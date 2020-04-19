package com.liftwizard.generator.reladomo.database.plugin;

import java.io.File;

import com.gs.fw.common.mithra.generator.dbgenerator.CoreMithraDbDefinitionGenerator;
import com.liftwizard.maven.reladomo.logger.MavenReladomoLogger;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
        name = "generate-reladomo-database",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateReladomoDatabaseMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(
            property = "definitionsXmlFile",
            required = true,
            readonly = true)
    private File definitionsXmlFile;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-resources/sql")
    private File outputDirectory;

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
    public void execute()
    {
        if (!this.outputDirectory.exists())
        {
            this.outputDirectory.mkdirs();
        }

        CoreMithraDbDefinitionGenerator coreGenerator = new CoreMithraDbDefinitionGenerator();
        coreGenerator.setXml(this.definitionsXmlFile.getAbsolutePath());
        coreGenerator.setGeneratedDir(this.outputDirectory.getAbsolutePath());
        coreGenerator.setDatabaseType(this.databaseType);
        coreGenerator.setIgnoreNonGeneratedAbstractClasses(this.ignoreNonGeneratedAbstractClasses);
        coreGenerator.setIgnoreTransactionalMethods(this.ignoreTransactionalMethods);
        coreGenerator.setIgnorePackageNamingConvention(this.ignorePackageNamingConvention);
        coreGenerator.setDefaultFinalGetters(this.defaultFinalGetters);
        coreGenerator.setForceOffHeap(this.forceOffHeap);
        coreGenerator.setGenerateFileHeaders(this.generateFileHeaders);
        coreGenerator.setLogger(new MavenReladomoLogger(this.getLog()));
        coreGenerator.execute();

        Resource resource = new Resource();
        resource.setDirectory(this.outputDirectory.getAbsolutePath());
        // TODO: Should be based on the output path
        resource.setTargetPath("sql");
        this.mavenProject.addResource(resource);
    }
}
