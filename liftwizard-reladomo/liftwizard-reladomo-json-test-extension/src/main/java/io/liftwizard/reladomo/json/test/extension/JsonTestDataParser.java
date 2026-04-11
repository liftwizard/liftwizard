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
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gs.fw.common.mithra.MithraDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTestDataParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTestDataParser.class);

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
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException("Error reading JSON file: " + this.filename, e);
		}
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
