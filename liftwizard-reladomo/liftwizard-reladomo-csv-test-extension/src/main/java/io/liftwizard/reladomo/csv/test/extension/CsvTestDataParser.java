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

package io.liftwizard.reladomo.csv.test.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.BooleanAttribute;
import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.attribute.DoubleAttribute;
import com.gs.fw.common.mithra.attribute.FloatAttribute;
import com.gs.fw.common.mithra.attribute.IntegerAttribute;
import com.gs.fw.common.mithra.attribute.LongAttribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvTestDataParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsvTestDataParser.class);

	private static final Class<?>[] NO_PARAMS = {};
	private static final Object[] NO_ARGS = {};

	@Nonnull
	private final String filename;

	@Nonnull
	private final String className;

	@Nonnull
	private final List<Attribute<?, ?>> attributes;

	@Nonnull
	private final List<MithraDataObject> dataObjects;

	public CsvTestDataParser(@Nonnull String filename) {
		this.filename = filename;
		this.className = this.extractClassNameFromFilename(filename);
		this.attributes = Lists.mutable.empty();
		this.dataObjects = Lists.mutable.empty();
		this.parse();
	}

	@Nonnull
	private String extractClassNameFromFilename(@Nonnull String filenameParam) {
		String baseFilename = filenameParam;
		if (baseFilename.contains("/")) {
			baseFilename = baseFilename.substring(baseFilename.lastIndexOf('/') + 1);
		}
		if (!baseFilename.endsWith(".csv")) {
			throw new IllegalArgumentException("Filename must end with .csv: " + this.filename);
		}
		return baseFilename.substring(0, baseFilename.length() - 4);
	}

	private void parse() {
		LOGGER.debug("Parsing CSV file: {}", this.filename);

		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(this.filename)) {
			if (inputStream == null) {
				throw new IllegalArgumentException("Could not find file: " + this.filename);
			}

			try (
				Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(
					reader,
					CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build()
				)
			) {
				this.parseHeaders(csvParser.getHeaderNames());
				this.parseDataRows(csvParser);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading CSV file: " + this.filename, e);
		}
	}

	private void parseHeaders(@Nonnull List<String> headerNames) {
		try {
			String finderClassName = this.className + "Finder";
			Class<?> finderClass = Class.forName(finderClassName);

			for (String headerName : headerNames) {
				String getterMethodName = headerName;
				Method getterMethod = finderClass.getMethod(getterMethodName, NO_PARAMS);
				Attribute<?, ?> attribute = (Attribute<?, ?>) getterMethod.invoke(null, NO_ARGS);
				this.attributes.add(attribute);
			}
		} catch (
			ClassNotFoundException
			| NoSuchMethodException
			| IllegalAccessException
			| InvocationTargetException e
		) {
			throw new RuntimeException("Error finding attributes for class: " + this.className, e);
		}
	}

	private void parseDataRows(@Nonnull CSVParser csvParser) {
		try {
			String dataClassName = this.className + "Data";
			Class<?> dataClass = Class.forName(dataClassName);

			for (CSVRecord record : csvParser) {
				MithraDataObject dataObject = (MithraDataObject) dataClass.getDeclaredConstructor().newInstance();
				this.populateDataObject(dataObject, record);
				this.dataObjects.add(dataObject);
			}
		} catch (
			ClassNotFoundException
			| NoSuchMethodException
			| IllegalAccessException
			| InvocationTargetException
			| InstantiationException e
		) {
			throw new RuntimeException("Error creating data objects for class: " + this.className, e);
		}
	}

	private void populateDataObject(@Nonnull MithraDataObject dataObject, @Nonnull CSVRecord record) {
		for (int i = 0; i < this.attributes.size(); i++) {
			Attribute<?, ?> attribute = this.attributes.get(i);
			String value = record.get(i);

			if (value == null || value.isEmpty()) {
				this.setNullValue(dataObject, attribute);
			} else {
				this.setValue(dataObject, attribute, value);
			}
		}
	}

	private void setNullValue(@Nonnull MithraDataObject dataObject, @Nonnull Attribute<?, ?> attribute) {
		if (attribute instanceof TimestampAttribute timestampAttribute) {
			timestampAttribute.setTimestampValue(dataObject, null);
		} else if (attribute instanceof DateAttribute dateAttribute) {
			dateAttribute.setDateValue(dataObject, null);
		} else if (attribute instanceof StringAttribute stringAttribute) {
			stringAttribute.setStringValue(dataObject, null);
		} else if (attribute instanceof IntegerAttribute integerAttribute) {
			integerAttribute.setValueNull(dataObject);
		} else if (attribute instanceof LongAttribute longAttribute) {
			longAttribute.setValueNull(dataObject);
		} else if (attribute instanceof DoubleAttribute doubleAttribute) {
			doubleAttribute.setValueNull(dataObject);
		} else if (attribute instanceof FloatAttribute floatAttribute) {
			floatAttribute.setValueNull(dataObject);
		} else if (attribute instanceof BooleanAttribute booleanAttribute) {
			booleanAttribute.setValueNull(dataObject);
		} else {
			throw new UnsupportedOperationException("Unsupported attribute type: " + attribute.getClass().getName());
		}
	}

	private void setValue(
		@Nonnull MithraDataObject dataObject,
		@Nonnull Attribute<?, ?> attribute,
		@Nonnull String value
	) {
		if (attribute instanceof TimestampAttribute timestampAttribute) {
			Instant instant = Instant.parse(value);
			Timestamp timestamp = Timestamp.from(instant);
			timestampAttribute.setTimestampValue(dataObject, timestamp);
		} else if (attribute instanceof DateAttribute dateAttribute) {
			LocalDate localDate = LocalDate.parse(value);
			Date date = Date.valueOf(localDate);
			dateAttribute.setDateValue(dataObject, date);
		} else if (attribute instanceof StringAttribute stringAttribute) {
			stringAttribute.setStringValue(dataObject, value);
		} else if (attribute instanceof IntegerAttribute integerAttribute) {
			integerAttribute.setIntValue(dataObject, Integer.parseInt(value));
		} else if (attribute instanceof LongAttribute longAttribute) {
			longAttribute.setLongValue(dataObject, Long.parseLong(value));
		} else if (attribute instanceof DoubleAttribute doubleAttribute) {
			doubleAttribute.setDoubleValue(dataObject, Double.parseDouble(value));
		} else if (attribute instanceof FloatAttribute floatAttribute) {
			floatAttribute.setFloatValue(dataObject, Float.parseFloat(value));
		} else if (attribute instanceof BooleanAttribute booleanAttribute) {
			booleanAttribute.setBooleanValue(dataObject, Boolean.parseBoolean(value));
		} else {
			throw new UnsupportedOperationException("Unsupported attribute type: " + attribute.getClass().getName());
		}
	}

	@Nonnull
	public String getClassName() {
		return this.className;
	}

	@Nonnull
	public List<Attribute<?, ?>> getAttributes() {
		return this.attributes;
	}

	@Nonnull
	public List<MithraDataObject> getDataObjects() {
		return this.dataObjects;
	}
}
