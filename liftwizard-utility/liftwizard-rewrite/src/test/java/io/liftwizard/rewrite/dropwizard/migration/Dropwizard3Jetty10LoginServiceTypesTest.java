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

class Dropwizard3Jetty10LoginServiceTypesTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3Jetty10LoginServiceTypes")
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
						"""
						package org.eclipse.jetty.security;

						public abstract class AbstractLoginService {
						    public static class UserPrincipal {
						        public UserPrincipal(String name, Object credential) {}
						        public String getName() { return null; }
						    }
						    public static class RolePrincipal {
						        public RolePrincipal(String name) {}
						    }
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
					import org.eclipse.jetty.security.AbstractLoginService;
					import org.eclipse.jetty.security.AbstractLoginService.UserPrincipal;
					import org.eclipse.jetty.security.AbstractLoginService.RolePrincipal;

					class AdminLoginService extends AbstractLoginService {

					    private final UserPrincipal adminPrincipal = new UserPrincipal("admin", "secret");

					    String[] loadRoleInfo(UserPrincipal principal) {
					        return new String[] { "admin" };
					    }

					    UserPrincipal loadUserInfo(String userName) {
					        return this.adminPrincipal;
					    }

					    RolePrincipal makeRole(String name) {
					        return new RolePrincipal(name);
					    }
					}
					""",
					"""
					import org.eclipse.jetty.security.AbstractLoginService;
					import org.eclipse.jetty.security.RolePrincipal;
					import org.eclipse.jetty.security.UserPrincipal;

					class AdminLoginService extends AbstractLoginService {

					    private final UserPrincipal adminPrincipal = new UserPrincipal("admin", "secret");

					    String[] loadRoleInfo(UserPrincipal principal) {
					        return new String[] { "admin" };
					    }

					    UserPrincipal loadUserInfo(String userName) {
					        return this.adminPrincipal;
					    }

					    RolePrincipal makeRole(String name) {
					        return new RolePrincipal(name);
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
					class UnrelatedTypes {

					    static class UserPrincipal {
					        UserPrincipal(String name) {}
					    }

					    static class RolePrincipal {
					        RolePrincipal(String name) {}
					    }

					    UserPrincipal getUser() {
					        return new UserPrincipal("alice");
					    }

					    RolePrincipal getRole() {
					        return new RolePrincipal("admin");
					    }
					}
					"""
				)
			);
	}
}
