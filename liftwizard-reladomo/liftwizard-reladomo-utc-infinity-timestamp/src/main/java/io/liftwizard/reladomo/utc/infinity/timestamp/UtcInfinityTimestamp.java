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

package io.liftwizard.reladomo.utc.infinity.timestamp;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

import com.gs.fw.common.mithra.util.MithraTimestamp;

public final class UtcInfinityTimestamp {

	private static final MithraTimestamp DEFAULT_INFINITY;
	private static final Instant DEFAULT_INFINITY_INSTANT;

	static {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(Calendar.YEAR, 9999);
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		DEFAULT_INFINITY = new MithraTimestamp(calendar.getTime().getTime(), false);
		DEFAULT_INFINITY_INSTANT = Instant.parse("9999-12-01T23:59:00Z");
	}

	private UtcInfinityTimestamp() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static MithraTimestamp getDefaultInfinity() {
		return DEFAULT_INFINITY;
	}

	public static Instant getDefaultInfinityInstant() {
		return DEFAULT_INFINITY_INSTANT;
	}
}
