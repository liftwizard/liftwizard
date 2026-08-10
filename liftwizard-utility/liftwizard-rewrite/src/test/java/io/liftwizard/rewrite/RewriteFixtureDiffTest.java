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

package io.liftwizard.rewrite;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RewriteFixtureDiffTest {

	private static final String BEFORE = """
		import org.eclipse.collections.api.list.MutableList;

		class Test {

			int test(MutableList<String> list) {
				return list.select(each -> true).size();
			}
		}
		""";

	private static final String AFTER = """
		import org.eclipse.collections.api.list.MutableList;

		class Test {

			int test(MutableList<String> list) {
				return list.count(each -> true);
			}
		}
		""";

	@Test
	void roundTripsBothSides() {
		String diff = AbstractRewriteFixtureDiff.format("replacePatterns/01", BEFORE, AFTER);

		assertThat(AbstractRewriteFixtureDiff.before(diff)).isEqualTo(BEFORE);
		assertThat(AbstractRewriteFixtureDiff.after(diff)).isEqualTo(AFTER);
	}

	@Test
	void carriesOnlyChangedLinesTwice() {
		String diff = AbstractRewriteFixtureDiff.format("replacePatterns/01", BEFORE, AFTER);

		assertThat(diff).contains("-\t\treturn list.select(each -> true).size();");
		assertThat(diff).contains("+\t\treturn list.count(each -> true);");
		assertThat(
			diff
				.lines()
				.filter((line) -> line.startsWith("-") && !line.startsWith("---"))
				.count()
		).isEqualTo(1);
		assertThat(
			diff
				.lines()
				.filter((line) -> line.startsWith("+") && !line.startsWith("+++"))
				.count()
		).isEqualTo(1);
	}

	@Test
	void writesEmptyContextLinesEmptySoTheTrailingWhitespaceHookCannotCorruptThem() {
		String diff = AbstractRewriteFixtureDiff.format("replacePatterns/01", BEFORE, AFTER);

		assertThat(diff.lines()).noneMatch(" "::equals);
	}

	@Test
	void survivesTrailingWhitespaceStripping() {
		String diff = AbstractRewriteFixtureDiff.format("replacePatterns/01", BEFORE, AFTER);
		String stripped = diff
			.lines()
			.map((line) -> line.stripTrailing())
			.reduce("", (a, b) -> a + b + "\n");

		assertThat(AbstractRewriteFixtureDiff.before(stripped)).isEqualTo(BEFORE);
		assertThat(AbstractRewriteFixtureDiff.after(stripped)).isEqualTo(AFTER);
	}

	@Test
	void roundTripsTabsAndBlankLinesUnchanged() {
		String identical = "class Test {\n\n\tvoid test() {}\n}\n";

		String diff = AbstractRewriteFixtureDiff.format("unchanged/01", identical, identical);

		assertThat(AbstractRewriteFixtureDiff.before(diff)).isEqualTo(identical);
		assertThat(AbstractRewriteFixtureDiff.after(diff)).isEqualTo(identical);
	}

	@Test
	void roundTripsWhenEveryLineDiffers() {
		String before = "a\nb\n";
		String after = "c\nd\n";

		String diff = AbstractRewriteFixtureDiff.format("replacePatterns/01", before, after);

		assertThat(AbstractRewriteFixtureDiff.before(diff)).isEqualTo(before);
		assertThat(AbstractRewriteFixtureDiff.after(diff)).isEqualTo(after);
	}
}
