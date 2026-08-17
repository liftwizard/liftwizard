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

package io.liftwizard.rewrite.logging;

import io.liftwizard.rewrite.AbstractRewriteFixtures;
import io.liftwizard.rewrite.AbstractRewriteStyles;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class UsesLog4j1ObjectLoggingTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new UsesLog4j1ObjectLogging())
			.parser(
				JavaParser.fromJavaVersion()
					.styles(AbstractRewriteStyles.styles())
					.classpathFromResources(new InMemoryExecutionContext(), "reload4j")
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

	@Test
	void detectsObjectArgumentWhenLoggerInheritanceIsSevered() {
		String severedLoggerStub = """
			package org.apache.log4j;
			public class Logger {
			    public static Logger getLogger(Class clazz) { return null; }
			    public void trace(Object message) {}
			    public void debug(Object message) {}
			    public void info(Object message) {}
			    public void warn(Object message) {}
			    public void error(Object message) {}
			    public void fatal(Object message) {}
			}
			""";

		this.rewriteRun(
			(spec) ->
				spec.parser(
					JavaParser.fromJavaVersion().styles(AbstractRewriteStyles.styles()).dependsOn(severedLoggerStub)
				),
			this.javaFixture("detectsObjectArgumentWhenLoggerInheritanceIsSevered/01")
		);
	}
}
