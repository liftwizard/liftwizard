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

import io.liftwizard.rewrite.AbstractRewriteStyles;
import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

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
				.styles(AbstractRewriteStyles.styles())
				.dependsOn(GATHERER_STUB, GATHERERS_STUB)
		);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(this.javaFixture("replacePatterns/01"));
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"));
	}
}
