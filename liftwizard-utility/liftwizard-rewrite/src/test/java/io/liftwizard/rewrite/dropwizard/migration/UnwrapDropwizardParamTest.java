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

import io.liftwizard.rewrite.dropwizard.UnwrapDropwizardParam;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UnwrapDropwizardParamTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new UnwrapDropwizardParam(
				"io.dropwizard.jersey.params.BooleanParam",
				"java.lang.Boolean"
			))
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					"""
					package io.dropwizard.jersey.params;

					public class BooleanParam {
					    private final Boolean value;
					    public BooleanParam(String input) {
					        this.value = Boolean.valueOf(input);
					    }
					    public Boolean get() {
					        return this.value;
					    }
					}
					"""
				)
			);
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
			java(
				"""
				import io.dropwizard.jersey.params.BooleanParam;

				class Test {
				    void parameterUsage(BooleanParam flag) {
				        Boolean value = flag.get();
				    }

				    void localVariable() {
				        BooleanParam param = new BooleanParam("true");
				        boolean result = param.get();
				    }

				    Boolean returnValue(BooleanParam param) {
				        return param.get();
				    }

				    void passToMethod(BooleanParam param) {
				        System.out.println(param.get());
				    }
				}
				""",
				"""
				class Test {
				    void parameterUsage(Boolean flag) {
				        Boolean value = flag;
				    }

				    void localVariable() {
				        Boolean param = new Boolean("true");
				        boolean result = param;
				    }

				    Boolean returnValue(Boolean param) {
				        return param;
				    }

				    void passToMethod(Boolean param) {
				        System.out.println(param);
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
				class Test {
				    void alreadyUsingBoolean(Boolean flag) {
				        boolean result = flag;
				    }

				    void primitiveBoolean(boolean flag) {
				        boolean result = flag;
				    }
				}
				"""
			)
		);
	}
}
