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
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.maven.Assertions.pomXml;

class Dropwizard3SwitchToLiftwizardRedirectBundleTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3SwitchToLiftwizardRedirectBundle")
			.typeValidationOptions(TypeValidation.builder().dependencyModel(false).build())
			.markerPrinter(PrintOutputCapture.MarkerPrinter.SANITIZED);
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
					            <groupId>io.dropwizard-bundles</groupId>
					            <artifactId>dropwizard-redirect-bundle</artifactId>
					            <version>1.3.5</version>
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
					            <groupId>io.liftwizard</groupId>
					            <artifactId>liftwizard-redirect-bundle</artifactId>
					            <version>${project.version}</version>
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
					            <groupId>io.dropwizard</groupId>
					            <artifactId>dropwizard-core</artifactId>
					            <version>3.0.17</version>
					        </dependency>
					    </dependencies>
					</project>
					"""
				)
			);
	}
}
