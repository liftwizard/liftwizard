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

class ECSimplifyMethodReferencesTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSimplifyMethodReferences());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Consumer;

					class Test {
					    void test(
					            MutableList<String> list,
					            Predicate<String> predicate,
					            Function<String, Integer> function,
					            Procedure<String> procedure,
					            java.util.function.Predicate<String> jdkPredicate,
					            java.util.function.Function<String, Integer> jdkFunction,
					            Consumer<String> consumer) {
					        // Eclipse Collections Predicate - accept method
					        MutableList<String> selectAccept = list.select(predicate::accept);
					        MutableList<String> rejectAccept = list.reject(predicate::accept);

					        // Eclipse Collections Predicate - test method (JDK compatible)
					        MutableList<String> selectTest = list.select(predicate::test);
					        MutableList<String> rejectTest = list.reject(predicate::test);

					        // Eclipse Collections Function - valueOf method
					        MutableList<Integer> collectValueOf = list.collect(function::valueOf);

					        // Eclipse Collections Function - apply method (JDK compatible)
					        MutableList<Integer> collectApply = list.collect(function::apply);

					        // Eclipse Collections Procedure - value method
					        list.forEach(procedure::value);

					        // Eclipse Collections Procedure - accept method (JDK compatible)
					        list.forEach(procedure::accept);

					        // JDK Predicate - test method
					        MutableList<String> jdkSelectTest = list.select(jdkPredicate::test);

					        // JDK Function - apply method
					        MutableList<Integer> jdkCollectApply = list.collect(jdkFunction::apply);

					        // JDK Consumer - accept method
					        list.forEach(consumer::accept);

					        // Works in chained calls
					        MutableList<Integer> chained = list
					            .select(predicate::accept)
					            .collect(function::valueOf);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Consumer;

					class Test {
					    void test(
					            MutableList<String> list,
					            Predicate<String> predicate,
					            Function<String, Integer> function,
					            Procedure<String> procedure,
					            java.util.function.Predicate<String> jdkPredicate,
					            java.util.function.Function<String, Integer> jdkFunction,
					            Consumer<String> consumer) {
					        // Eclipse Collections Predicate - accept method
					        MutableList<String> selectAccept = list.select(predicate);
					        MutableList<String> rejectAccept = list.reject(predicate);

					        // Eclipse Collections Predicate - test method (JDK compatible)
					        MutableList<String> selectTest = list.select(predicate);
					        MutableList<String> rejectTest = list.reject(predicate);

					        // Eclipse Collections Function - valueOf method
					        MutableList<Integer> collectValueOf = list.collect(function);

					        // Eclipse Collections Function - apply method (JDK compatible)
					        MutableList<Integer> collectApply = list.collect(function);

					        // Eclipse Collections Procedure - value method
					        list.forEach(procedure);

					        // Eclipse Collections Procedure - accept method (JDK compatible)
					        list.forEach(procedure);

					        // JDK Predicate - test method
					        MutableList<String> jdkSelectTest = list.select(jdkPredicate);

					        // JDK Function - apply method
					        MutableList<Integer> jdkCollectApply = list.collect(jdkFunction);

					        // JDK Consumer - accept method
					        list.forEach(consumer);

					        // Works in chained calls
					        MutableList<Integer> chained = list
					            .select(predicate)
					            .collect(function);
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
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(
					            MutableList<String> list,
					            Predicate<String> predicate,
					            Function<String, Integer> function,
					            Procedure<String> procedure) {
					        // Already simplified - should not change
					        MutableList<String> alreadySimple = list.select(predicate);
					        MutableList<Integer> alreadySimpleCollect = list.collect(function);
					        list.forEach(procedure);

					        // Lambda expressions - should not change
					        MutableList<String> lambda = list.select(s -> s.isEmpty());
					        MutableList<Integer> lambdaCollect = list.collect(s -> s.length());
					        list.forEach(System.out::println);

					        // Method references to other methods - should not change
					        MutableList<String> methodRef = list.select(String::isEmpty);
					        MutableList<Integer> methodRefCollect = list.collect(String::length);
					        list.forEach(System.out::println);

					        // Method references to non-functional-interface methods - should not change
					        MutableList<String> toString = list.collect(Object::toString);
					        MutableList<Integer> hashCode = list.collect(Object::hashCode);
					    }
					}
					"""
				)
			);
	}
}
