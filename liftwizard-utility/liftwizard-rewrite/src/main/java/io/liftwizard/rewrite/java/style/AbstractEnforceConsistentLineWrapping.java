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

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.Recipe;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;

/**
 * Abstract superclass for recipes that enforce consistent line wrapping
 * in comma-separated lists (arguments, parameters, array initializers, etc.).
 *
 * <p>When a list has multiple elements and ANY element is on a different
 * line, this ensures ALL elements are line-wrapped for consistency,
 * unless the elements form a valid fixed-multiple grouping (e.g., key-value pairs).
 */
public abstract class AbstractEnforceConsistentLineWrapping extends Recipe {

	/**
	 * Returns true if wrapping should be enforced: the list is partially wrapped
	 * and does not form a valid fixed-multiple grouping.
	 */
	protected static boolean shouldEnforceWrapping(List<? extends JRightPadded<? extends J>> elements) {
		if (elements.size() < 2) {
			return false;
		}

		boolean anyWrapped = false;
		boolean allWrapped = true;

		for (JRightPadded<? extends J> element : elements) {
			if (containsNewline(element.getElement().getPrefix())) {
				anyWrapped = true;
			} else {
				allWrapped = false;
			}
		}

		if (!anyWrapped || allWrapped) {
			return false;
		}

		return !hasConsistentFixedMultipleGrouping(elements);
	}

	/**
	 * Finds the indentation string from the first wrapped element.
	 *
	 * @return the indent string, or null if no wrapped element is found
	 */
	protected static String findWrappedIndent(List<? extends JRightPadded<? extends J>> elements) {
		for (JRightPadded<? extends J> element : elements) {
			Space prefix = element.getElement().getPrefix();
			if (containsNewline(prefix)) {
				return extractIndentAfterLastNewline(prefix.getWhitespace());
			}
		}
		return null;
	}

	/**
	 * Applies line wrapping to all elements using the given indent string.
	 * Elements that already have newlines in their prefix are left unchanged.
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends J> List<JRightPadded<T>> applyWrapping(List<JRightPadded<T>> elements, String indent) {
		String newlineAndIndent = "\n" + indent;
		List<JRightPadded<T>> result = new ArrayList<>(elements.size());

		for (JRightPadded<T> element : elements) {
			T tree = element.getElement();
			Space prefix = tree.getPrefix();

			if (!containsNewline(prefix)) {
				Space newPrefix = prefix.withWhitespace(newlineAndIndent);
				result.add(element.withElement((T) tree.withPrefix(newPrefix)));
			} else {
				result.add(element);
			}
		}

		return result;
	}

	/**
	 * Detects valid fixed-multiple grouping patterns like key-value pairs.
	 *
	 * <p>A valid fixed-multiple grouping requires:
	 * <ul>
	 *   <li>The first element must have a newline prefix</li>
	 *   <li>Elements are grouped by newline boundaries (a new group starts at each newline-prefixed element)</li>
	 *   <li>There must be at least 2 groups</li>
	 *   <li>All groups must have the same size, which must be at least 2</li>
	 * </ul>
	 *
	 * <p>Example valid pattern (3 groups of 2):
	 * <pre>
	 * Maps.mutable.with(
	 *         "key1", "value1",
	 *         "key2", "value2",
	 *         "key3", "value3");
	 * </pre>
	 */
	protected static boolean hasConsistentFixedMultipleGrouping(List<? extends JRightPadded<? extends J>> elements) {
		if (elements.isEmpty()) {
			return false;
		}

		// First element must have a newline prefix
		if (!containsNewline(elements.get(0).getElement().getPrefix())) {
			return false;
		}

		// Group elements by newline boundaries
		List<Integer> groupSizes = new ArrayList<>();
		int currentGroupSize = 1;

		for (int i = 1; i < elements.size(); i++) {
			if (containsNewline(elements.get(i).getElement().getPrefix())) {
				groupSizes.add(currentGroupSize);
				currentGroupSize = 1;
			} else {
				currentGroupSize++;
			}
		}
		groupSizes.add(currentGroupSize);

		// Need at least 2 groups
		if (groupSizes.size() < 2) {
			return false;
		}

		// All groups must have the same size >= 2
		int firstGroupSize = groupSizes.get(0);
		if (firstGroupSize < 2) {
			return false;
		}

		for (int groupSize : groupSizes) {
			if (groupSize != firstGroupSize) {
				return false;
			}
		}

		return true;
	}

	protected static boolean containsNewline(Space space) {
		return space.getWhitespace().contains("\n");
	}

	protected static String extractIndentAfterLastNewline(String whitespace) {
		int lastNewline = whitespace.lastIndexOf('\n');
		if (lastNewline < 0) {
			return whitespace;
		}
		return whitespace.substring(lastNewline + 1);
	}
}
