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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

class Dropwizard3ExcludeStaleJettyServletApiTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3ExcludeStaleJettyServletApi");
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				pomXml(
					"""
					<?xml version="1.0" encoding="UTF-8"?>
					<project xmlns="http://maven.apache.org/POM/4.0.0">
					    <modelVersion>4.0.0</modelVersion>
					    <groupId>com.example</groupId>
					    <artifactId>example</artifactId>
					    <version>1.0.0</version>
					    <dependencies>
					        <dependency>
					            <groupId>org.eclipse.jetty</groupId>
					            <artifactId>jetty-server</artifactId>
					            <version>10.0.26</version>
					        </dependency>
					    </dependencies>
					</project>
					""",
					"""
					<?xml version="1.0" encoding="UTF-8"?>
					<project xmlns="http://maven.apache.org/POM/4.0.0">
					    <modelVersion>4.0.0</modelVersion>
					    <groupId>com.example</groupId>
					    <artifactId>example</artifactId>
					    <version>1.0.0</version>
					    <dependencies>
					        <dependency>
					            <groupId>org.eclipse.jetty</groupId>
					            <artifactId>jetty-server</artifactId>
					            <version>10.0.26</version>
					            <exclusions>
					                <exclusion>
					                    <groupId>org.eclipse.jetty.toolchain</groupId>
					                    <artifactId>jetty-servlet-api</artifactId>
					                </exclusion>
					            </exclusions>
					        </dependency>
					    </dependencies>
					</project>
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				pomXml(
					"""
					<?xml version="1.0" encoding="UTF-8"?>
					<project xmlns="http://maven.apache.org/POM/4.0.0">
					    <modelVersion>4.0.0</modelVersion>
					    <groupId>com.example</groupId>
					    <artifactId>example</artifactId>
					    <version>1.0.0</version>
					    <dependencies>
					        <dependency>
					            <groupId>com.fasterxml.jackson.core</groupId>
					            <artifactId>jackson-databind</artifactId>
					            <version>2.21.3</version>
					        </dependency>
					    </dependencies>
					</project>
					"""
				)
			);
	}
}
