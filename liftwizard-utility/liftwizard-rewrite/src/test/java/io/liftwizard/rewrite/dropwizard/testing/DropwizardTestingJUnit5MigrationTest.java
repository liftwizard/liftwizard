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

import io.liftwizard.rewrite.AbstractRewriteFixtures;
import io.liftwizard.rewrite.AbstractRewriteStyles;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class DropwizardTestingJUnit5MigrationTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.testing.DropwizardTestingJUnit5Migration")
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
					.styles(AbstractRewriteStyles.styles())
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(this.javaFixture("replacePatterns/01"));
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"));
	}
}
