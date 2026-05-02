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

class Dropwizard3IAccessEventGetSequenceNumberTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new Dropwizard3IAccessEventGetSequenceNumber())
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
						"""
						package ch.qos.logback.access.spi;

						public interface IAccessEvent {
						    String getRequestURL();
						    long getSequenceNumber();
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
					import ch.qos.logback.access.spi.IAccessEvent;

					class FakeAccessEvent implements IAccessEvent {

					    @Override
					    public String getRequestURL() {
					        return "https://example.com";
					    }
					}
					""",
					"""
					import ch.qos.logback.access.spi.IAccessEvent;

					class FakeAccessEvent implements IAccessEvent {

					    @Override
					    public String getRequestURL() {
					        return "https://example.com";
					    }

					    @Override
					    public long getSequenceNumber() {
					        throw new UnsupportedOperationException(
					                this.getClass().getSimpleName() + ".getSequenceNumber() not implemented yet"
					        );
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
					import ch.qos.logback.access.spi.IAccessEvent;

					// Already declares getSequenceNumber — leave alone.
					class AlreadyImplemented implements IAccessEvent {

					    @Override
					    public String getRequestURL() {
					        return "url";
					    }

					    @Override
					    public long getSequenceNumber() {
					        return 42L;
					    }
					}

					// Not an IAccessEvent — even if name collides, leave alone.
					class Unrelated {

					    public long getRequestURL() {
					        return 0;
					    }
					}

					// Interface that re-declares the method but isn't a concrete impl — skip.
					interface SubAccessEvent extends IAccessEvent {
					    String extraMethod();
					}
					"""
				)
			);
	}
}
