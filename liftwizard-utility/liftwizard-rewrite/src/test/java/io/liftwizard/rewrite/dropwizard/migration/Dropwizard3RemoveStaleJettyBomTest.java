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

class Dropwizard3RemoveStaleJettyBomTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3RemoveStaleJettyBom");
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
					    <dependencyManagement>
					        <dependencies>
					            <dependency>
					                <groupId>org.eclipse.jetty</groupId>
					                <artifactId>jetty-bom</artifactId>
					                <version>9.4.57.v20241219</version>
					                <type>pom</type>
					                <scope>import</scope>
					            </dependency>
					            <dependency>
					                <groupId>io.dropwizard</groupId>
					                <artifactId>dropwizard-bom</artifactId>
					                <version>3.0.17</version>
					                <type>pom</type>
					                <scope>import</scope>
					            </dependency>
					        </dependencies>
					    </dependencyManagement>
					</project>
					""",
					"""
					<?xml version="1.0" encoding="UTF-8"?>
					<project xmlns="http://maven.apache.org/POM/4.0.0">
					    <modelVersion>4.0.0</modelVersion>
					    <groupId>com.example</groupId>
					    <artifactId>example</artifactId>
					    <version>1.0.0</version>
					    <dependencyManagement>
					        <dependencies>
					            <dependency>
					                <groupId>io.dropwizard</groupId>
					                <artifactId>dropwizard-bom</artifactId>
					                <version>3.0.17</version>
					                <type>pom</type>
					                <scope>import</scope>
					            </dependency>
					        </dependencies>
					    </dependencyManagement>
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
					    <dependencyManagement>
					        <dependencies>
					            <dependency>
					                <groupId>io.dropwizard</groupId>
					                <artifactId>dropwizard-bom</artifactId>
					                <version>3.0.17</version>
					                <type>pom</type>
					                <scope>import</scope>
					            </dependency>
					            <dependency>
					                <groupId>com.fasterxml.jackson</groupId>
					                <artifactId>jackson-bom</artifactId>
					                <version>2.21.3</version>
					                <type>pom</type>
					                <scope>import</scope>
					            </dependency>
					        </dependencies>
					    </dependencyManagement>
					</project>
					"""
				)
			);
	}
}
