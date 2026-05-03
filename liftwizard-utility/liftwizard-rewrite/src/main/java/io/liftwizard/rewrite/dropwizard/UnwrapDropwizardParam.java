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

package io.liftwizard.rewrite.dropwizard;

import java.text.MessageFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class UnwrapDropwizardParam extends Recipe {

	@Option(
		displayName = "Old param type",
		description = "Fully qualified name of the Dropwizard *Param class to unwrap.",
		example = "io.dropwizard.jersey.params.BooleanParam"
	)
	private final String oldParamType;

	@Option(
		displayName = "New unwrapped type",
		description = "Fully qualified name of the replacement type.",
		example = "java.lang.Boolean"
	)
	private final String newType;

	@JsonCreator
	public UnwrapDropwizardParam(
		@JsonProperty("oldParamType") String oldParamType,
		@JsonProperty("newType") String newType
	) {
		this.oldParamType = oldParamType;
		this.newType = newType;
	}

	@Override
	public String getDisplayName() {
		return MessageFormat.format("Unwrap `{0}` to `{1}`", getSimpleName(this.oldParamType), getSimpleName(this.newType));
	}

	@Override
	public String getDescription() {
		return MessageFormat.format(
			"Replaces `{0}` with `{1}` and removes `.get()` calls, "
			+ "since the Dropwizard *Param wrapper is no longer needed.",
			this.oldParamType,
			this.newType
		);
	}

	@Override
	public List<Recipe> getRecipeList() {
		return List.of(
			new UnwrapGetCalls(this.oldParamType),
			new ChangeType(this.oldParamType, this.newType, true)
		);
	}

	private static String getSimpleName(String fqn) {
		int lastDot = fqn.lastIndexOf('.');
		return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
	}

	private static class UnwrapGetCalls extends Recipe {

		private final String oldParamType;
		private final MethodMatcher getMatcher;

		UnwrapGetCalls(String oldParamType) {
			this.oldParamType = oldParamType;
			this.getMatcher = new MethodMatcher(oldParamType + " get()");
		}

		@Override
		public String getDisplayName() {
			return "Remove `.get()` calls on `" + getSimpleName(this.oldParamType) + '`';
		}

		@Override
		public String getDescription() {
			return "Replaces `param.get()` with `param` for " + this.oldParamType + " instances.";
		}

		@Override
		public TreeVisitor<?, ExecutionContext> getVisitor() {
			return new JavaVisitor<>() {
				@Override
				public J visitMethodInvocation(
					J.MethodInvocation method,
					ExecutionContext ctx
				) {
					J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

					if (!UnwrapGetCalls.this.getMatcher.matches(mi)) {
						return mi;
					}

					Expression select = mi.getSelect();
					if (select == null) {
						return mi;
					}

					return select.withPrefix(mi.getPrefix());
				}
			};
		}
	}
}
