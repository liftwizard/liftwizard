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

package io.liftwizard.dropwizard.configuration.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated {@link String} must be a time zone ID accepted by {@link java.time.ZoneId#of(String)}, either a region ID like {@code America/New_York} or an offset like {@code +05:00}. {@code null} is valid; combine with {@code @NotNull} to reject it.
 *
 * <p>When the value is invalid, the violation message names the bad value and suggests the closest valid IDs.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.FIELD,
	ElementType.METHOD,
	ElementType.PARAMETER,
	ElementType.ANNOTATION_TYPE,
	ElementType.TYPE_USE,
})
@Constraint(validatedBy = ZoneIdNameValidator.class)
public @interface ZoneIdName
{
	String message() default "is not a time zone ID";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
