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

package io.liftwizard.servlet.logging.typesafe;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class StructuredArgumentsPath {

	private final String absolute;
	private final String full;
	private String template;
	private String baseUriPath;

	public StructuredArgumentsPath(String absolute, String full) {
		this.absolute = requireNonNull(absolute);
		this.full = requireNonNull(full);
	}

	@JsonProperty
	public String getAbsolute() {
		return this.absolute;
	}

	@JsonProperty
	public String getFull() {
		return this.full;
	}

	public void setTemplate(String template) {
		checkState(this.template == null, this.template);
		this.template = requireNonNull(template);
	}

	@JsonProperty
	public String getTemplate() {
		return this.template;
	}

	public void setBaseUriPath(String baseUriPath) {
		checkState(this.baseUriPath == null, this.baseUriPath);
		this.baseUriPath = requireNonNull(baseUriPath);
	}
}
