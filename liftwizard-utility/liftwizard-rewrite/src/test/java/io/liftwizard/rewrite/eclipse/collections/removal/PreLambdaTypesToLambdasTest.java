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

package io.liftwizard.rewrite.eclipse.collections.removal;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class PreLambdaTypesToLambdasTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new PreLambdaTypesToLambdas()).typeValidationOptions(TypeValidation.none());
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
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import org.eclipse.collections.impl.block.factory.Functions;
					import org.eclipse.collections.impl.block.function.AddFunction;
					import org.eclipse.collections.impl.block.function.MultiplyFunction;
					import org.eclipse.collections.impl.block.function.PassThruFunction0;
					import org.eclipse.collections.impl.block.function.SubtractFunction;
					import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
					import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
					import org.eclipse.collections.impl.block.procedure.MapPutProcedure;
					import java.util.ArrayList;
					import java.util.HashMap;
					import java.util.List;
					import java.util.Map;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> addInt = AddFunction.INTEGER;
					        Function2<Long, Long, Long> addLong = AddFunction.LONG;
					        Function2<Double, Double, Double> addDouble = AddFunction.DOUBLE;
					        Function2<Float, Float, Float> addFloat = AddFunction.FLOAT;

					        Function2<Integer, Integer, Integer> mulInt = MultiplyFunction.INTEGER;
					        Function2<Long, Long, Long> mulLong = MultiplyFunction.LONG;
					        Function2<Double, Double, Double> mulDouble = MultiplyFunction.DOUBLE;

					        Function2<Integer, Integer, Integer> subInt = SubtractFunction.INTEGER;
					        Function2<Long, Long, Long> subLong = SubtractFunction.LONG;
					        Function2<Double, Double, Double> subDouble = SubtractFunction.DOUBLE;

					        Function<String, Integer> stringToInteger = Functions.getStringToInteger();
					        Function<String, String> stringTrim = Functions.getStringTrim();
					        Function<Object, Class<?>> toClass = Functions.getToClass();
					        Function<Object, String> toString = Functions.getToString();

					        Map<String, Integer> map = new HashMap<>();
					        Procedure2<String, Integer> mapPut = new MapPutProcedure<>(map);

					        Function0<String> literalSupplier = new PassThruFunction0<>("hello");
					        String value = "world";
					        Function0<String> variableSupplier = new PassThruFunction0<>(value);
					        Function0<Integer> intSupplier = new PassThruFunction0<>(42);

					        List<String> addList = new ArrayList<>();
					        addList.forEach(CollectionAddProcedure.on(addList));

					        List<String> addCtor1 = new ArrayList<>();
					        Procedure<String> addProcedure1 = new CollectionAddProcedure<String>(addCtor1);

					        List<String> addCtor2 = new ArrayList<>();
					        Procedure<String> addProcedure2 = new CollectionAddProcedure<>(addCtor2);

					        List<String> addCtor3 = new ArrayList<>();
					        addCtor3.forEach(new CollectionAddProcedure<>(addCtor3));

					        List<String> removeList = new ArrayList<>();
					        Procedure<String> removeProcedure1 = CollectionRemoveProcedure.on(removeList);

					        List<String> removeCtor1 = new ArrayList<>();
					        Procedure<String> removeProcedure2 = new CollectionRemoveProcedure<String>(removeCtor1);

					        List<String> removeCtor2 = new ArrayList<>();
					        Procedure<String> removeProcedure3 = new CollectionRemoveProcedure<>(removeCtor2);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.function.Function0;
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import java.util.ArrayList;
					import java.util.HashMap;
					import java.util.List;
					import java.util.Map;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> addInt = Integer::sum;
					        Function2<Long, Long, Long> addLong = Long::sum;
					        Function2<Double, Double, Double> addDouble = Double::sum;
					        Function2<Float, Float, Float> addFloat = Float::sum;

					        Function2<Integer, Integer, Integer> mulInt = (Integer a, Integer b) -> a * b;
					        Function2<Long, Long, Long> mulLong = (Long a, Long b) -> a * b;
					        Function2<Double, Double, Double> mulDouble = (Double a, Double b) -> a * b;

					        Function2<Integer, Integer, Integer> subInt = (Integer a, Integer b) -> a - b;
					        Function2<Long, Long, Long> subLong = (Long a, Long b) -> a - b;
					        Function2<Double, Double, Double> subDouble = (Double a, Double b) -> a - b;

					        Function<String, Integer> stringToInteger = Integer::valueOf;
					        Function<String, String> stringTrim = String::trim;
					        Function<Object, Class<?>> toClass = Object::getClass;
					        Function<Object, String> toString = Object::toString;

					        Map<String, Integer> map = new HashMap<>();
					        Procedure2<String, Integer> mapPut = map::put;

					        Function0<String> literalSupplier = () -> "hello";
					        String value = "world";
					        Function0<String> variableSupplier = () -> value;
					        Function0<Integer> intSupplier = () -> 42;

					        List<String> addList = new ArrayList<>();
					        addList.forEach(addList::add);

					        List<String> addCtor1 = new ArrayList<>();
					        Procedure<String> addProcedure1 = addCtor1::add;

					        List<String> addCtor2 = new ArrayList<>();
					        Procedure<String> addProcedure2 = addCtor2::add;

					        List<String> addCtor3 = new ArrayList<>();
					        addCtor3.forEach(addCtor3::add);

					        List<String> removeList = new ArrayList<>();
					        Procedure<String> removeProcedure1 = removeList::remove;

					        List<String> removeCtor1 = new ArrayList<>();
					        Procedure<String> removeProcedure2 = removeCtor1::remove;

					        List<String> removeCtor2 = new ArrayList<>();
					        Procedure<String> removeProcedure3 = removeCtor2::remove;
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
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.api.block.function.primitive.IntObjectToIntFunction;
					import org.eclipse.collections.api.block.procedure.Procedure;
					import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
					import org.eclipse.collections.impl.block.function.AddFunction;
					import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
					import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
					import java.util.ArrayList;
					import java.util.List;

					class Test {
					    static void accept(Object value) {}
					    static void forEach(Procedure<? super Integer> procedure) {}
					    static void forEach(IntProcedure procedure) {}

					    static <T> Integer injectInto(int identity, Iterable<T> iterable, IntObjectToIntFunction<? super T> fn) { return 0; }
					    static <T> Integer injectInto(int identity, Iterable<T> iterable, Function2<? super Integer, ? super T, ? extends Integer> fn) { return 0; }

					    void test() {
					        List<String> concreteAdd = new ArrayList<>();
					        CollectionAddProcedure<String> addConcrete = new CollectionAddProcedure<>(concreteAdd);

					        List<String> concreteRemove = new ArrayList<>();
					        CollectionRemoveProcedure<String> removeConcrete = new CollectionRemoveProcedure<>(concreteRemove);

					        List<String> objectArg = new ArrayList<>();
					        accept(CollectionAddProcedure.on(objectArg));
					        accept(new CollectionAddProcedure<>(objectArg));
					        accept(CollectionRemoveProcedure.on(objectArg));
					        accept(new CollectionRemoveProcedure<>(objectArg));
					        accept(1L, "double", AddFunction.DOUBLE);

					        Procedure<String> addNullReceiver = CollectionAddProcedure.on(null);
					        Procedure<String> removeNullReceiver = CollectionRemoveProcedure.on(null);

					        List<Integer> ambiguous = new ArrayList<>();
					        forEach(CollectionAddProcedure.on(ambiguous));

					        Iterable<Integer> iterable = List.of(1, 2, 3);
					        Integer result = injectInto(1, iterable, AddFunction.INTEGER);
					    }

					    static void accept(long a, String name, Object value) {}
					}
					"""
				)
			);
	}
}
