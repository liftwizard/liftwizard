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

package io.liftwizard.rewrite.eclipse.collections;

import io.liftwizard.rewrite.AbstractRewriteFixtures;
import io.liftwizard.rewrite.AbstractRewriteStyles;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class HideUtilityClassConstructorTest implements AbstractRewriteFixtures, RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new HideUtilityClassConstructor())
			.parser(JavaParser.fromJavaVersion().styles(AbstractRewriteStyles.styles()));
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		rewriteRun(
			//language=java
			this.javaFixture("replacePatterns/01"),
			//language=java
			this.javaFixture("replacePatterns/02"),
			//language=java
			this.javaFixture("replacePatterns/03"),
			//language=java
			this.javaFixture("replacePatterns/04"),
			//language=java
			this.javaFixture("replacePatterns/05")
		);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		rewriteRun(
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/01"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/02"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/03"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/04"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/05"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/06"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/07"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/08"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/09"),
			//language=java
			this.javaFixtureUnchanged("doNotReplaceInvalidPatterns/10")
		);
	}
}
