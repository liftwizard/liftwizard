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

class ECSelectSizeToCountTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSelectSizeToCountRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				// Pattern 1: RichIterable select().size() -> count()
				this.javaFixture("replacePatterns/01"),
				// Pattern 1: ImmutableList
				this.javaFixture("replacePatterns/02"),
				// Pattern 1: MutableSet
				this.javaFixture("replacePatterns/03"),
				// Pattern 1: With lambda predicate
				this.javaFixture("replacePatterns/04"),
				// Pattern 2: ArrayIterate.select().size() -> ArrayIterate.count()
				this.javaFixture("replacePatterns/05"),
				// Pattern 2: ListIterate.select().size() -> ListIterate.count()
				this.javaFixture("replacePatterns/06")
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				// Do not replace when size is not called on select result
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"),
				// Do not replace when select has intermediate operations
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/02"),
				// Do not replace for non-EC types
				this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/03")
			);
	}
}
