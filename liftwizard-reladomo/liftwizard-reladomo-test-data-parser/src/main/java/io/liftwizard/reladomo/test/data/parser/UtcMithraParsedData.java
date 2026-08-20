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

package io.liftwizard.reladomo.test.data.parser;

import java.io.StreamTokenizer;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.NumericAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.util.fileparser.MithraParsedData;

/**
 * A UTC-aware version of Reladomo's {@link MithraParsedData}.
 *
 * <p>The standard {@code MithraParsedData} parses timestamps using the JVM's default timezone.
 * This class overrides {@link #parseData(StreamTokenizer, int, Object)} to parse timestamp
 * values in UTC. Reladomo as-of infinity values are the exception because they are
 * JVM-local wall-clock sentinels.</p>
 *
 * @see MithraParsedData
 */
public class UtcMithraParsedData extends MithraParsedData {

	protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private final SimpleDateFormat utcDateFormat;

	public UtcMithraParsedData() {
		this.utcDateFormat = new SimpleDateFormat(DATE_FORMAT);
		this.utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void parseData(StreamTokenizer st, int attributeNumber, Object currentData) throws ParseException {
		List<Attribute> attributes = this.getAttributes();
		if (attributeNumber >= attributes.size()) {
			throw new ParseException("extra data on line " + st.lineno(), st.lineno());
		}
		Attribute attribute = attributes.get(attributeNumber);
		int token = st.ttype;
		switch (token) {
			case StreamTokenizer.TT_NUMBER -> attribute.parseNumberAndSet(st.nval, currentData, st.lineno());
			case StreamTokenizer.TT_WORD -> {
				String word = st.sval;
				if (!"null".equals(word) && attribute instanceof NumericAttribute) {
					attribute.parseStringAndSet(st.sval, currentData, st.lineno(), null);
				} else {
					attribute.parseWordAndSet(word, currentData, st.lineno());
				}
			}
			case '"' -> this.parseStringAndSet(attribute, st.sval, currentData, st.lineno());
			case StreamTokenizer.TT_EOL -> throw new RuntimeException("should never get here");
			case StreamTokenizer.TT_EOF -> throw new ParseException("Unexpected end of file", st.lineno());
			default -> {
				var ch = (char) st.ttype;
				throw new ParseException(
					"unexpected character "
					+ ch
					+ " or type "
					+ st.ttype
					+ " on line "
					+ st.lineno()
					+ " attribute count "
					+ attributeNumber,
					st.lineno()
				);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void parseStringAndSet(Attribute attribute, String value, Object currentData, int lineNumber)
		throws ParseException {
		if (attribute instanceof TimestampAttribute timestampAttribute) {
			Timestamp timestamp = this.parseTimestamp(timestampAttribute, value);
			timestampAttribute.setTimestampValue(currentData, timestamp);
			return;
		}
		attribute.parseStringAndSet(value, currentData, lineNumber, this.utcDateFormat);
	}

	private Timestamp parseTimestamp(TimestampAttribute<?> timestampAttribute, String value) throws ParseException {
		var timestamp = new Timestamp(this.utcDateFormat.parse(value).getTime());
		LocalDateTime utcDateTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
		if (
			timestampAttribute.isAsOfAttributeTo()
			&& (timestamp.equals(timestampAttribute.getAsOfAttributeInfinity())
				|| utcDateTime.equals(timestampAttribute.getAsOfAttributeInfinity().toLocalDateTime()))
		) {
			return timestampAttribute.getAsOfAttributeInfinity();
		}
		return timestampAttribute.requiresConversionFromUtc() ? Timestamp.valueOf(utcDateTime) : timestamp;
	}
}
