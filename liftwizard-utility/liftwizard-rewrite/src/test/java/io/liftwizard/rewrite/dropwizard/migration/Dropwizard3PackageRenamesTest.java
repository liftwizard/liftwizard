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
	void replacesCorePackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.Application;
					import io.dropwizard.Configuration;

					class MyApp extends Application<Configuration> {
					    public void run(String... args) {}
					}
					""",
					"""
					import io.dropwizard.core.Application;
					import io.dropwizard.core.Configuration;

					class MyApp extends Application<Configuration> {
					    public void run(String... args) {}
					}
					"""
				)
			);
	}

	@Test
	void replacesSetupPackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.setup.Bootstrap;
					import io.dropwizard.setup.Environment;

					class MyBundle {
					    void initialize(Bootstrap<?> bootstrap) {}
					    void run(Environment environment) {}
					}
					""",
					"""
					import io.dropwizard.core.setup.Bootstrap;
					import io.dropwizard.core.setup.Environment;

					class MyBundle {
					    void initialize(Bootstrap<?> bootstrap) {}
					    void run(Environment environment) {}
					}
					"""
				)
			);
	}

	@Test
	void replacesCliPackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.cli.Command;
					import io.dropwizard.cli.ConfiguredCommand;

					abstract class MyCommand extends Command {
					    public void run(Object environment, Object namespace) {}
					}
					""",
					"""
					import io.dropwizard.core.cli.Command;
					import io.dropwizard.core.cli.ConfiguredCommand;

					abstract class MyCommand extends Command {
					    public void run(Object environment, Object namespace) {}
					}
					"""
				)
			);
	}

	@Test
	void replacesLoggingPackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.logging.AbstractAppenderFactory;
					import io.dropwizard.logging.filter.FilterFactory;
					import io.dropwizard.logging.layout.LayoutFactory;

					class MyAppender {
					    FilterFactory<?> filterFactory;
					    LayoutFactory<?> layoutFactory;
					}
					""",
					"""
					import io.dropwizard.logging.common.AbstractAppenderFactory;
					import io.dropwizard.logging.common.filter.FilterFactory;
					import io.dropwizard.logging.common.layout.LayoutFactory;

					class MyAppender {
					    FilterFactory<?> filterFactory;
					    LayoutFactory<?> layoutFactory;
					}
					"""
				)
			);
	}

	@Test
	void replacesMetricsPackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.metrics.ReporterFactory;

					class MyReporter implements ReporterFactory {
					}
					""",
					"""
					import io.dropwizard.metrics.common.ReporterFactory;

					class MyReporter implements ReporterFactory {
					}
					"""
				)
			);
	}

	@Test
	void replacesViewsPackage() {
		this.rewriteRun(
				java(
					"""
					import io.dropwizard.views.View;

					class MyView extends View {
					    protected MyView() {
					        super("my-template.ftl");
					    }
					}
					""",
					"""
					import io.dropwizard.views.common.View;

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
	void doesNotRenameUnaffectedPackages() {
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
