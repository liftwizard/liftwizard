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

import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

/**
 * Checks that a Reladomo model works in a JVM whose default time zone is not UTC.
 *
 * <p>Once Reladomo's default time zone is UTC, timestamp attributes without {@code timezoneConversion="convert-to-utc"}
 * no longer round-trip, and as-of attributes whose infinity is not {@link UtcInfinityTimestamp} do not match infinity
 * rows. Neither problem shows up in a JVM whose default time zone is UTC, so callers validate only in other time
 * zones.</p>
 */
public final class UtcInfinityModelValidator
{
	private static final String CONVERT_TO_UTC_REASON = "timezoneConversion is not \"convert-to-utc\".";

	private UtcInfinityModelValidator()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * Validates every class in the loaded Reladomo runtime configurations when the JVM default time zone is not UTC.
	 */
	public static void validateRuntimeConfigurationIfNotUtc()
	{
		if ("UTC".equals(TimeZone.getDefault().getID()))
		{
			return;
		}

		MutableList<RelatedFinder<?>> finders = Lists.mutable.empty();
		for (MithraRuntimeCacheController controller : MithraManagerProvider.getMithraManager().getRuntimeCacheControllerSet())
		{
			finders.add(controller.getFinderInstance());
		}
		UtcInfinityModelValidator.validate(finders);
	}

	public static void validate(Iterable<? extends RelatedFinder<?>> finders)
	{
		MutableList<String> violations = Lists.mutable.empty();
		for (RelatedFinder<?> finder : finders)
		{
			UtcInfinityModelValidator.addViolations(finder, violations);
		}

		if (violations.notEmpty())
		{
			String message = """
			The JVM default time zone is %s, not UTC. Every Reladomo timestamp attribute must use \
			timezoneConversion="convert-to-utc" and every as-of attribute must use UtcInfinityTimestamp:
			    %s""".formatted(TimeZone.getDefault().getID(), String.join("\n    ", violations));
			throw new IllegalStateException(message);
		}
	}

	private static void addViolations(RelatedFinder<?> finder, List<String> violations)
	{
		String className = finder.getFinderClassName().replaceFirst("Finder$", "");

		// Persistent attributes include the from and to attributes of as-of attributes.
		for (Attribute<?, ?> attribute : finder.getPersistentAttributes())
		{
			if (
				attribute instanceof TimestampAttribute<?> timestampAttribute
				&& !timestampAttribute.requiresConversionFromUtc()
			)
			{
				violations.add(className + "." + attribute.getAttributeName() + ": " + CONVERT_TO_UTC_REASON);
			}
		}

		AsOfAttribute<?>[] asOfAttributes = finder.getAsOfAttributes();
		if (asOfAttributes == null)
		{
			return;
		}
		for (AsOfAttribute<?> asOfAttribute : asOfAttributes)
		{
			if (asOfAttribute.isInfinityNull())
			{
				continue;
			}
			Instant infinity = asOfAttribute.getInfinityDate().toInstant();
			Instant expected = UtcInfinityTimestamp.getDefaultInfinityInstant();
			if (!infinity.equals(expected))
			{
				violations.add(
					"%s.%s: infinityDate is %s, not UtcInfinityTimestamp's %s.".formatted(
						className,
						asOfAttribute.getAttributeName(),
						infinity,
						expected
					)
				);
			}
		}
	}
}
