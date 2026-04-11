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
		spec.recipe(new PreLambdaTypesToLambdasRecipes()).typeValidationOptions(TypeValidation.none());
	}

	@DocumentExample
	@Test
	void addFunction() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.impl.block.function.AddFunction;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> addInt = AddFunction.INTEGER;
					        Function2<Long, Long, Long> addLong = AddFunction.LONG;
					        Function2<Double, Double, Double> addDouble = AddFunction.DOUBLE;
					        Function2<Float, Float, Float> addFloat = AddFunction.FLOAT;
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function2;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> addInt = Integer::sum;
					        Function2<Long, Long, Long> addLong = Long::sum;
					        Function2<Double, Double, Double> addDouble = Double::sum;
					        Function2<Float, Float, Float> addFloat = Float::sum;
					    }
					}
					"""
				)
			);
	}

	@Test
	void multiplyFunction() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.impl.block.function.MultiplyFunction;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> mulInt = MultiplyFunction.INTEGER;
					        Function2<Long, Long, Long> mulLong = MultiplyFunction.LONG;
					        Function2<Double, Double, Double> mulDouble = MultiplyFunction.DOUBLE;
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function2;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> mulInt = (a, b) -> a * b;
					        Function2<Long, Long, Long> mulLong = (a, b) -> a * b;
					        Function2<Double, Double, Double> mulDouble = (a, b) -> a * b;
					    }
					}
					"""
				)
			);
	}

	@Test
	void subtractFunction() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function2;
					import org.eclipse.collections.impl.block.function.SubtractFunction;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> subInt = SubtractFunction.INTEGER;
					        Function2<Long, Long, Long> subLong = SubtractFunction.LONG;
					        Function2<Double, Double, Double> subDouble = SubtractFunction.DOUBLE;
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function2;

					class Test {
					    void test() {
					        Function2<Integer, Integer, Integer> subInt = (a, b) -> a - b;
					        Function2<Long, Long, Long> subLong = (a, b) -> a - b;
					        Function2<Double, Double, Double> subDouble = (a, b) -> a - b;
					    }
					}
					"""
				)
			);
	}

	@Test
	void functionsGetStringToInteger() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.impl.block.factory.Functions;

					class Test {
					    void test() {
					        Function<String, Integer> fn = Functions.getStringToInteger();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;

					class Test {
					    void test() {
					        Function<String, Integer> fn = Integer::valueOf;
					    }
					}
					"""
				)
			);
	}

	@Test
	void functionsGetStringTrim() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.impl.block.factory.Functions;

					class Test {
					    void test() {
					        Function<String, String> fn = Functions.getStringTrim();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;

					class Test {
					    void test() {
					        Function<String, String> fn = String::trim;
					    }
					}
					"""
				)
			);
	}

	@Test
	void functionsGetToClass() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.impl.block.factory.Functions;

					class Test {
					    void test() {
					        Function<Object, Class<?>> fn = Functions.getToClass();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;

					class Test {
					    void test() {
					        Function<Object, Class<?>> fn = Object::getClass;
					    }
					}
					"""
				)
			);
	}

	@Test
	void functionsGetToString() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.impl.block.factory.Functions;

					class Test {
					    void test() {
					        Function<Object, String> fn = Functions.getToString();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function;

					class Test {
					    void test() {
					        Function<Object, String> fn = Object::toString;
					    }
					}
					"""
				)
			);
	}

	@Test
	void mapPutProcedure() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import org.eclipse.collections.impl.block.procedure.MapPutProcedure;
					import java.util.Map;
					import java.util.HashMap;

					class Test {
					    void test() {
					        Map<String, Integer> map = new HashMap<>();
					        Procedure2<String, Integer> procedure = new MapPutProcedure<>(map);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.procedure.Procedure2;
					import java.util.Map;
					import java.util.HashMap;

					class Test {
					    void test() {
					        Map<String, Integer> map = new HashMap<>();
					        Procedure2<String, Integer> procedure = map::put;
					    }
					}
					"""
				)
			);
	}

	@Test
	void passThruFunction0() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function0;
					import org.eclipse.collections.impl.block.function.PassThruFunction0;

					class Test {
					    void test() {
					        Function0<String> fn1 = new PassThruFunction0<>("hello");

					        String value = "world";
					        Function0<String> fn2 = new PassThruFunction0<>(value);

					        Function0<Integer> fn3 = new PassThruFunction0<>(42);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.block.function.Function0;

					class Test {
					    void test() {
					        Function0<String> fn1 = () -> "hello";

					        String value = "world";
					        Function0<String> fn2 = () -> value;

					        Function0<Integer> fn3 = () -> 42;
					    }
					}
					"""
				)
			);
	}
}
