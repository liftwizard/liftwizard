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

class Dropwizard3JerseyParamMigrationTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipeFromResources("io.liftwizard.rewrite.dropwizard.Dropwizard3JerseyParamMigration")
			.parser(
				JavaParser.fromJavaVersion().dependsOn(
					"""
					package io.dropwizard.jersey.params;

					public class InstantParam {
					    public java.time.Instant get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class LocalDateParam {
					    public java.time.LocalDate get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class DateTimeParam {
					    public Object get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class BooleanParam {
					    public Boolean get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class DurationParam {
					    public java.time.Duration get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class SizeParam {
					    public Object get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.jsr310;

					public class InstantParam {
					    public java.time.Instant get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.jsr310;

					public class LocalDateParam {
					    public java.time.LocalDate get() { return null; }
					}
					""",
					"""
					package io.dropwizard.jersey.jsr310;

					public class ZonedDateTimeParam {
					    public java.time.ZonedDateTime get() { return null; }
					}
					""",
					"""
					package io.dropwizard.util;

					public class DataSize {
					}
					""",
					"""
					package io.dropwizard.jersey.params;

					public class IntParam {
					    public int get() { return 0; }
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
				import io.dropwizard.jersey.params.InstantParam;
				import io.dropwizard.jersey.params.LocalDateParam;
				import io.dropwizard.jersey.params.DateTimeParam;
				import io.dropwizard.jersey.params.BooleanParam;
				import io.dropwizard.jersey.params.DurationParam;
				import io.dropwizard.jersey.params.SizeParam;

				class Test {
				    void packageMoves(InstantParam instant, LocalDateParam date) {
				        Object a = instant.get();
				        Object b = date.get();
				    }

				    void dateTimeParamReplacement(DateTimeParam dateTime) {
				        Object value = dateTime.get();
				    }

				    void unwrapBooleanParam(BooleanParam flag) {
				        Boolean value = flag.get();
				    }

				    void unwrapDurationParam(DurationParam duration) {
				        Object value = duration.get();
				    }

				    void unwrapSizeParam(SizeParam size) {
				        Object value = size.get();
				    }
				}
				""",
				"""
				import io.dropwizard.jersey.jsr310.InstantParam;
				import io.dropwizard.jersey.jsr310.LocalDateParam;
				import io.dropwizard.jersey.jsr310.ZonedDateTimeParam;
				import io.dropwizard.util.DataSize;

				import java.time.Duration;

				class Test {
				    void packageMoves(InstantParam instant, LocalDateParam date) {
				        Object a = instant.get();
				        Object b = date.get();
				    }

				    void dateTimeParamReplacement(ZonedDateTimeParam dateTime) {
				        Object value = dateTime.get();
				    }

				    void unwrapBooleanParam(Boolean flag) {
				        Boolean value = flag;
				    }

				    void unwrapDurationParam(Duration duration) {
				        Object value = duration;
				    }

				    void unwrapSizeParam(DataSize size) {
				        Object value = size;
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
				import io.dropwizard.jersey.params.IntParam;

				class Test {
				    void intParamStaysInParams(IntParam intParam) {
				        int value = intParam.get();
				    }

				    void alreadyUsingRawTypes(Boolean flag, java.time.Duration duration) {
				        boolean b = flag;
				        long millis = duration.toMillis();
				    }
				}
				"""
			)
		);
	}
}
