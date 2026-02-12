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

package io.liftwizard.rewrite.dropwizard.testing;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class DropwizardTestingJUnit5MigrationTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources(
				"io.liftwizard.rewrite.dropwizard.testing.DropwizardTestingJUnit5Migration"
			)
			.parser(
				JavaParser.fromJavaVersion()
					.dependsOn(
						"""
						package io.dropwizard.testing.junit;

						public class DropwizardAppRule<C> {
						    public DropwizardAppRule(Class<?> applicationClass, String configPath) {}
						}
						""",
						"""
						package io.dropwizard.testing.junit;

						public class DropwizardClientRule {
						    public DropwizardClientRule(Object... resources) {}
						}
						""",
						"""
						package io.dropwizard.testing.junit;

						public class ResourceTestRule {
						    public static Builder builder() {
						        return new Builder();
						    }

						    public static class Builder {
						        public Builder addResource(Object resource) {
						            return this;
						        }

						        public ResourceTestRule build() {
						            return new ResourceTestRule();
						        }
						    }
						}
						""",
						"""
						package io.liftwizard.junit.extension.app;

						public class LiftwizardAppExtension<C> {
						    public LiftwizardAppExtension(Class<?> applicationClass, String configPath) {}
						}
						""",
						"""
						package io.dropwizard.testing.junit5;

						public class DropwizardClientExtension {
						    public DropwizardClientExtension(Object... resources) {}
						}
						""",
						"""
						package io.dropwizard.testing.junit5;

						public class ResourceExtension {
						    public static Builder builder() {
						        return new Builder();
						    }

						    public static class Builder {
						        public Builder addResource(Object resource) {
						            return this;
						        }

						        public ResourceExtension build() {
						            return new ResourceExtension();
						        }
						    }
						}
						""",
						"""
						package io.dropwizard.testing.junit5;

						public class DropwizardExtensionsSupport {
						}
						""",
						"""
						package org.junit;

						import java.lang.annotation.*;

						@Retention(RetentionPolicy.RUNTIME)
						@Target({ElementType.FIELD})
						public @interface ClassRule {
						}
						""",
						"""
						package org.junit;

						import java.lang.annotation.*;

						@Retention(RetentionPolicy.RUNTIME)
						@Target({ElementType.FIELD})
						public @interface Rule {
						}
						"""
					)
					.classpath("junit-jupiter-api")
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.junit.ClassRule;
					import org.junit.Rule;
					import io.dropwizard.testing.junit.DropwizardAppRule;
					import io.dropwizard.testing.junit.DropwizardClientRule;
					import io.dropwizard.testing.junit.ResourceTestRule;

					class MyTest {
					    @ClassRule
					    public static DropwizardAppRule<Object> APP_RULE =
					            new DropwizardAppRule<>(Object.class, "config.yml");

					    @Rule
					    public DropwizardAppRule<Object> instanceRule =
					            new DropwizardAppRule<>(Object.class, "config.yml");

					    @ClassRule
					    public static DropwizardClientRule CLIENT_RULE =
					            new DropwizardClientRule(new Object());

					    @ClassRule
					    public static ResourceTestRule RESOURCES = ResourceTestRule.builder()
					            .addResource(new Object())
					            .build();
					}
					""",
					"""
					import io.dropwizard.testing.junit5.DropwizardClientExtension;
					import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
					import io.dropwizard.testing.junit5.ResourceExtension;
					import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
					import org.junit.jupiter.api.extension.ExtendWith;
					import org.junit.jupiter.api.extension.RegisterExtension;

					@ExtendWith(DropwizardExtensionsSupport.class)
					class MyTest {
					    @RegisterExtension
					    public static LiftwizardAppExtension<Object> APP_RULE =
					            new LiftwizardAppExtension<>(Object.class, "config.yml");

					    @RegisterExtension
					    public LiftwizardAppExtension<Object> instanceRule =
					            new LiftwizardAppExtension<>(Object.class, "config.yml");

					    @RegisterExtension
					    public static DropwizardClientExtension CLIENT_RULE =
					            new DropwizardClientExtension(new Object());

					    @RegisterExtension
					    public static ResourceExtension RESOURCES = ResourceExtension.builder()
					            .addResource(new Object())
					            .build();
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
					import io.dropwizard.testing.junit5.DropwizardClientExtension;
					import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
					import io.dropwizard.testing.junit5.ResourceExtension;
					import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
					import org.junit.jupiter.api.extension.ExtendWith;
					import org.junit.jupiter.api.extension.RegisterExtension;

					@ExtendWith(DropwizardExtensionsSupport.class)
					class MyTest {
					    @RegisterExtension
					    public static LiftwizardAppExtension<Object> APP_RULE =
					            new LiftwizardAppExtension<>(Object.class, "config.yml");

					    @RegisterExtension
					    public static DropwizardClientExtension CLIENT_RULE =
					            new DropwizardClientExtension(new Object());

					    @RegisterExtension
					    public static ResourceExtension RESOURCES = ResourceExtension.builder()
					            .addResource(new Object())
					            .build();
					}
					"""
				)
			);
	}
}
