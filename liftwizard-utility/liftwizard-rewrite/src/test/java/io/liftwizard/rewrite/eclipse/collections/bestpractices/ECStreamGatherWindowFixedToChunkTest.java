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
 * Tests for {@link ECStreamGatherWindowFixedToChunk}.
 *
 * <p>Since {@code Gatherers} is a Java 24+ API and this project targets Java 17,
 * we provide stub classes for {@code Gatherer} and {@code Gatherers} and disable
 * type validation because {@code Stream.gather()} does not exist in the Java 17 JDK.
 */
class ECStreamGatherWindowFixedToChunkTest extends AbstractEclipseCollectionsTest {

	// Stub for java.util.stream.Gatherer (Java 24+)
	private static final String GATHERER_STUB = """
		package java.util.stream;

		public interface Gatherer<T, A, R> {
		}
		""";

	// Stub for java.util.stream.Gatherers (Java 24+)
	private static final String GATHERERS_STUB = """
		package java.util.stream;

		import java.util.List;

		public class Gatherers {
		    public static <T> Gatherer<T, ?, List<T>> windowFixed(int windowSize) {
		        return null;
		    }
		}
		""";

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamGatherWindowFixedToChunk());
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
					import java.util.List;
					import java.util.stream.Collectors;
					import java.util.stream.Gatherers;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;

					    List<List<String>> windowFixedWithMutableList() {
					        return mutableList.stream()
					            .gather(Gatherers.windowFixed(3))
					            .collect(Collectors.toList());
					    }

					    List<List<String>> windowFixedWithImmutableList() {
					        return immutableList.stream()
					            .gather(Gatherers.windowFixed(5))
					            .collect(Collectors.toList());
					    }

					    List<List<Integer>> windowFixedWithMutableSet() {
					        return mutableSet.stream()
					            .gather(Gatherers.windowFixed(2))
					            .collect(Collectors.toList());
					    }

					    List<List<String>> windowFixedWithVariableSize(int batchSize) {
					        return mutableList.stream()
					            .gather(Gatherers.windowFixed(batchSize))
					            .collect(Collectors.toList());
					    }
					}
					""",
					"""
					import java.util.List;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;

					    List<List<String>> windowFixedWithMutableList() {
					        return mutableList.chunk(3);
					    }

					    List<List<String>> windowFixedWithImmutableList() {
					        return immutableList.chunk(5);
					    }

					    List<List<Integer>> windowFixedWithMutableSet() {
					        return mutableSet.chunk(2);
					    }

					    List<List<String>> windowFixedWithVariableSize(int batchSize) {
					        return mutableList.chunk(batchSize);
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
					import java.util.stream.Collectors;
					import java.util.stream.Gatherers;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    ArrayList<String> arrayList;
					    MutableList<String> mutableList;

					    List<List<String>> nonEclipseCollectionsType() {
					        return arrayList.stream()
					            .gather(Gatherers.windowFixed(3))
					            .collect(Collectors.toList());
					    }

					    Stream<List<String>> withoutCollect() {
					        return mutableList.stream()
					            .gather(Gatherers.windowFixed(3));
					    }
					}
					"""
				)
			);
	}
}
