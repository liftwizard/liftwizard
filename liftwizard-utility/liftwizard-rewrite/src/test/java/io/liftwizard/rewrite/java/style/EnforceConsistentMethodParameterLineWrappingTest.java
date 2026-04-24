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

package io.liftwizard.rewrite.java.style;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class EnforceConsistentMethodParameterLineWrappingTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new EnforceConsistentMethodParameterLineWrapping());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					abstract class Test {
					    void partiallyWrapped(String a,
					            String b,
					            String c) {}

					    void twoParamsPartiallyWrapped(String a,
					            String b) {}

					    void firstParamWrappedOthersNot(
					            String a, String b, String c) {}

					    abstract String abstractMethod(String a,
					            String b);

					    void fiveParams(
					            String a, String b,
					            String c, String d,
					            String e) {}

					    void sevenParams(
					            String a, String b,
					            String c, String d,
					            String e, String f, String g) {}
					}""",
					"""
					abstract class Test {
					    void partiallyWrapped(
					            String a,
					            String b,
					            String c) {}

					    void twoParamsPartiallyWrapped(
					            String a,
					            String b) {}

					    void firstParamWrappedOthersNot(
					            String a,
					            String b,
					            String c) {}

					    abstract String abstractMethod(
					            String a,
					            String b);

					    void fiveParams(
					            String a,
					            String b,
					            String c,
					            String d,
					            String e) {}

					    void sevenParams(
					            String a,
					            String b,
					            String c,
					            String d,
					            String e,
					            String f,
					            String g) {}
					}"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					class Test {
					    void singleLineCall(String a, String b, String c) {}

					    void alreadyFullyWrapped(
					            String a,
					            String b,
					            String c) {}

					    void singleParam(String a) {}

					    void singleParamWrapped(
					            String a) {}

					    void noParams() {}

					    void sixParamsTwoPerLine(
					            String a, String b,
					            String c, String d,
					            String e, String f) {}

					    void nineParamsThreePerLine(
					            String a, String b, String c,
					            String d, String e, String f,
					            String g, String h, String i) {}

					    void eightParamsFourPerLine(
					            String a, String b, String c, String d,
					            String e, String f, String g, String h) {}
					}"""
				)
			);
	}
}
