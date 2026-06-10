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

import java.time.Duration;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Transforms {@code stream()} chains on Eclipse Collections lists to direct Eclipse Collections
 * call chains, removing the {@code Stream} bridge entirely.
 *
 * <p>Example:
 * <pre>{@code
 * // Before
 * ecList.stream().skip(1).toArray()
 * ecList.stream().filter(each -> !each.isEmpty()).anyMatch(each -> each.length() > 3)
 *
 * // After
 * ecList.drop(1).toArray()
 * ecList.select(each -> !each.isEmpty()).anySatisfy(each -> each.length() > 3)
 * }</pre>
 *
 * <p>Chain walking, the operation translation tables, and the shared guards live in
 * {@link AbstractECStreamChainVisitor}. This recipe adds the collection-specific root handling:
 * the receiver must be assignable to {@code ListIterable}, and the {@code stream()} call is
 * removed by reattaching the chain to the receiver.
 *
 * <p>The receiver is restricted to lists on purpose: Eclipse Collections operations preserve the
 * container kind, so on a set {@code collect} deduplicates mapped values while
 * {@code stream().map} does not. List receivers keep order and duplicates in both worlds.
 */
public class ECStreamChainToListIterable extends Recipe {

	private static final MethodMatcher STREAM_MATCHER = new MethodMatcher("java.util.Collection stream()", true);

	@Override
	public String getDisplayName() {
		return "`ecList.stream()` chains to Eclipse Collections call chains";
	}

	@Override
	public String getDescription() {
		return (
			"Transforms `stream()` chains on Eclipse Collections lists to direct Eclipse Collections call chains, "
			+ "renaming intermediate operations to their Eclipse Collections equivalents "
			+ "(`skip` to `drop`, `limit` to `take`, `filter` to `select`, `map` to `collect`, "
			+ "`anyMatch` to `anySatisfy`, ...). This eliminates the unnecessary Stream intermediary "
			+ "since Eclipse Collections has these methods directly."
		);
	}

	@Override
	public Set<String> getTags() {
		return Sets.fixedSize.with("eclipse-collections");
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofSeconds(15);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(new UsesMethod<>(STREAM_MATCHER), new ECStreamChainToListIterableVisitor());
	}

	private static final class ECStreamChainToListIterableVisitor extends AbstractECStreamChainVisitor {

		@Override
		protected boolean isChainRoot(J.MethodInvocation invocation) {
			return STREAM_MATCHER.matches(invocation);
		}

		@Override
		protected boolean isRootTranslatable(J.MethodInvocation root) {
			Expression receiver = root.getSelect();
			return (
				receiver != null
				&& TypeUtils.isAssignableTo("org.eclipse.collections.api.list.ListIterable", receiver.getType())
			);
		}

		@Override
		protected J.MethodInvocation replaceRootIn(J.MethodInvocation linkAboveRoot) {
			J.MethodInvocation streamCall = (J.MethodInvocation) linkAboveRoot.getSelect();
			return linkAboveRoot.withSelect(streamCall.getSelect().withPrefix(streamCall.getPrefix()));
		}
	}
}
