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

class JCFListToMutableListTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFListToMutableList());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Map;
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.impl.list.mutable.FastList;

					class Test {
					    private final List<String> fieldList = Lists.mutable.empty();

					    void test() {
					        List<String> simpleList = Lists.mutable.empty();
					        java.util.List<String> fullyQualifiedList = Lists.mutable.empty();
					        List rawList = Lists.mutable.empty();
					        java.util.List fullyQualifiedRawList = Lists.mutable.empty();
					        List<Map<String, Integer>> nestedGenericsList = Lists.mutable.empty();
					        List<String> fastList = FastList.newList();
					        List<String> list1 = Lists.mutable.empty(), list2 = Lists.mutable.with("a", "b");
					        List<? extends Number> wildcardGenerics = Lists.mutable.empty();
					    }

					    /**
					     * Tests that {@link Map#size()} method works correctly.
					     * Also tests {@link List#size()} method.
					     */
					    void javaDocReference() {
					        List<String> list = Lists.mutable.empty();
					    }
					}

					class InstanceofExample {
					    void method() {
					        Object obj = Lists.mutable.empty();
					        if (obj instanceof List) {
					            List<String> list = Lists.mutable.empty();
					        }
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.Map;
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.list.mutable.FastList;

					class Test {
					    private final MutableList<String> fieldList = Lists.mutable.empty();

					    void test() {
					        MutableList<String> simpleList = Lists.mutable.empty();
					        MutableList<String> fullyQualifiedList = Lists.mutable.empty();
					        MutableList rawList = Lists.mutable.empty();
					        MutableList fullyQualifiedRawList = Lists.mutable.empty();
					        MutableList<Map<String, Integer>> nestedGenericsList = Lists.mutable.empty();
					        MutableList<String> fastList = FastList.newList();
					        MutableList<String> list1 = Lists.mutable.empty(), list2 = Lists.mutable.with("a", "b");
					        MutableList<? extends Number> wildcardGenerics = Lists.mutable.empty();
					    }

					    /**
					     * Tests that {@link Map#size()} method works correctly.
					     * Also tests {@link List#size()} method.
					     */
					    void javaDocReference() {
					        MutableList<String> list = Lists.mutable.empty();
					    }
					}

					class InstanceofExample {
					    void method() {
					        Object obj = Lists.mutable.empty();
					        if (obj instanceof List) {
					            MutableList<String> list = Lists.mutable.empty();
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
					import java.util.ArrayList;
					import java.util.List;
					import org.eclipse.collections.api.factory.Lists;

					class Test {
					    List<String> methodReturnType() {
					        return Lists.mutable.empty();
					    }

					    void methodParameter(List<String> list) {
					        list.size();
					    }

					    void variableWithNonMutableListInitializer() {
					        List<String> arrayList = new ArrayList<>();
					    }

					    void variableWithoutInitializer() {
					        List<String> uninitializedList;
					    }

					    void nonFinalField() {
					        class Inner {
					            private List<String> nonFinalField = Lists.mutable.empty();
					        }
					    }
					}

					interface MyInterface extends List<String> {
					}

					class ImplementsExample implements List<String> {
					}

					class GenericBoundsExample<T extends List<String>> {
					}
					"""
				)
			);
	}
}
