/*
 * Copyright 2026 Craig Motlin
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

package io.liftwizard.rewrite.eclipse.collections;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class EclipseCollectionsDeclarationSiteTypeVariancesTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipeFromResources(
			"io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsDeclarationSiteTypeVariances"
		);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.function.Function0;
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.api.block.function.primitive.IntFunction;
					import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.predicate.Predicate2;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    // Predicate<IN> -> Predicate<? super IN>
					    <T> MutableList<T> filter(MutableList<T> list, Predicate<T> predicate) {
					        return list.select(predicate);
					    }

					    // Predicate2<IN, IN> -> Predicate2<? super IN, ? super IN>
					    <T, P> MutableList<T> filterWith(MutableList<T> list, Predicate2<T, P> predicate, P param) {
					        return list.selectWith(predicate, param);
					    }

					    // Procedure<IN> -> Procedure<? super IN>
					    <T> void process(MutableList<T> list, Procedure<T> procedure) {
					        list.forEach(procedure);
					    }

					    // Procedure2<IN, IN> -> Procedure2<? super IN, ? super IN>
					    <T, P> void processWith(MutableList<T> list, Procedure2<T, P> procedure, P param) {
					        list.forEachWith(procedure, param);
					    }

					    // Function<IN, OUT> -> Function<? super IN, ? extends OUT>
					    <T, R> MutableList<R> transform(MutableList<T> list, Function<T, R> function) {
					        return list.collect(function);
					    }

					    // Function0<OUT> -> Function0<? extends OUT>
					    <T> T getOrDefault(T value, Function0<T> factory) {
					        return value != null ? value : factory.value();
					    }

					    // Function2<IN, IN, OUT> -> Function2<? super IN, ? super IN, ? extends OUT>
					    <T, R> R reduce(MutableList<T> list, R initial, Function2<R, T, R> function) {
					        return list.injectInto(initial, function);
					    }

					    // IntFunction<IN> -> IntFunction<? super IN>
					    <T> long sum(MutableList<T> list, IntFunction<T> function) {
					        return list.sumOfInt(function);
					    }

					    // IntToObjectFunction<OUT> -> IntToObjectFunction<? extends OUT>
					    <T> MutableList<T> collect(int[] array, IntToObjectFunction<T> function) {
					        return null;
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.function.Function0;
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.api.block.function.primitive.IntFunction;
					import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.predicate.Predicate2;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    // Predicate<IN> -> Predicate<? super IN>
					    <T> MutableList<T> filter(MutableList<T> list, Predicate<? super T> predicate) {
					        return list.select(predicate);
					    }

					    // Predicate2<IN, IN> -> Predicate2<? super IN, ? super IN>
					    <T, P> MutableList<T> filterWith(MutableList<T> list, Predicate2<? super T, ? super P> predicate, P param) {
					        return list.selectWith(predicate, param);
					    }

					    // Procedure<IN> -> Procedure<? super IN>
					    <T> void process(MutableList<T> list, Procedure<? super T> procedure) {
					        list.forEach(procedure);
					    }

					    // Procedure2<IN, IN> -> Procedure2<? super IN, ? super IN>
					    <T, P> void processWith(MutableList<T> list, Procedure2<? super T, ? super P> procedure, P param) {
					        list.forEachWith(procedure, param);
					    }

					    // Function<IN, OUT> -> Function<? super IN, ? extends OUT>
					    <T, R> MutableList<R> transform(MutableList<T> list, Function<? super T, ? extends R> function) {
					        return list.collect(function);
					    }

					    // Function0<OUT> -> Function0<? extends OUT>
					    <T> T getOrDefault(T value, Function0<? extends T> factory) {
					        return value != null ? value : factory.value();
					    }

					    // Function2<IN, IN, OUT> -> Function2<? super IN, ? super IN, ? extends OUT>
					    <T, R> R reduce(MutableList<T> list, R initial, Function2<? super R, ? super T, ? extends R> function) {
					        return list.injectInto(initial, function);
					    }

					    // IntFunction<IN> -> IntFunction<? super IN>
					    <T> long sum(MutableList<T> list, IntFunction<? super T> function) {
					        return list.sumOfInt(function);
					    }

					    // IntToObjectFunction<OUT> -> IntToObjectFunction<? extends OUT>
					    <T> MutableList<T> collect(int[] array, IntToObjectFunction<? extends T> function) {
					        return null;
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
					import org.eclipse.collections.api.block.function.Function0;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    // Already has correct variance - should not change
					    <T> MutableList<T> filter(MutableList<T> list, Predicate<? super T> predicate) {
					        return list.select(predicate);
					    }

					    <T> void process(MutableList<T> list, Procedure<? super T> procedure) {
					        list.forEach(procedure);
					    }

					    <T, R> MutableList<R> transform(MutableList<T> list, Function<? super T, ? extends R> function) {
					        return list.collect(function);
					    }

					    <T> T getOrDefault(T value, Function0<? extends T> factory) {
					        return value != null ? value : factory.value();
					    }
					}
					"""
				)
			);
	}
}
