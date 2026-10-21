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

package io.liftwizard.reladomo.utc.infinity.timestamp;

import java.util.List;
import java.util.TimeZone;

import io.liftwizard.reladomo.utc.infinity.timestamp.model.LocalAsOfObjectFinder;
import io.liftwizard.reladomo.utc.infinity.timestamp.model.LocalTimestampObjectFinder;
import io.liftwizard.reladomo.utc.infinity.timestamp.model.NewYorkInfinityObjectFinder;
import io.liftwizard.reladomo.utc.infinity.timestamp.model.UtcCompliantObjectFinder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtcInfinityModelValidatorTest
{
	@Test
	void compliantModel()
	{
		assertThatCode(() ->
			UtcInfinityModelValidator.validate(List.of(UtcCompliantObjectFinder.getFinderInstance()))
		).doesNotThrowAnyException();
	}

	@Test
	void reportsEveryViolation()
	{
		String expectedMessage = """
		The JVM default time zone is %s, not UTC. Every Reladomo timestamp attribute must use \
		timezoneConversion="convert-to-utc" and every as-of attribute must use UtcInfinityTimestamp:
		    io.liftwizard.reladomo.utc.infinity.timestamp.model.LocalTimestampObject.createdOn: \
		timezoneConversion is not "convert-to-utc".
		    io.liftwizard.reladomo.utc.infinity.timestamp.model.NewYorkInfinityObject.system: \
		infinityDate is 9999-12-02T04:59:00Z, not UtcInfinityTimestamp's 9999-12-01T23:59:00Z.
		    io.liftwizard.reladomo.utc.infinity.timestamp.model.LocalAsOfObject.systemFrom: \
		timezoneConversion is not "convert-to-utc".
		    io.liftwizard.reladomo.utc.infinity.timestamp.model.LocalAsOfObject.systemTo: \
		timezoneConversion is not "convert-to-utc".""".formatted(TimeZone.getDefault().getID());

		assertThatThrownBy(() ->
			UtcInfinityModelValidator.validate(
				List.of(
					UtcCompliantObjectFinder.getFinderInstance(),
					LocalTimestampObjectFinder.getFinderInstance(),
					NewYorkInfinityObjectFinder.getFinderInstance(),
					LocalAsOfObjectFinder.getFinderInstance()
				)
			)
		)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(expectedMessage);
	}
}
