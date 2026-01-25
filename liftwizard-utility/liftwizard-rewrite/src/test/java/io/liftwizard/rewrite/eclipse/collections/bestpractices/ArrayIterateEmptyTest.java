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

class ArrayIterateEmptyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ArrayIterateEmptyRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class Test {
					    void test(String[] strings, Object[] objects) {
					        boolean isEmptyNullOrLength =
					                strings == null || strings.length == 0;
					        boolean isEmptyNullOrLengthLessOrEqual =
					                strings == null || strings.length <= 0;
					        boolean isEmptyNullOrLengthLessThanOne =
					                strings == null || strings.length < 1;
					        boolean notEmptyNotNullAndLength =
					                objects != null && objects.length > 0;
					        boolean notEmptyNotNullAndLengthNotEqual =
					                objects != null && objects.length != 0;
					        boolean notEmptyNotNullAndLengthGreaterOrEqual =
					                objects != null && objects.length >= 1;

					        if (strings == null || strings.length == 0) {
					        }

					        if (strings == null || strings.length <= 0) {
					        }

					        if (strings == null || strings.length < 1) {
					        }

					        if (objects != null && objects.length > 0) {
					        }

					        if (objects != null && objects.length != 0) {
					        }

					        if (objects != null && objects.length >= 1) {
					        }
					    }

					    boolean testNegatedArrayIterateIsEmpty(String[] array) {
					        return !ArrayIterate.isEmpty(array);
					    }

					    boolean testNegatedArrayIterateNotEmpty(String[] array) {
					        return !ArrayIterate.notEmpty(array);
					    }

					    void testMultipleNegated(String[] array1, Object[] array2) {
					        if (!ArrayIterate.isEmpty(array1)) {
					        }

					        if (!ArrayIterate.notEmpty(array2)) {
					        }
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.utility.ArrayIterate;

					class Test {
					    void test(String[] strings, Object[] objects) {
					        boolean isEmptyNullOrLength =
					                ArrayIterate.isEmpty(strings);
					        boolean isEmptyNullOrLengthLessOrEqual =
					                ArrayIterate.isEmpty(strings);
					        boolean isEmptyNullOrLengthLessThanOne =
					                ArrayIterate.isEmpty(strings);
					        boolean notEmptyNotNullAndLength =
					                ArrayIterate.notEmpty(objects);
					        boolean notEmptyNotNullAndLengthNotEqual =
					                ArrayIterate.notEmpty(objects);
					        boolean notEmptyNotNullAndLengthGreaterOrEqual =
					                ArrayIterate.notEmpty(objects);

					        if (ArrayIterate.isEmpty(strings)) {
					        }

					        if (ArrayIterate.isEmpty(strings)) {
					        }

					        if (ArrayIterate.isEmpty(strings)) {
					        }

					        if (ArrayIterate.notEmpty(objects)) {
					        }

					        if (ArrayIterate.notEmpty(objects)) {
					        }

					        if (ArrayIterate.notEmpty(objects)) {
					        }
					    }

					    boolean testNegatedArrayIterateIsEmpty(String[] array) {
					        return ArrayIterate.notEmpty(array);
					    }

					    boolean testNegatedArrayIterateNotEmpty(String[] array) {
					        return ArrayIterate.isEmpty(array);
					    }

					    void testMultipleNegated(String[] array1, Object[] array2) {
					        if (ArrayIterate.notEmpty(array1)) {
					        }

					        if (ArrayIterate.isEmpty(array2)) {
					        }
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
					    void test(String[] array) {
					        boolean simpleNullCheck =
					                array == null;
					        boolean simpleLengthCheck =
					                array.length == 0;
					        boolean simpleLengthLessOrEqual =
					                array.length <= 0;
					        boolean simpleLengthLessThanOne =
					                array.length < 1;
					        boolean simpleLengthNotEqual =
					                array.length != 0;
					        boolean simpleLengthGreaterOrEqual =
					                array.length >= 1;
					        boolean differentLength =
					                array != null && array.length > 5;
					        boolean wrongOperator =
					                array != null || array.length > 0;
					        boolean wrongOperatorLessOrEqual =
					                array != null || array.length <= 0;
					        boolean wrongOperatorLessThanOne =
					                array != null || array.length < 1;
					        boolean wrongOperatorNotEqual =
					                array == null && array.length != 0;
					        boolean wrongOperatorGreaterOrEqual =
					                array == null && array.length >= 1;
					    }
					}
					"""
				)
			);
	}
}
