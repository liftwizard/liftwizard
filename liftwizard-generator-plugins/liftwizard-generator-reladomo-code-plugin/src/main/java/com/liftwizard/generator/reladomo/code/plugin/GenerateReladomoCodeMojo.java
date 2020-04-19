package com.liftwizard.generator.reladomo.code.plugin;

import java.io.File;

import com.gs.fw.common.mithra.generator.CoreMithraGenerator;
import com.liftwizard.maven.reladomo.logger.MavenReladomoLogger;
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
public class GenerateReladomoCodeMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(
            property = "definitionsXmlFile",
            required = true,
            readonly = true)
    private File definitionsXmlFile;

    @Parameter(property = "generatedDir", defaultValue = "${project.build.directory}/generated-sources/reladomo")
    private File generatedDir;

    @Parameter(property = "nonGeneratedDir", defaultValue = "${project.build.sourceDirectory}")
    private File nonGeneratedDir;

    @Parameter(property = "generateConcreteClasses", defaultValue = "true")
    private final boolean generateConcreteClasses  = true;
    @Parameter(property = "warnAboutConcreteClasses", defaultValue = "true")
    private final boolean warnAboutConcreteClasses = true;
    @Parameter(property = "generateEcListMethod", defaultValue = "true")
    private final boolean generateEcListMethod     = true;

    @Override
    public void execute()
    {
        if (!this.generatedDir.exists())
        {
            this.generatedDir.mkdirs();
        }

        CoreMithraGenerator coreGenerator = new CoreMithraGenerator();
        coreGenerator.setXml(this.definitionsXmlFile.getAbsolutePath());
        coreGenerator.setGeneratedDir(this.generatedDir.getAbsolutePath());
        coreGenerator.setNonGeneratedDir(this.nonGeneratedDir.getAbsolutePath());
        coreGenerator.setGenerateConcreteClasses(this.generateConcreteClasses);
        coreGenerator.setWarnAboutConcreteClasses(this.warnAboutConcreteClasses);
        coreGenerator.setGenerateEcListMethod(this.generateEcListMethod);
        coreGenerator.setLogger(new MavenReladomoLogger(this.getLog()));
        coreGenerator.execute();

        this.mavenProject.addCompileSourceRoot(this.generatedDir.getAbsolutePath());
    }
}
