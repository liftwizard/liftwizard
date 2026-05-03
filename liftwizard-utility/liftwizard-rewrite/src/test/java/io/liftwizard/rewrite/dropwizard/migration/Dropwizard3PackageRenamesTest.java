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

package io.liftwizard.rewrite.dropwizard.migration;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class Dropwizard3PackageRenamesTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3PackageRenames")
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					"""
					package io.dropwizard;

					public class Application<T> {
					    public void run(String... args) {}
					}
					""",
					"""
					package io.dropwizard;

					public interface Bundle {
					}
					""",
					"""
					package io.dropwizard;

					public class Configuration {
					}
					""",
					"""
					package io.dropwizard;

					public interface ConfiguredBundle<T> {
					    void initialize(Object bootstrap);
					    void run(T configuration, Object environment);
					}
					""",
					"""
					package io.dropwizard.server;

					public interface ServerFactory {
					}
					""",
					"""
					package io.dropwizard.setup;

					public class Bootstrap<T> {
					    public void addBundle(Object bundle) {}
					}
					""",
					"""
					package io.dropwizard.setup;

					public class Environment {
					    public String getName() { return null; }
					}
					""",
					"""
					package io.dropwizard.cli;

					public abstract class Command {
					    public abstract void run(Object environment, Object namespace);
					}
					""",
					"""
					package io.dropwizard.cli;

					public abstract class ConfiguredCommand<T> extends Command {
					}
					""",
					"""
					package io.dropwizard.logging;

					public abstract class AbstractAppenderFactory<E> {
					}
					""",
					"""
					package io.dropwizard.logging.filter;

					public interface FilterFactory<E> {
					}
					""",
					"""
					package io.dropwizard.logging.layout;

					public interface LayoutFactory<E> {
					}
					""",
					"""
					package io.dropwizard.metrics;

					public interface ReporterFactory {
					}
					""",
					"""
					package io.dropwizard.views;

					public abstract class View {
					    protected View(String templateName) {}
					}
					""",
					"""
					package io.dropwizard.auth;

					public class AuthFilter {
					}
					""",
					"""
					package io.dropwizard.jersey.setup;

					public class JerseyEnvironment {
					}
					"""
				)
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
			java(
				"""
				import io.dropwizard.Application;
				import io.dropwizard.Configuration;
				import io.dropwizard.cli.Command;
				import io.dropwizard.cli.ConfiguredCommand;
				import io.dropwizard.logging.AbstractAppenderFactory;
				import io.dropwizard.logging.filter.FilterFactory;
				import io.dropwizard.logging.layout.LayoutFactory;
				import io.dropwizard.metrics.ReporterFactory;
				import io.dropwizard.setup.Bootstrap;
				import io.dropwizard.setup.Environment;
				import io.dropwizard.views.View;

				class MyApp extends Application<Configuration> {
				    public void run(String... args) {}

				    void setupPackage(Bootstrap<?> bootstrap, Environment environment) {}

				    void loggingPackage(FilterFactory<?> filterFactory, LayoutFactory<?> layoutFactory) {}

				    void metricsPackage(ReporterFactory reporterFactory) {}
				}

				abstract class MyCommand extends Command {
				    public void run(Object environment, Object namespace) {}
				}

				class MyView extends View {
				    protected MyView() {
				        super("my-template.ftl");
				    }
				}
				""",
				"""
				import io.dropwizard.core.cli.Command;
				import io.dropwizard.core.cli.ConfiguredCommand;
				import io.dropwizard.core.Application;
				import io.dropwizard.core.Configuration;
				import io.dropwizard.logging.common.AbstractAppenderFactory;
				import io.dropwizard.logging.common.filter.FilterFactory;
				import io.dropwizard.logging.common.layout.LayoutFactory;
				import io.dropwizard.metrics.common.ReporterFactory;
				import io.dropwizard.core.setup.Bootstrap;
				import io.dropwizard.core.setup.Environment;
				import io.dropwizard.views.common.View;

				class MyApp extends Application<Configuration> {
				    public void run(String... args) {}

				    void setupPackage(Bootstrap<?> bootstrap, Environment environment) {}

				    void loggingPackage(FilterFactory<?> filterFactory, LayoutFactory<?> layoutFactory) {}

				    void metricsPackage(ReporterFactory reporterFactory) {}
				}

				abstract class MyCommand extends Command {
				    public void run(Object environment, Object namespace) {}
				}

				class MyView extends View {
				    protected MyView() {
				        super("my-template.ftl");
				    }
				}
				"""
			)
		);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
			java(
				"""
				import io.dropwizard.auth.AuthFilter;
				import io.dropwizard.jersey.setup.JerseyEnvironment;

				class MyResource {
				    AuthFilter filter;
				    JerseyEnvironment jersey;
				}
				"""
			)
		);
	}
}
