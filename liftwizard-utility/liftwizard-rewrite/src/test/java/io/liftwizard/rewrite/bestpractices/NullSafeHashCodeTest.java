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

package io.liftwizard.rewrite.bestpractices;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class NullSafeHashCodeTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new NullSafeHashCodeRecipes()).parser(JavaParser.fromJavaVersion());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
					    int test(String str, Integer num) {
					        int hash1 = str == null ? 0 : str.hashCode();
					        int hash2 = num == null ? 0 : num.hashCode();
					        int hash3 = str != null ? str.hashCode() : 0;
					        int hash4 = num != null ? num.hashCode() : 0;

					        return str == null ? 0 : str.hashCode();
					    }
					}
					""",
					"""
					import java.util.Objects;

					class Test {
					    int test(String str, Integer num) {
					        int hash1 = Objects.hashCode(str);
					        int hash2 = Objects.hashCode(num);
					        int hash3 = Objects.hashCode(str);
					        int hash4 = Objects.hashCode(num);

					        return Objects.hashCode(str);
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
					class Test {
					    void test(String str) {
					        int simpleHashCode = str.hashCode();
					        int simpleNullCheck = str == null ? 0 : 1;
					        int differentValue = str == null ? 1 : str.hashCode();
					        int invertedDifferentValue = str != null ? str.hashCode() : 1;
					    }
					}
					"""
				)
			);
	}
}
