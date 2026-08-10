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

import io.liftwizard.rewrite.AbstractRewriteFixtures;
import io.liftwizard.rewrite.AbstractRewriteStyles;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

class Dropwizard3LoadRoleInfoSignatureTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new Dropwizard3LoadRoleInfoSignature())
			.typeValidationOptions(TypeValidation.none())
			.parser(
				JavaParser.fromJavaVersion()
					.styles(AbstractRewriteStyles.styles())
					.dependsOn(
						"""
						package org.eclipse.jetty.security;

						public abstract class AbstractLoginService {
						    protected abstract String[] loadRoleInfo(UserPrincipal principal);
						    protected abstract UserPrincipal loadUserInfo(String userName);
						}
						""",
						"""
						package org.eclipse.jetty.security;

						public class UserPrincipal {
						    public UserPrincipal(String name, Object credential) {}
						    public String getName() { return null; }
						}
						""",
						"""
						package org.eclipse.jetty.security;

						public class RolePrincipal {
						    public RolePrincipal(String name) {}
						}
						"""
					)
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
