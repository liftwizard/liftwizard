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
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class Dropwizard3LoadRoleInfoSignatureTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new Dropwizard3LoadRoleInfoSignature())
			.typeValidationOptions(TypeValidation.none())
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
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
		this.rewriteRun(
				java(
					"""
					import org.eclipse.jetty.security.AbstractLoginService;
					import org.eclipse.jetty.security.UserPrincipal;

					class AdminLoginService extends AbstractLoginService {

					    private final String adminUserName;
					    private final UserPrincipal adminPrincipal;

					    AdminLoginService(String userName) {
					        this.adminUserName = userName;
					        this.adminPrincipal = new UserPrincipal(userName, "secret");
					    }

					    @Override
					    protected String[] loadRoleInfo(UserPrincipal principal) {
					        if (this.adminUserName.equals(principal.getName())) {
					            return new String[] { "admin" };
					        }
					        if ("multi".equals(principal.getName())) {
					            return new String[] { "admin", "ops" };
					        }
					        return new String[0];
					    }

					    @Override
					    protected UserPrincipal loadUserInfo(String userName) {
					        return this.adminUserName.equals(userName) ? this.adminPrincipal : null;
					    }
					}
					""",
					"""
					import org.eclipse.jetty.security.AbstractLoginService;
					import org.eclipse.jetty.security.RolePrincipal;
					import org.eclipse.jetty.security.UserPrincipal;

					import java.util.List;

					class AdminLoginService extends AbstractLoginService {

					    private final String adminUserName;
					    private final UserPrincipal adminPrincipal;

					    AdminLoginService(String userName) {
					        this.adminUserName = userName;
					        this.adminPrincipal = new UserPrincipal(userName, "secret");
					    }

					    @Override
					    protected List<RolePrincipal> loadRoleInfo(UserPrincipal principal) {
					        if (this.adminUserName.equals(principal.getName())) {
					            return List.of(new RolePrincipal("admin"));
					        }
					        if ("multi".equals(principal.getName())) {
					            return List.of(new RolePrincipal("admin"), new RolePrincipal("ops"));
					        }
					        return List.of();
					    }

					    @Override
					    protected UserPrincipal loadUserInfo(String userName) {
					        return this.adminUserName.equals(userName) ? this.adminPrincipal : null;
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
					import org.eclipse.jetty.security.AbstractLoginService;
					import org.eclipse.jetty.security.UserPrincipal;

					class Sibling {

					    // Same name but unrelated containing class — leave alone since UsesType
					    // only fires for the AbstractLoginService-using compilation unit; this
					    // covers the case where loadRoleInfo lives next to one but isn't an
					    // override.
					    String[] loadRoleInfo(String unrelated) {
					        return new String[] { unrelated };
					    }

					    // Different return type — already migrated, no-op.
					    java.util.List<String> alreadyMigrated(UserPrincipal principal) {
					        return java.util.List.of(principal.getName());
					    }

					    // Two-arg overload — wrong arity, leave alone.
					    String[] loadRoleInfo(UserPrincipal principal, int extra) {
					        return new String[0];
					    }

					    // String[] return value that has nothing to do with loadRoleInfo —
					    // since it's not inside a target method, must not be rewritten.
					    String[] unrelated() {
					        return new String[] { "keep" };
					    }

					    // Force the AbstractLoginService UsesType precondition to fire so the
					    // visitor actually traverses this CU.
					    AbstractLoginService anchor() {
					        return null;
					    }
					}
					"""
				)
			);
	}
}
