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

import static org.openrewrite.test.SourceSpecs.text;

class Dropwizard3LogbackConversionWordsTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3LogbackConversionWords");
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(text("""
				<?xml version="1.0" encoding="UTF-8"?>

				<configuration>
				    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%rootException</pattern>
				        </encoder>
				    </appender>
				    <appender name="Short" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%rEx</pattern>
				        </encoder>
				    </appender>
				    <appender name="Extended" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%xException</pattern>
				        </encoder>
				    </appender>
				    <appender name="XThrow" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%xThrowable</pattern>
				        </encoder>
				    </appender>
				    <appender name="XShort" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%xEx</pattern>
				        </encoder>
				    </appender>
				    <appender name="Full" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%exception</pattern>
				        </encoder>
				    </appender>
				    <appender name="Throw" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%throwable</pattern>
				        </encoder>
				    </appender>
				    <appender name="Ex" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%ex</pattern>
				        </encoder>
				    </appender>
				</configuration>
				""", """
				<?xml version="1.0" encoding="UTF-8"?>

				<configuration>
				    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwRootException</pattern>
				        </encoder>
				    </appender>
				    <appender name="Short" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwREx</pattern>
				        </encoder>
				    </appender>
				    <appender name="Extended" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwXException</pattern>
				        </encoder>
				    </appender>
				    <appender name="XThrow" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwXThrowable</pattern>
				        </encoder>
				    </appender>
				    <appender name="XShort" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwXEx</pattern>
				        </encoder>
				    </appender>
				    <appender name="Full" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwException</pattern>
				        </encoder>
				    </appender>
				    <appender name="Throw" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwThrowable</pattern>
				        </encoder>
				    </appender>
				    <appender name="Ex" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwEx</pattern>
				        </encoder>
				    </appender>
				</configuration>
				""", (spec) -> spec.path("logback-test.xml")));
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(text("""
				<?xml version="1.0" encoding="UTF-8"?>

				<configuration>
				    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
				        <encoder>
				            <pattern>%message%n%dwRootException</pattern>
				        </encoder>
				    </appender>
				</configuration>
				""", (spec) -> spec.path("logback-test.xml")));
	}
}
