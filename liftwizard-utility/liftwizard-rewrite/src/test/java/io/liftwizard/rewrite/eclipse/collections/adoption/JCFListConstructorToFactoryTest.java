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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class JCFListConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFListConstructorToFactory());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.ArrayList;
					import java.util.Arrays;
					import java.util.Collection;
					import java.util.List;

					class Test {
					    private final List<String> fieldInterfaceEmpty = new ArrayList<>();
					    private final List<Integer> fieldInterfaceCapacity = new ArrayList<>(10);
					    private final List<String> fieldInterfaceCollection = new ArrayList<>(Arrays.asList("a", "b"));

					    void test(Collection<String> inputCollection) {
					        Collection<String> collection = new ArrayList<>();
					        List<String> typeInference = new ArrayList<>();
					        List<List<String>> nestedGenerics = new ArrayList<>();
					        List<? extends Number> wildcardGenerics = new ArrayList<>();
					        List<String> explicitSimple = new ArrayList<String>();
					        List<List<String>> explicitNested = new ArrayList<List<String>>();
					        java.util.List<String> fullyQualified = new ArrayList<>();
					        List<String> withCapacity20 = new ArrayList<>(20);
					        List<String> explicit30 = new ArrayList<String>(30);
					        List<String> interfaceFromCollection = new ArrayList<>(inputCollection);
					        List<String> fromList = new ArrayList<>(Arrays.asList("x", "y", "z"));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.Lists;

					import java.util.Arrays;
					import java.util.Collection;
					import java.util.List;

					class Test {
					    private final List<String> fieldInterfaceEmpty = Lists.mutable.empty();
					    private final List<Integer> fieldInterfaceCapacity = Lists.mutable.withInitialCapacity(10);
					    private final List<String> fieldInterfaceCollection = Lists.mutable.withAll(Arrays.asList("a", "b"));

					    void test(Collection<String> inputCollection) {
					        Collection<String> collection = Lists.mutable.empty();
					        List<String> typeInference = Lists.mutable.empty();
					        List<List<String>> nestedGenerics = Lists.mutable.empty();
					        List<? extends Number> wildcardGenerics = Lists.mutable.empty();
					        List<String> explicitSimple = Lists.mutable.<String>empty();
					        List<List<String>> explicitNested = Lists.mutable.<List<String>>empty();
					        java.util.List<String> fullyQualified = Lists.mutable.empty();
					        List<String> withCapacity20 = Lists.mutable.withInitialCapacity(20);
					        List<String> explicit30 = Lists.mutable.<String>withInitialCapacity(30);
					        List<String> interfaceFromCollection = Lists.mutable.withAll(inputCollection);
					        List<String> fromList = Lists.mutable.withAll(Arrays.asList("x", "y", "z"));
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
					import java.util.Collection;

					class Test {
					    private final ArrayList<String> fieldConcreteType = new ArrayList<>();

					    void test(Collection<String> inputCollection) {
					        ArrayList<String> diamondList = new ArrayList<>();
					        ArrayList rawList = new ArrayList();
					        ArrayList<String> withInitialCapacity = new ArrayList<>(10);
					        ArrayList<String> concreteFromCollection = new ArrayList<>(inputCollection);
					    }
					}
					"""
				)
			);
	}
}
