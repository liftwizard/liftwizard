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

package io.liftwizard.rewrite.java.style;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class EnforceConsistentAnnotationArgumentLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentAnnotationArgumentLineWrapping());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.lang.annotation.Repeatable;

					@Repeatable(MyAnnotation.Container.class)
					@interface MyAnnotation {
					    String name() default "";
					    String value() default "";
					    int count() default 0;
					    boolean enabled() default false;
					    String description() default "";

					    @interface Container {
					        MyAnnotation[] value();
					    }
					}

					class Foo {
					    @MyAnnotation(name = "first",
					            value = "second",
					            count = 1)
					    void method1() {}

					    @MyAnnotation(name = "first",
					            value = "second")
					    void method2() {}

					    @MyAnnotation(
					            name = "first", value = "second", count = 1)
					    void method3() {}

					    @MyAnnotation(
					            name = "first", value = "second", count = 1,
					            enabled = true, description = "fifth")
					    void method4() {}
					}""",
					"""
					import java.lang.annotation.Repeatable;

					@Repeatable(MyAnnotation.Container.class)
					@interface MyAnnotation {
					    String name() default "";
					    String value() default "";
					    int count() default 0;
					    boolean enabled() default false;
					    String description() default "";

					    @interface Container {
					        MyAnnotation[] value();
					    }
					}

					class Foo {
					    @MyAnnotation(
					            name = "first",
					            value = "second",
					            count = 1)
					    void method1() {}

					    @MyAnnotation(
					            name = "first",
					            value = "second")
					    void method2() {}

					    @MyAnnotation(
					            name = "first",
					            value = "second",
					            count = 1)
					    void method3() {}

					    @MyAnnotation(
					            name = "first",
					            value = "second",
					            count = 1,
					            enabled = true,
					            description = "fifth")
					    void method4() {}
					}"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import java.lang.annotation.Repeatable;

					@Repeatable(MyAnnotation.Container.class)
					@interface MyAnnotation {
					    String name() default "";
					    String value() default "";
					    int count() default 0;
					    boolean enabled() default false;
					    String description() default "";

					    @interface Container {
					        MyAnnotation[] value();
					    }
					}

					class Foo {
					    @MyAnnotation(name = "first", value = "second", count = 1)
					    void method1() {}

					    @MyAnnotation(name = "first")
					    void method2() {}

					    @MyAnnotation(
					            name = "first",
					            value = "second",
					            count = 1)
					    void method3() {}

					    @MyAnnotation
					    void method4() {}

					    @MyAnnotation(
					            name = "first", value = "second",
					            count = 1, enabled = true)
					    void method5() {}
					}"""
				)
			);
	}
}
