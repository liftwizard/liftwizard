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

class Dropwizard3Jetty10MigrationTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3Jetty10Migration")
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					"""
					package org.eclipse.jetty.util.component;

					import java.util.EventListener;

					public abstract class AbstractLifeCycle implements LifeCycle {
					    public void addLifeCycleListener(LifeCycle.Listener listener) {}
					    public void addEventListener(EventListener listener) {}
					}
					""",
					"""
					package org.eclipse.jetty.util.component;

					import java.util.EventListener;

					public interface LifeCycle {
					    void addLifeCycleListener(Listener listener);
					    void addEventListener(EventListener listener);

					    interface Listener extends EventListener {
					    }
					}
					""",
					"""
					package org.eclipse.jetty.util.component;

					public class ContainerLifeCycle extends AbstractLifeCycle {
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
				import org.eclipse.jetty.util.component.AbstractLifeCycle;
				import org.eclipse.jetty.util.component.ContainerLifeCycle;
				import org.eclipse.jetty.util.component.LifeCycle;

				class MyComponent {
				    void onAbstractLifeCycle(AbstractLifeCycle lifecycle, LifeCycle.Listener listener) {
				        lifecycle.addLifeCycleListener(listener);
				    }

				    void onContainerLifeCycle(ContainerLifeCycle lifecycle, LifeCycle.Listener listener) {
				        lifecycle.addLifeCycleListener(listener);
				    }
				}
				""",
				"""
				import org.eclipse.jetty.util.component.AbstractLifeCycle;
				import org.eclipse.jetty.util.component.ContainerLifeCycle;
				import org.eclipse.jetty.util.component.LifeCycle;

				class MyComponent {
				    void onAbstractLifeCycle(AbstractLifeCycle lifecycle, LifeCycle.Listener listener) {
				        lifecycle.addEventListener(listener);
				    }

				    void onContainerLifeCycle(ContainerLifeCycle lifecycle, LifeCycle.Listener listener) {
				        lifecycle.addEventListener(listener);
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
				import java.util.EventListener;
				import org.eclipse.jetty.util.component.AbstractLifeCycle;

				class MyComponent {
				    void alreadyUsingAddEventListener(AbstractLifeCycle lifecycle, EventListener listener) {
				        lifecycle.addEventListener(listener);
				    }
				}
				"""
			)
		);
	}
}
