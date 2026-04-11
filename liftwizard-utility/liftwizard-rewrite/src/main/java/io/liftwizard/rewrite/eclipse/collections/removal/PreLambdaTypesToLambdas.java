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

import java.util.Map;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.function.AddFunction;
import org.eclipse.collections.impl.block.function.MultiplyFunction;
import org.eclipse.collections.impl.block.function.PassThruFunction0;
import org.eclipse.collections.impl.block.function.SubtractFunction;
import org.eclipse.collections.impl.block.procedure.MapPutProcedure;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "Replace pre-lambda Eclipse Collections types with lambdas and method references",
	description = "Replace Eclipse Collections' pre-lambda utility classes and constants with equivalent Java lambdas and method references."
)
public class PreLambdaTypesToLambdas {

	@RecipeDescriptor(
		name = "`AddFunction.INTEGER` to `Integer::sum`",
		description = "Replace `AddFunction.INTEGER` with `Integer::sum`."
	)
	public static class AddFunctionIntegerToLambdaRecipe {

		@BeforeTemplate
		Function2<Integer, Integer, Integer> before() {
			return AddFunction.INTEGER;
		}

		@AfterTemplate
		Function2<Integer, Integer, Integer> after() {
			return Integer::sum;
		}
	}

	@RecipeDescriptor(
		name = "`AddFunction.LONG` to `Long::sum`",
		description = "Replace `AddFunction.LONG` with `Long::sum`."
	)
	public static class AddFunctionLongToLambdaRecipe {

		@BeforeTemplate
		Function2<Long, Long, Long> before() {
			return AddFunction.LONG;
		}

		@AfterTemplate
		Function2<Long, Long, Long> after() {
			return Long::sum;
		}
	}

	@RecipeDescriptor(
		name = "`AddFunction.DOUBLE` to `Double::sum`",
		description = "Replace `AddFunction.DOUBLE` with `Double::sum`."
	)
	public static class AddFunctionDoubleToLambdaRecipe {

		@BeforeTemplate
		Function2<Double, Double, Double> before() {
			return AddFunction.DOUBLE;
		}

		@AfterTemplate
		Function2<Double, Double, Double> after() {
			return Double::sum;
		}
	}

	@RecipeDescriptor(
		name = "`AddFunction.FLOAT` to `Float::sum`",
		description = "Replace `AddFunction.FLOAT` with `Float::sum`."
	)
	public static class AddFunctionFloatToLambdaRecipe {

		@BeforeTemplate
		Function2<Float, Float, Float> before() {
			return AddFunction.FLOAT;
		}

		@AfterTemplate
		Function2<Float, Float, Float> after() {
			return Float::sum;
		}
	}

	@RecipeDescriptor(
		name = "`Functions.getStringToInteger()` to `Integer::valueOf`",
		description = "Replace `Functions.getStringToInteger()` with `Integer::valueOf`."
	)
	public static class FunctionsGetStringToIntegerToMethodReferenceRecipe {

		@BeforeTemplate
		Function<String, Integer> before() {
			return Functions.getStringToInteger();
		}

		@AfterTemplate
		Function<String, Integer> after() {
			return Integer::valueOf;
		}
	}

	@RecipeDescriptor(
		name = "`Functions.getStringTrim()` to `String::trim`",
		description = "Replace `Functions.getStringTrim()` with `String::trim`."
	)
	public static class FunctionsGetStringTrimToMethodReferenceRecipe {

		@BeforeTemplate
		Function<String, String> before() {
			return Functions.getStringTrim();
		}

		@AfterTemplate
		Function<String, String> after() {
			return String::trim;
		}
	}

	@RecipeDescriptor(
		name = "`Functions.getToClass()` to `Object::getClass`",
		description = "Replace `Functions.getToClass()` with `Object::getClass`."
	)
	public static class FunctionsGetToClassToMethodReferenceRecipe {

		@BeforeTemplate
		Function<Object, Class<?>> before() {
			return Functions.getToClass();
		}

		@AfterTemplate
		Function<Object, Class<?>> after() {
			return Object::getClass;
		}
	}

	@RecipeDescriptor(
		name = "`Functions.getToString()` to `Object::toString`",
		description = "Replace `Functions.getToString()` with `Object::toString`."
	)
	public static class FunctionsGetToStringToMethodReferenceRecipe {

		@BeforeTemplate
		Function<Object, String> before() {
			return Functions.getToString();
		}

		@AfterTemplate
		Function<Object, String> after() {
			return Object::toString;
		}
	}

	@RecipeDescriptor(
		name = "`new MapPutProcedure<>(map)` to `map::put`",
		description = "Replace `new MapPutProcedure<>(map)` with `map::put`."
	)
	public static class MapPutProcedureToMethodReferenceRecipe<K, V> {

		@BeforeTemplate
		Procedure2<K, V> before(Map<K, V> map) {
			return new MapPutProcedure<>(map);
		}

		@AfterTemplate
		Procedure2<K, V> after(Map<K, V> map) {
			return map::put;
		}
	}

	@RecipeDescriptor(
		name = "`MultiplyFunction.INTEGER` to `(a, b) -> a * b`",
		description = "Replace `MultiplyFunction.INTEGER` with `(a, b) -> a * b`."
	)
	public static class MultiplyFunctionIntegerToLambdaRecipe {

		@BeforeTemplate
		Function2<Integer, Integer, Integer> before() {
			return MultiplyFunction.INTEGER;
		}

		@AfterTemplate
		Function2<Integer, Integer, Integer> after() {
			return (a, b) -> a * b;
		}
	}

	@RecipeDescriptor(
		name = "`MultiplyFunction.LONG` to `(a, b) -> a * b`",
		description = "Replace `MultiplyFunction.LONG` with `(a, b) -> a * b`."
	)
	public static class MultiplyFunctionLongToLambdaRecipe {

		@BeforeTemplate
		Function2<Long, Long, Long> before() {
			return MultiplyFunction.LONG;
		}

		@AfterTemplate
		Function2<Long, Long, Long> after() {
			return (a, b) -> a * b;
		}
	}

	@RecipeDescriptor(
		name = "`MultiplyFunction.DOUBLE` to `(a, b) -> a * b`",
		description = "Replace `MultiplyFunction.DOUBLE` with `(a, b) -> a * b`."
	)
	public static class MultiplyFunctionDoubleToLambdaRecipe {

		@BeforeTemplate
		Function2<Double, Double, Double> before() {
			return MultiplyFunction.DOUBLE;
		}

		@AfterTemplate
		Function2<Double, Double, Double> after() {
			return (a, b) -> a * b;
		}
	}

	@RecipeDescriptor(
		name = "`new PassThruFunction0<>(value)` to `() -> value`",
		description = "Replace `new PassThruFunction0<>(value)` with `() -> value`."
	)
	public static class PassThruFunction0ToLambdaRecipe<T> {

		@BeforeTemplate
		Function0<T> before(T value) {
			return new PassThruFunction0<>(value);
		}

		@AfterTemplate
		Function0<T> after(T value) {
			return () -> value;
		}
	}

	@RecipeDescriptor(
		name = "`SubtractFunction.INTEGER` to `(a, b) -> a - b`",
		description = "Replace `SubtractFunction.INTEGER` with `(a, b) -> a - b`."
	)
	public static class SubtractFunctionIntegerToLambdaRecipe {

		@BeforeTemplate
		Function2<Integer, Integer, Integer> before() {
			return SubtractFunction.INTEGER;
		}

		@AfterTemplate
		Function2<Integer, Integer, Integer> after() {
			return (a, b) -> a - b;
		}
	}

	@RecipeDescriptor(
		name = "`SubtractFunction.LONG` to `(a, b) -> a - b`",
		description = "Replace `SubtractFunction.LONG` with `(a, b) -> a - b`."
	)
	public static class SubtractFunctionLongToLambdaRecipe {

		@BeforeTemplate
		Function2<Long, Long, Long> before() {
			return SubtractFunction.LONG;
		}

		@AfterTemplate
		Function2<Long, Long, Long> after() {
			return (a, b) -> a - b;
		}
	}

	@RecipeDescriptor(
		name = "`SubtractFunction.DOUBLE` to `(a, b) -> a - b`",
		description = "Replace `SubtractFunction.DOUBLE` with `(a, b) -> a - b`."
	)
	public static class SubtractFunctionDoubleToLambdaRecipe {

		@BeforeTemplate
		Function2<Double, Double, Double> before() {
			return SubtractFunction.DOUBLE;
		}

		@AfterTemplate
		Function2<Double, Double, Double> after() {
			return (a, b) -> a - b;
		}
	}
}
