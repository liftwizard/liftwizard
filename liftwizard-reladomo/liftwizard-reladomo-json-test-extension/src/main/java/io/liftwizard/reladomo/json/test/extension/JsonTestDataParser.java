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

package io.liftwizard.reladomo.json.test.extension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTestDataParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTestDataParser.class);
	private static final Class<?>[] NO_PARAMS = {};

	@Nonnull
	private final String filename;

	@Nonnull
	private final String className;

	@Nonnull
	private List<MithraDataObject> dataObjects = List.of();

	public JsonTestDataParser(@Nonnull String filename) {
		this.filename = filename;
		this.className = this.extractClassNameFromFilename(filename);
		this.parse();
	}

	@Nonnull
	private String extractClassNameFromFilename(@Nonnull String filenameParam) {
		String baseFilename = filenameParam;
		if (baseFilename.contains("/")) {
			baseFilename = baseFilename.substring(baseFilename.lastIndexOf('/') + 1);
		}
		if (!baseFilename.endsWith(".json")) {
			throw new IllegalArgumentException("Filename must end with .json: " + this.filename);
		}
		return baseFilename.substring(0, baseFilename.length() - 5);
	}

	private void parse() {
		LOGGER.debug("Parsing JSON file: {}", this.filename);

		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(this.filename)) {
			if (inputStream == null) {
				throw new IllegalArgumentException("Could not find file: " + this.filename);
			}

			var objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());

			JsonNode rootNode = objectMapper.readTree(inputStream);

			if (!(rootNode instanceof ArrayNode arrayNode)) {
				throw new IllegalArgumentException(
					"Expected a JSON array but found " + rootNode.getNodeType() + " in file: " + this.filename
				);
			}

			if (arrayNode.isEmpty()) {
				return;
			}

			String dataClassName = this.className + "Data";
			Class<?> dataClass = Class.forName(dataClassName);
			this.dataObjects = objectMapper.readerForListOf(dataClass).readValue(arrayNode);
			this.convertTimestampsFromUtc(arrayNode);
		} catch (IOException | ReflectiveOperationException e) {
			throw new RuntimeException("Error reading JSON file: " + this.filename, e);
		}
	}

	private void convertTimestampsFromUtc(ArrayNode arrayNode)
		throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<?> finderClass = Class.forName(this.className + "Finder");
		for (var rowIndex = 0; rowIndex < arrayNode.size(); rowIndex++) {
			JsonNode row = arrayNode.get(rowIndex);
			MithraDataObject dataObject = this.dataObjects.get(rowIndex);
			var fieldNames = row.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				Method attributeMethod = finderClass.getMethod(fieldName, NO_PARAMS);
				Object attribute = attributeMethod.invoke(null);
				if (attribute instanceof TimestampAttribute timestampAttribute) {
					Timestamp timestamp = timestampAttribute.valueOf(dataObject);
					timestampAttribute.setTimestampValue(
						dataObject,
						convertTimestampFromUtc(timestampAttribute, timestamp)
					);
				}
			}
		}
	}

	private static Timestamp convertTimestampFromUtc(TimestampAttribute<?> timestampAttribute, Timestamp timestamp) {
		if (timestamp == null) {
			return null;
		}
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

	@Nonnull
	public String getClassName() {
		return this.className;
	}

	@Nonnull
	public List<MithraDataObject> getDataObjects() {
		return this.dataObjects;
	}
}
