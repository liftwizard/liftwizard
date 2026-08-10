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

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;

/**
 * Reads and writes the single-file fixture format that stores a recipe's input and expected output as one unified
 * diff, so that a reviewer sees the transformation in one place and the two sides cannot drift apart.
 *
 * <p>Every line of both sides is carried, so the diff reconstructs each side exactly rather than approximately. An
 * empty context line is written empty rather than as a single space, because the repository's trailing-whitespace
 * pre-commit hook would otherwise strip that space and corrupt the fixture. Both spellings parse.
 */
public interface AbstractRewriteFixtureDiff {
	static String before(String diff) {
		return side(diff, '+');
	}

	static String after(String diff) {
		return side(diff, '-');
	}

	static String format(String name, String before, String after) {
		ListIterable<String> beforeLines = lines(before);
		ListIterable<String> afterLines = lines(after);

		var result = new StringBuilder();
		result.append("--- a/").append(name).append("-before.java\n");
		result.append("+++ b/").append(name).append("-after.java\n");
		result.append("@@ -1,").append(beforeLines.size()).append(" +1,").append(afterLines.size()).append(" @@\n");

		for (String line : editScript(beforeLines, afterLines)) {
			result.append(line).append('\n');
		}
		return result.toString();
	}

	private static String side(String diff, char exclude) {
		var result = new StringBuilder();
		for (String line : lines(diff)) {
			if (isHeader(line)) {
				continue;
			}
			if (line.isEmpty()) {
				result.append('\n');
				continue;
			}
			char marker = line.charAt(0);
			if (marker == exclude || marker == '\\') {
				continue;
			}
			result.append(line, 1, line.length()).append('\n');
		}
		return result.toString();
	}

	private static boolean isHeader(String line) {
		return line.startsWith("--- ") || line.startsWith("+++ ") || line.startsWith("@@");
	}

	private static ListIterable<String> lines(String contents) {
		String withoutTrailingNewline = contents.endsWith("\n")
			? contents.substring(0, contents.length() - 1)
			: contents;
		return Lists.immutable.with(withoutTrailingNewline.split("\n", -1));
	}

	/**
	 * A longest-common-subsequence edit script covering every line of both sides. The fixtures are small enough that
	 * the quadratic table costs nothing.
	 */
	private static ListIterable<String> editScript(ListIterable<String> before, ListIterable<String> after) {
		int rows = before.size();
		int columns = after.size();
		int[][] common = new int[rows + 1][columns + 1];
		for (int row = rows - 1; row >= 0; row--) {
			for (int column = columns - 1; column >= 0; column--) {
				common[row][column] = before.get(row).equals(after.get(column))
					? common[row + 1][column + 1] + 1
					: Math.max(common[row + 1][column], common[row][column + 1]);
			}
		}

		MutableList<String> result = Lists.mutable.empty();
		int row = 0;
		int column = 0;
		while (row < rows && column < columns) {
			if (before.get(row).equals(after.get(column))) {
				result.add(context(before.get(row)));
				row++;
				column++;
			} else if (common[row + 1][column] >= common[row][column + 1]) {
				result.add("-" + before.get(row));
				row++;
			} else {
				result.add("+" + after.get(column));
				column++;
			}
		}
		while (row < rows) {
			result.add("-" + before.get(row));
			row++;
		}
		while (column < columns) {
			result.add("+" + after.get(column));
			column++;
		}
		return result;
	}

	private static String context(String line) {
		return line.isEmpty() ? "" : " " + line;
	}
}
