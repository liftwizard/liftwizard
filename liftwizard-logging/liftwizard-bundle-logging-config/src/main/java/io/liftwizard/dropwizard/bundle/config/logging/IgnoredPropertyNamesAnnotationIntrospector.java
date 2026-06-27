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

package io.liftwizard.dropwizard.bundle.config.logging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * An {@link com.fasterxml.jackson.databind.AnnotationIntrospector} that ignores bean properties by name on a specific
 * declaring type (and its subtypes), without requiring a {@code @JsonIgnore} annotation or mix-in on the target type. It
 * is meant to be paired with the mapper's existing introspector so that all other annotations continue to apply.
 *
 * <p>This is used to skip properties that cannot be serialized in some deployments, such as the Dropwizard request log
 * factory ({@code requestLog} on {@link io.dropwizard.server.AbstractServerFactory}) in an application that runs a
 * non-logback logging backend without logback-access on the classpath. The ignore is scoped to the declaring type so
 * that a property of the same name on an unrelated type is left untouched.
 */
public final class IgnoredPropertyNamesAnnotationIntrospector extends NopAnnotationIntrospector {

	private final Class<?> declaringType;
	private final JsonIgnoreProperties.Value ignoredProperties;

	public IgnoredPropertyNamesAnnotationIntrospector(Class<?> declaringType, String... propertyNames) {
		this.declaringType = declaringType;
		this.ignoredProperties = JsonIgnoreProperties.Value.forIgnoredProperties(propertyNames);
	}

	@Override
	public JsonIgnoreProperties.Value findPropertyIgnoralByName(MapperConfig<?> config, Annotated annotated) {
		if (
			annotated instanceof AnnotatedClass annotatedClass
			&& this.declaringType.isAssignableFrom(annotatedClass.getRawType())
		) {
			return this.ignoredProperties;
		}
		return super.findPropertyIgnoralByName(config, annotated);
	}
}
