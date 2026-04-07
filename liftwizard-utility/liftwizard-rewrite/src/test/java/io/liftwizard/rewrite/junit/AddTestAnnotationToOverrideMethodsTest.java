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

package io.liftwizard.rewrite.junit;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddTestAnnotationToOverrideMethodsTest implements RewriteTest {

	@Override
	public void defaults(final RecipeSpec spec) {
		spec
			.recipe(new AddTestAnnotationToOverrideMethods())
			.parser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.junit.jupiter.api.Test;

					public class ParentTest {
					    @Test
					    public void testOne() {
					    }

					    @Test
					    public void testTwo() {
					    }

					    public void helperMethod() {
					    }
					}
					"""
				),
				java(
					"""
					public class ChildTest extends ParentTest {
					    @Override
					    public void testOne() {
					    }

					    @Override
					    public void testTwo() {
					    }

					    @Override
					    public void helperMethod() {
					    }
					}
					""",
					"""
					import org.junit.jupiter.api.Test;

					public class ChildTest extends ParentTest {
					    @Override
					    @Test
					    public void testOne() {
					    }

					    @Override
					    @Test
					    public void testTwo() {
					    }

					    @Override
					    public void helperMethod() {
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
					import org.junit.jupiter.api.BeforeEach;
					import org.junit.jupiter.api.Test;

					public class ParentNegative {
					    @Test
					    public void testMethod() {
					    }

					    public void helperMethod() {
					    }

					    @BeforeEach
					    public void setUp() {
					    }
					}
					"""
				),
				java(
					"""
					import org.junit.jupiter.api.Test;

					public class ChildNegative extends ParentNegative {
					    @Test
					    @Override
					    public void testMethod() {
					    }

					    @Override
					    public void helperMethod() {
					    }

					    @Override
					    public void setUp() {
					    }

					    public void localMethod() {
					    }
					}
					"""
				)
			);
	}
}
