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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

/**
 * Tests for {@link ECStreamGatherFoldToInjectInto}.
 *
 * <p>Since {@code Gatherers} is a Java 24+ API and this project targets Java 17,
 * we provide stub classes for {@code Gatherer} and {@code Gatherers} and disable
 * type validation because {@code Stream.gather()} does not exist in the Java 17 JDK.
 */
class ECStreamGatherFoldToInjectIntoTest extends AbstractEclipseCollectionsTest {

	// Stub for java.util.stream.Gatherer (Java 24+)
	private static final String GATHERER_STUB = """
		package java.util.stream;

		public interface Gatherer<T, A, R> {
		}
		""";

	// Stub for java.util.stream.Gatherers (Java 24+)
	private static final String GATHERERS_STUB = """
		package java.util.stream;

		import java.util.function.BiFunction;
		import java.util.function.Supplier;

		public class Gatherers {
		    public static <T, R> Gatherer<T, ?, R> fold(Supplier<R> initial, BiFunction<? super R, ? super T, ? extends R> folder) {
		        return null;
		    }
		}
		""";

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamGatherFoldToInjectInto());
		spec.typeValidationOptions(TypeValidation.none());
		spec.parser(
			JavaParser.fromJavaVersion()
				.classpath("eclipse-collections-api", "eclipse-collections")
				.dependsOn(GATHERER_STUB, GATHERERS_STUB)
		);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.stream.Gatherers;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Integer foldWithOrElseThrow(MutableList<Integer> list) {
					        return list.stream()
					            .gather(Gatherers.fold(() -> 0, Integer::sum))
					            .findFirst()
					            .orElseThrow();
					    }

					    Integer foldWithGet(MutableList<Integer> list) {
					        return list.stream()
					            .gather(Gatherers.fold(() -> 0, Integer::sum))
					            .findFirst()
					            .get();
					    }

					    Integer foldWithLambda(MutableList<Integer> list) {
					        return list.stream()
					            .gather(Gatherers.fold(() -> 0, (a, b) -> a + b))
					            .findFirst()
					            .orElseThrow();
					    }

					    Integer foldWithImmutableList(ImmutableList<Integer> list) {
					        return list.stream()
					            .gather(Gatherers.fold(() -> 0, Integer::sum))
					            .findFirst()
					            .orElseThrow();
					    }

					    String foldWithStringConcat(MutableList<String> list) {
					        return list.stream()
					            .gather(Gatherers.fold(() -> "", String::concat))
					            .findFirst()
					            .orElseThrow();
					    }

					    Integer foldWithMutableSet(MutableSet<Integer> set) {
					        return set.stream()
					            .gather(Gatherers.fold(() -> 1, (a, b) -> a * b))
					            .findFirst()
					            .orElseThrow();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Integer foldWithOrElseThrow(MutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }

					    Integer foldWithGet(MutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }

					    Integer foldWithLambda(MutableList<Integer> list) {
					        return list.injectInto(0, (a, b) -> a + b);
					    }

					    Integer foldWithImmutableList(ImmutableList<Integer> list) {
					        return list.injectInto(0, Integer::sum);
					    }

					    String foldWithStringConcat(MutableList<String> list) {
					        return list.injectInto("", String::concat);
					    }

					    Integer foldWithMutableSet(MutableSet<Integer> set) {
					        return set.injectInto(1, (a, b) -> a * b);
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
					import java.util.function.Supplier;
					import java.util.stream.Gatherers;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    ArrayList<Integer> arrayList;
					    MutableList<Integer> mutableList;
					    Supplier<Integer> supplier = () -> 0;

					    Integer nonEclipseCollectionsType() {
					        return arrayList.stream()
					            .gather(Gatherers.fold(() -> 0, Integer::sum))
					            .findFirst()
					            .orElseThrow();
					    }

					    Stream<Integer> withoutFindFirst() {
					        return mutableList.stream()
					            .gather(Gatherers.fold(() -> 0, Integer::sum));
					    }

					    Integer nonSupplierLambda() {
					        return mutableList.stream()
					            .gather(Gatherers.fold(supplier, Integer::sum))
					            .findFirst()
					            .orElseThrow();
					    }
					}
					"""
				)
			);
	}
}
