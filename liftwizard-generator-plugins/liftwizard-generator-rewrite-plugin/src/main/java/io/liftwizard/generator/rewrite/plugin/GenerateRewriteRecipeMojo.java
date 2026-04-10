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

package io.liftwizard.generator.rewrite.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.liftwizard.generator.rewrite.CompositeRecipeGenerator;
import io.liftwizard.generator.rewrite.FilteredRecipeSpec;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openrewrite.config.Environment;

@Mojo(name = "generate-rewrite-recipes", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class GenerateRewriteRecipeMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject mavenProject;

	@Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-resources")
	private File outputDirectory;

	@Parameter(required = true)
	private List<FilteredRecipeSpec> filteredRecipeSpecs;

	@Parameter(property = "indent", defaultValue = "    ")
	private String indent;

	@Override
	public void execute() throws MojoExecutionException {
		Environment env = Environment.builder().scanRuntimeClasspath().build();
		var generator = new CompositeRecipeGenerator(this.indent);

		Path metaInfRewrite = this.outputDirectory.toPath().resolve("META-INF/rewrite");
		try {
			Files.createDirectories(metaInfRewrite);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to create output directory: " + metaInfRewrite, e);
		}

		for (FilteredRecipeSpec spec : this.filteredRecipeSpecs) {
			String yaml = generator.generate(env, spec);
			Path outputFile = metaInfRewrite.resolve(spec.getOutputFileName());
			try {
				Files.writeString(outputFile, yaml, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new MojoExecutionException("Failed to write: " + outputFile, e);
			}
			this.getLog().info("Generated: " + outputFile);
		}

		var resource = new Resource();
		resource.setDirectory(this.outputDirectory.getAbsolutePath());
		this.mavenProject.addResource(resource);
	}
}
