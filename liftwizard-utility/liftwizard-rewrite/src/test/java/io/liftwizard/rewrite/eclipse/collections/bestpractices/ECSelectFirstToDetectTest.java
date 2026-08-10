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

class ECSelectFirstToDetectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSelectFirstToDetectRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				// Pattern 1: OrderedIterable select().getFirstOptional() -> detectOptional()
				this.javaFixture("replacePatterns/01"),
				// Pattern 1: RichIterable select().getFirst() -> detect()
				this.javaFixture("replacePatterns/02"),
				// Pattern 1: ImmutableList
				this.javaFixture("replacePatterns/03"),
				// Pattern 1: MutableSet with getFirst() (not getFirstOptional())
				this.javaFixture("replacePatterns/04"),
				// Pattern 2: ArrayIterate.select().getFirstOptional() -> ArrayIterate.detectOptional()
				this.javaFixture("replacePatterns/05"),
				// Pattern 2: ArrayIterate.select().getFirst() -> ArrayIterate.detect()
				this.javaFixture("replacePatterns/06"),
				// Pattern 2: ListIterate.select().getFirstOptional() -> ListIterate.detectOptional()
				this.javaFixture("replacePatterns/07")
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				// Do not replace when getFirstOptional is not called on select result
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"),
				// Do not replace when getFirst is not called on select result
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/02"),
				// Do not replace when select has intermediate operations
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/03"),
				// Do not replace for non-EC types
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/04")
			);
	}
}
