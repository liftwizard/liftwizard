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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECArraysStreamToArrayAdapterTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECArraysStreamToArrayAdapter());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.List;

					class Test {
					    void test(String[] values, String[] names) {
					        var result1 = Arrays.stream(values).toList();
					        List<String> result2 = Arrays.stream(names).toList();
					        var result3 = Arrays.stream(new String[]{"a", "b", "c"}).toList();
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

					import java.util.List;

					class Test {
					    void test(String[] values, String[] names) {
					        var result1 = ArrayAdapter.adapt(values).toList();
					        List<String> result2 = ArrayAdapter.adapt(names).toList();
					        var result3 = ArrayAdapter.adapt(new String[]{"a", "b", "c"}).toList();
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
					import java.util.Arrays;

					class Test {
					    void test(int[] intValues, long[] longValues, double[] doubleValues, String[] values) {
					        var result1 = Arrays.stream(intValues).sum();
					        var result2 = Arrays.stream(longValues).sum();
					        var result3 = Arrays.stream(doubleValues).sum();
					        var result4 = Arrays.stream(values, 1, 3).toList();
					    }
					}
					"""
				)
			);
	}
}
