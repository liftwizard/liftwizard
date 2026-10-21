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

import java.util.TimeZone;

import com.gs.fw.common.mithra.util.MithraTimestamp;

/**
 * Reladomo binds an as-of attribute's infinity value with the sentinel {@link MithraTimestamp#DefaultTimeZone}, which
 * writes the JVM-local wall clock. That only matches the conventional {@code 9999-12-01 23:59:00} literal when the
 * infinity is a JVM-local timestamp like {@code DefaultInfinityTimestamp}. For {@link UtcInfinityTimestamp}, the UTC
 * wall clock must be written instead.
 */
final class UtcInfinityTimeZones
{
	private UtcInfinityTimeZones()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * Replaces Reladomo's infinity sentinel time zone with UTC. The comparison is by identity because Reladomo only
	 * passes the {@link MithraTimestamp#DefaultTimeZone} instance itself when binding infinity. Every other time zone
	 * is returned unchanged.
	 */
	static TimeZone forWriting(TimeZone timeZone)
	{
		return timeZone == MithraTimestamp.DefaultTimeZone ? MithraTimestamp.UtcTimeZone : timeZone;
	}
}
