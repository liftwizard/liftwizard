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

package io.liftwizard.reladomo.test.data.parser;

import java.io.InputStream;
import java.net.URL;

import com.gs.fw.common.mithra.test.MithraTestDataParser;

/**
 * A UTC-aware version of Reladomo's {@link MithraTestDataParser}.
 *
 * <p>This parser uses {@link UtcMithraParsedData} to ensure timestamps in test data files
 * are always parsed in UTC, regardless of the JVM's default timezone.</p>
 *
 * @see MithraTestDataParser
 * @see UtcMithraParsedData
 */
public class UtcMithraTestDataParser extends MithraTestDataParser {

	public UtcMithraTestDataParser(String filename) {
		super(filename);
	}

	public UtcMithraTestDataParser(URL streamLocation, InputStream is) {
		super(streamLocation, is);
	}

	@Override
	protected void addNewMithraParsedData() {
		UtcMithraParsedData utcParsedData = new UtcMithraParsedData();
		this.setCurrentParsedData(utcParsedData);
		this.getResults().add(utcParsedData);
	}
}
