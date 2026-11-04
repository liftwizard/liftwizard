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

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Validator;

import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneIdNameValidatorTest
{
	private final Validator validator = BaseValidator.newValidator();

	@Test
	void validTimeZonesHaveNoViolations()
	{
		assertThat(this.validate(null)).isEmpty();
		assertThat(this.validate("UTC")).isEmpty();
		assertThat(this.validate("America/New_York")).isEmpty();
		assertThat(this.validate("+05:00")).isEmpty();
		assertThat(this.validate("UTC+05:00")).isEmpty();
		assertThat(this.validate("Z")).isEmpty();
	}

	@Test
	void invalidTimeZonesSuggestCloseMatches()
	{
		// missing underscore
		assertThat(this.validate("America/NewYork")).hasSameElementsAs(
			Set.of(
				"timeZoneName 'America/NewYork' is not a time zone ID. Did you mean: America/New_York? See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
		// wrong case
		assertThat(this.validate("america/new_york")).hasSameElementsAs(
			Set.of(
				"timeZoneName 'america/new_york' is not a time zone ID. Did you mean: America/New_York? See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
		// city without region
		assertThat(this.validate("New York")).hasSameElementsAs(
			Set.of(
				"timeZoneName 'New York' is not a time zone ID. Did you mean: America/New_York? See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
		// typo
		assertThat(this.validate("Europe/Lodnon")).hasSameElementsAs(
			Set.of(
				"timeZoneName 'Europe/Lodnon' is not a time zone ID. Did you mean: Europe/London? See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
	}

	@Test
	void invalidTimeZonesWithoutCloseMatchesOmitSuggestions()
	{
		// no close match
		assertThat(this.validate("Nowhere/Land")).hasSameElementsAs(
			Set.of(
				"timeZoneName 'Nowhere/Land' is not a time zone ID. See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
		// message interpolation syntax is not evaluated
		assertThat(this.validate("${1+1}{value}\\")).hasSameElementsAs(
			Set.of(
				"timeZoneName '${1+1}{value}\\' is not a time zone ID. See java.time.ZoneId.getAvailableZoneIds() for all IDs."
			)
		);
	}

	private Set<String> validate(String timeZoneName)
	{
		return this.validator
			.validate(new TimeZoneHolder(timeZoneName))
			.stream()
			.map((violation) -> violation.getPropertyPath() + " " + violation.getMessage())
			.collect(Collectors.toSet());
	}

	static final class TimeZoneHolder
	{
		private final String timeZoneName;

		TimeZoneHolder(String timeZoneName)
		{
			this.timeZoneName = timeZoneName;
		}

		@ZoneIdName
		public String getTimeZoneName()
		{
			return this.timeZoneName;
		}
	}
}
