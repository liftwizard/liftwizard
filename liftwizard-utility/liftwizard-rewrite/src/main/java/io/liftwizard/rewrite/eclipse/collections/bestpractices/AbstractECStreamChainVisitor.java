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

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Shared engine for translating whole {@code java.util.stream.Stream} call chains to Eclipse
 * Collections call chains. Subclasses define what the chain root looks like (an array source like
 * {@code Arrays.stream(array)}, or a collection source like {@code ecList.stream()}) and how to
 * replace it; this visitor walks the full chain — down the selects to the root, up the cursor to
 * the terminal consumer — and translates all-or-nothing.
 *
 * <p>Intermediate operations are renamed to their Eclipse Collections equivalents
 * ({@code skip} → {@code drop}, {@code limit} → {@code take}, {@code filter} → {@code select},
 * {@code map} → {@code collect}, {@code sorted} → {@code toSortedList}, {@code distinct}
 * unchanged) and the chain must end in a terminal whose result type is compatible in both worlds
 * ({@code toList()}, {@code collect(Collectors.toList())}, {@code toArray()}, {@code iterator()},
 * {@code forEach}, {@code anyMatch} → {@code anySatisfy}, {@code allMatch} →
 * {@code allSatisfy}, {@code noneMatch} → {@code noneSatisfy}).
 *
 * <p>Guards shared by all chain sources:
 * <ul>
 * <li>Functional arguments must be lambdas or method references; a variable of a
 * {@code java.util.function} type would not compile against the Eclipse Collections
 * functional interfaces.</li>
 * <li>{@code skip}/{@code limit} arguments must be {@code int}; {@code Stream.skip(long)} has no
 * {@code drop(long)} equivalent.</li>
 * <li>Collector terminals must be known {@code Collectors} factory calls.</li>
 * <li>{@code count()} only translates to {@code size()} in literal comparisons where the
 * {@code long} to {@code int} return-type difference cannot change assignment or inference.</li>
 * <li>Stream-typed usages (assignment, argument, return) and other untranslatable links leave the
 * whole chain untouched.</li>
 * </ul>
 */
abstract class AbstractECStreamChainVisitor extends JavaIsoVisitor<ExecutionContext> {

	private static final MethodMatcher COLLECT_MATCHER = new MethodMatcher(
		"java.util.stream.Stream collect(java.util.stream.Collector)"
	);

	private static final MethodMatcher TO_LIST_MATCHER = new MethodMatcher("java.util.stream.Collectors toList()");

	private static final MethodMatcher TO_SET_MATCHER = new MethodMatcher("java.util.stream.Collectors toSet()");

	private static final MethodMatcher TO_UNMODIFIABLE_LIST_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors toUnmodifiableList()"
	);

	private static final MethodMatcher TO_UNMODIFIABLE_SET_MATCHER = new MethodMatcher(
		"java.util.stream.Collectors toUnmodifiableSet()"
	);

	/**
	 * Whether this invocation is the source of a translatable chain, e.g. {@code Arrays.stream(array)}
	 * or {@code ecList.stream()}. Matched syntactically; {@link #isRootTranslatable} applies the
	 * subclass's semantic guards.
	 */
	protected abstract boolean isChainRoot(J.MethodInvocation invocation);

	protected abstract boolean isRootTranslatable(J.MethodInvocation root);

	/**
	 * Replaces a chain root that sits inside a fully translatable chain. Called while visiting the
	 * root itself, so {@code this.getCursor()} points at it. Sources that replace the root with
	 * another invocation (e.g. {@code Arrays.stream(array)} → {@code ArrayAdapter.adapt(array)})
	 * override this; sources that simply remove the root (e.g. {@code ecList.stream()} →
	 * {@code ecList}) override {@link #replaceRootIn} instead.
	 */
	protected J.MethodInvocation visitTranslatableRoot(J.MethodInvocation root) {
		return root;
	}

	/**
	 * Adjusts the link directly above the chain root, for sources where the root is removed rather
	 * than replaced: the override swaps the link's select from the root invocation to the underlying
	 * collection expression.
	 */
	protected J.MethodInvocation replaceRootIn(J.MethodInvocation linkAboveRoot) {
		return linkAboveRoot;
	}

	@Override
	public final J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
		boolean isRoot = this.isChainRoot(method);
		boolean isNamedLink =
			intermediateTranslation(method.getSimpleName()) != null || terminalTranslation(method) != null;

		// Validate against the original subtree, before super.visitMethodInvocation transforms descendants
		boolean translatable = (isRoot || isNamedLink) && this.isInTranslatableChain(method, isRoot);
		boolean selectIsRoot =
			method.getSelect() instanceof J.MethodInvocation selectInvocation && this.isChainRoot(selectInvocation);

		J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
		if (!translatable) {
			return mi;
		}

		if (isRoot) {
			return this.visitTranslatableRoot(mi);
		}

		if (selectIsRoot) {
			mi = this.replaceRootIn(mi);
		}

		String streamName = method.getSimpleName();
		String ecName = intermediateTranslation(streamName) != null
			? intermediateTranslation(streamName)
			: terminalTranslation(method);
		boolean isCollectorTerminal = "collect".equals(streamName) && terminalTranslation(method) != null;
		if (isCollectorTerminal) {
			this.maybeRemoveImport("java.util.stream.Collectors");
		}

		// Stream.toList() is the materialization step, but every translated intermediate (drop, take,
		// select, collect, distinct) already returns a fresh MutableList, so a trailing toList() would
		// copy the list a second time. Elide it. With no intermediates the toList() stays: it is the
		// defensive copy that keeps the result independent of the source.
		if ("toList".equals(ecName) && !selectIsRoot) {
			return ((J.MethodInvocation) mi.getSelect()).withPrefix(mi.getPrefix());
		}

		if (isCollectorTerminal) {
			if (mi.getMethodType() != null) {
				JavaType.Method methodType = mi
					.getMethodType()
					.withName(ecName)
					.withParameterNames(List.of())
					.withParameterTypes(List.of());
				mi = mi.withName(mi.getName().withType(methodType)).withMethodType(methodType);
			}
			mi = mi.withArguments(List.of());
		}

		if ("findFirst".equals(streamName)) {
			return collapseFilterTerminal(mi, "detectOptional").withPrefix(mi.getPrefix());
		}

		if (streamName.equals(ecName)) {
			return mi;
		}

		JavaType.Method renamedType = mi.getMethodType() == null ? null : mi.getMethodType().withName(ecName);
		return mi.withName(mi.getName().withSimpleName(ecName).withType(renamedType)).withMethodType(renamedType);
	}

	/**
	 * Walks the full call chain containing {@code node} — down the selects to the chain root
	 * and up the cursor to the terminal consumer — and checks that every link up to and including
	 * the terminal is translatable. {@code node} must sit at or below the terminal.
	 */
	private boolean isInTranslatableChain(J.MethodInvocation node, boolean isRoot) {
		J.MethodInvocation root = node;
		MutableList<J.MethodInvocation> chain = Lists.mutable.empty();

		if (!isRoot) {
			MutableList<J.MethodInvocation> linksBelow = Lists.mutable.empty();
			J.MethodInvocation current = node;
			while (true) {
				if (!(current.getSelect() instanceof J.MethodInvocation selectInvocation)) {
					return false;
				}
				if (this.isChainRoot(selectInvocation)) {
					root = selectInvocation;
					break;
				}
				linksBelow.add(selectInvocation);
				current = selectInvocation;
			}
			chain.addAll(linksBelow.toReversed());
			chain.add(node);
		}

		if (!this.isRootTranslatable(root)) {
			return false;
		}

		int nodeIndex = chain.size() - 1;

		// Ascend until the chain reaches a valid terminal or leaves method-invocation territory
		Cursor cursor = this.getCursor();
		J.MethodInvocation current = node;
		while (chain.isEmpty() || !isValidTerminal(chain.getLast(), cursor)) {
			Cursor parentCursor = cursor.getParentTreeCursor();
			if (!(parentCursor.getValue() instanceof J.MethodInvocation parentInvocation)) {
				return false;
			}
			Expression select = parentInvocation.getSelect();
			if (select == null || !select.getId().equals(current.getId())) {
				return false;
			}
			chain.add(parentInvocation);
			current = parentInvocation;
			cursor = parentCursor;
		}

		int terminalIndex = chain.size() - 1;
		if (nodeIndex > terminalIndex) {
			return false;
		}

		for (int i = 0; i < terminalIndex; i++) {
			J.MethodInvocation link = chain.get(i);
			if (intermediateTranslation(link.getSimpleName()) == null || !argumentsAreTranslatable(link)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidTerminal(J.MethodInvocation link, Cursor cursor) {
		return terminalTranslation(link) != null && terminalArgumentsAreTranslatable(link, cursor);
	}

	private static String intermediateTranslation(String streamName) {
		return switch (streamName) {
			case "skip" -> "drop";
			case "limit" -> "take";
			case "filter" -> "select";
			case "map" -> "collect";
			case "distinct" -> "distinct";
			case "sorted" -> "toSortedList";
			case "findFirst" -> "findFirst";
			default -> null;
		};
	}

	private static String terminalTranslation(J.MethodInvocation link) {
		return switch (link.getSimpleName()) {
			case "anyMatch" -> "anySatisfy";
			case "allMatch" -> "allSatisfy";
			case "noneMatch" -> "noneSatisfy";
			case "toList", "toArray", "iterator", "forEach" -> link.getSimpleName();
			case "collect" -> collectorTerminalTranslation(link);
			case "count" -> "size";
			case "findFirst" -> "detectOptional";
			default -> null;
		};
	}

	private static boolean argumentsAreTranslatable(J.MethodInvocation link) {
		List<Expression> arguments = realArguments(link);
		return switch (link.getSimpleName()) {
			// Eclipse Collections drop(int)/take(int) cannot accept Stream.skip(long)'s long arguments
			case "skip", "limit" -> arguments.size() == 1 && isIntTyped(arguments.get(0));
			// Lambdas and method references compile against either functional interface family, but a
			// variable of a java.util.function type would not
			case "filter", "map", "anyMatch", "allMatch", "noneMatch", "forEach" -> arguments.size() == 1
			&& (arguments.get(0) instanceof J.Lambda || arguments.get(0) instanceof J.MemberReference);
			case "sorted" -> arguments.size() <= 1;
			case "findFirst", "distinct", "toList", "toArray", "iterator" -> arguments.isEmpty();
			default -> false;
		};
	}

	private static boolean terminalArgumentsAreTranslatable(J.MethodInvocation link, Cursor cursor) {
		List<Expression> arguments = realArguments(link);
		return switch (link.getSimpleName()) {
			case "collect" -> collectorTerminalTranslation(link) != null;
			case "count" -> arguments.isEmpty() && isSafeCountContext(cursor);
			case "findFirst" -> arguments.isEmpty() && isFilterCall(link.getSelect());
			default -> argumentsAreTranslatable(link);
		};
	}

	private static String collectorTerminalTranslation(J.MethodInvocation link) {
		if (!COLLECT_MATCHER.matches(link)) {
			return null;
		}

		List<Expression> arguments = realArguments(link);
		if (arguments.size() != 1 || !(arguments.get(0) instanceof J.MethodInvocation collectorCall)) {
			return null;
		}

		if (TO_LIST_MATCHER.matches(collectorCall)) {
			return "toList";
		}
		if (TO_SET_MATCHER.matches(collectorCall)) {
			return "toSet";
		}
		if (TO_UNMODIFIABLE_LIST_MATCHER.matches(collectorCall)) {
			return "toImmutableList";
		}
		if (TO_UNMODIFIABLE_SET_MATCHER.matches(collectorCall)) {
			return "toImmutableSet";
		}
		return null;
	}

	private static boolean isSafeCountContext(Cursor cursor) {
		Cursor parentCursor = cursor.getParentTreeCursor();
		if (!(parentCursor.getValue() instanceof J.Binary binary)) {
			return false;
		}
		if (!isComparison(binary.getOperator())) {
			return false;
		}
		J.MethodInvocation count = (J.MethodInvocation) cursor.getValue();
		if (binary.getLeft().getId().equals(count.getId())) {
			return isNumericLiteral(binary.getRight());
		}
		return binary.getRight().getId().equals(count.getId()) && isNumericLiteral(binary.getLeft());
	}

	private static boolean isComparison(J.Binary.Type operator) {
		return switch (operator) {
			case Equal, NotEqual, LessThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual -> true;
			default -> false;
		};
	}

	private static boolean isNumericLiteral(Expression expression) {
		if (!(expression instanceof J.Literal literal)) {
			return false;
		}
		return literal.getValue() instanceof Number;
	}

	private static boolean isFilterCall(Expression expression) {
		return (
			expression instanceof J.MethodInvocation methodInvocation
			&& "filter".equals(methodInvocation.getSimpleName())
		);
	}

	private static J.MethodInvocation collapseFilterTerminal(J.MethodInvocation method, String methodName) {
		J.MethodInvocation selectCall = (J.MethodInvocation) method.getSelect();
		JavaType.Method renamedType = method.getMethodType() == null
			? null
			: method.getMethodType().withName(methodName);
		return method
			.withSelect(selectCall.getSelect())
			.withName(method.getName().withSimpleName(methodName).withType(renamedType))
			.withArguments(selectCall.getArguments())
			.withMethodType(renamedType);
	}

	private static boolean isIntTyped(Expression expression) {
		JavaType type = expression.getType();
		return type == JavaType.Primitive.Int || TypeUtils.isOfClassType(type, "java.lang.Integer");
	}

	protected static List<Expression> realArguments(J.MethodInvocation invocation) {
		List<Expression> arguments = invocation.getArguments();
		if (arguments.size() == 1 && arguments.get(0) instanceof J.Empty) {
			return List.of();
		}
		return arguments;
	}
}
