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

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.tuple.primitive.ObjectIntPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

/**
 * Validates {@link ZoneIdName}. Invalid values get a message that names the value, suggests up to {@value #MAX_SUGGESTIONS} close matches, and points to {@link ZoneId#getAvailableZoneIds()} for the full list.
 */
public class ZoneIdNameValidator
	implements ConstraintValidator<ZoneIdName, String>
{
	private static final int MAX_SUGGESTIONS = 3;
	private static final int MAX_EDIT_DISTANCE = 2;

	private static final Pattern IGNORED_CHARACTERS = Pattern.compile("[_\\s-]");
	private static final Pattern MESSAGE_SYNTAX = Pattern.compile("[\\\\{}$]");

	private static final ImmutableList<String> ZONE_IDS = Lists.immutable
		.withAll(ZoneId.getAvailableZoneIds())
		.toSortedList()
		.toImmutable();

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		if (value == null || isZoneId(value))
		{
			return true;
		}

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(escapeMessageSyntax(getMessage(value))).addConstraintViolation();
		return false;
	}

	private static boolean isZoneId(String value)
	{
		try
		{
			ZoneId.of(value);
			return true;
		}
		catch (DateTimeException ignored)
		{
			return false;
		}
	}

	private static String getMessage(String value)
	{
		ImmutableList<String> suggestions = getSuggestions(value);
		String suggestionClause = suggestions.isEmpty() ? "" : suggestions.makeString(" Did you mean: ", ", ", "?");
		return "'%s' is not a time zone ID.%s See java.time.ZoneId.getAvailableZoneIds() for all IDs.".formatted(
			value,
			suggestionClause
		);
	}

	private static ImmutableList<String> getSuggestions(String value)
	{
		String normalizedValue = normalize(value);

		ImmutableList<String> normalizedMatches = ZONE_IDS.select(
			(zoneId) -> normalize(zoneId).equals(normalizedValue) || normalize(getCity(zoneId)).equals(normalizedValue)
		);
		if (normalizedMatches.notEmpty())
		{
			return normalizedMatches.take(MAX_SUGGESTIONS);
		}

		return ZONE_IDS.collect((zoneId) ->
			PrimitiveTuples.pair(zoneId, editDistance(normalizedValue, normalize(zoneId)))
		)
			.select((pair) -> pair.getTwo() <= MAX_EDIT_DISTANCE)
			.toSortedListBy(ObjectIntPair::getTwo)
			.take(MAX_SUGGESTIONS)
			.collect(ObjectIntPair::getOne)
			.toImmutable();
	}

	private static String getCity(String zoneId)
	{
		return zoneId.substring(zoneId.lastIndexOf('/') + 1);
	}

	private static String normalize(String value)
	{
		return IGNORED_CHARACTERS.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("");
	}

	private static int editDistance(String left, String right)
	{
		int[] previousRow = new int[right.length() + 1];
		int[] currentRow = new int[right.length() + 1];
		for (var column = 0; column <= right.length(); column++)
		{
			previousRow[column] = column;
		}

		for (var row = 1; row <= left.length(); row++)
		{
			currentRow[0] = row;
			for (var column = 1; column <= right.length(); column++)
			{
				int substitutionCost = left.charAt(row - 1) == right.charAt(column - 1) ? 0 : 1;
				currentRow[column] = Math.min(
					Math.min(currentRow[column - 1] + 1, previousRow[column] + 1),
					previousRow[column - 1] + substitutionCost
				);
			}
			int[] swap = previousRow;
			previousRow = currentRow;
			currentRow = swap;
		}
		return previousRow[right.length()];
	}

	/**
	 * Escapes characters that message interpolation would otherwise treat as parameter or expression syntax, so the user-supplied value appears verbatim.
	 */
	private static String escapeMessageSyntax(String message)
	{
		return MESSAGE_SYNTAX.matcher(message).replaceAll((matchResult) ->
			Matcher.quoteReplacement("\\" + matchResult.group())
		);
	}
}
