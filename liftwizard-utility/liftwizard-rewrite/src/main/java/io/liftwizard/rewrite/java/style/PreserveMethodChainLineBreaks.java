/*
 * Copyright 2026 Craig Motlin
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;

/**
 * Restores the line break before a chained call when another recipe in the same run replaced the tail of a multi-line
 * method chain.
 *
 * <p>A Refaster recipe such as {@code iterable.select(predicate).notEmpty()} to {@code iterable.anySatisfy(predicate)}
 * replaces the whole matched invocation with its template, and the template text has no line break before
 * {@code .anySatisfy}, so
 * <pre>{@code
 * list
 *     .asLazy()
 *     .select(predicate)
 *     .notEmpty();
 * }</pre>
 * becomes {@code .asLazy().anySatisfy(predicate)} on one line. Refaster templates cannot express whitespace, and the
 * generated recipes offer no hook after the replacement.
 *
 * <p>JavaTemplate keeps the substituted receiver ({@code list.asLazy()}) as the same tree, with the same id. The
 * scanner records, for every receiver that was followed by a line break, the whitespace before the next {@code .call};
 * the edit phase runs after the other recipes in the composite and puts that whitespace back wherever the receiver lost
 * it. List this recipe last in a composite.
 */
public class PreserveMethodChainLineBreaks
	extends ScanningRecipe<Map<UUID, Space>>
{
	@Override
	public String getDisplayName()
	{
		return "Preserve line breaks in multi-line method chains";
	}

	@Override
	public String getDescription()
	{
		return (
			"Restores the line break before a chained method call when an earlier recipe in the same run replaced the "
			+ "tail of a multi-line method chain and joined the call onto the previous line."
		);
	}

	@Override
	public Map<UUID, Space> getInitialValue(ExecutionContext ctx)
	{
		return new HashMap<>();
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getScanner(Map<UUID, Space> acc)
	{
		return new JavaIsoVisitor<>()
		{
			@Override
			public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx)
			{
				JRightPadded<Expression> select = method.getPadding().getSelect();
				if (select != null && select.getAfter().getWhitespace().contains("\n"))
				{
					acc.putIfAbsent(select.getElement().getId(), select.getAfter());
				}
				return super.visitMethodInvocation(method, ctx);
			}
		};
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor(Map<UUID, Space> acc)
	{
		return new JavaIsoVisitor<>()
		{
			@Override
			public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx)
			{
				J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
				JRightPadded<Expression> select = mi.getPadding().getSelect();
				if (select == null || select.getAfter().getWhitespace().contains("\n"))
				{
					return mi;
				}
				Space original = acc.get(select.getElement().getId());
				if (original == null)
				{
					return mi;
				}
				return mi.getPadding().withSelect(select.withAfter(original));
			}
		};
	}
}
