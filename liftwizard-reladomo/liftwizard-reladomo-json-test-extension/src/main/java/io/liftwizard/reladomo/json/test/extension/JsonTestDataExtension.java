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

package io.liftwizard.reladomo.json.test.extension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.MithraDatabaseObject;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTestDataExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTestDataExtension.class);

	private static final Class<?>[] NO_PARAMS = {};
	private static final Object[] NO_ARGS = {};

	@Nonnull
	private final ImmutableList<String> jsonFileNames;

	public JsonTestDataExtension(@Nonnull String... jsonFileNames) {
		this(Lists.immutable.with(jsonFileNames));
	}

	public JsonTestDataExtension(@Nonnull ImmutableList<String> jsonFileNames) {
		this.jsonFileNames = jsonFileNames;
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		for (String jsonFileName : this.jsonFileNames) {
			try {
				this.loadJsonTestData(jsonFileName);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Error while loading JSON test data file: " + jsonFileName, e);
			}
		}
	}

	private void loadJsonTestData(String jsonFileName) throws ReflectiveOperationException {
		LOGGER.debug("Loading JSON test data from file: {}", jsonFileName);

		var parser = new JsonTestDataParser(jsonFileName);
		String className = parser.getClassName();
		List<MithraDataObject> dataObjects = parser.getDataObjects();

		if (!MithraManagerProvider.getMithraManager().getConfigManager().isClassConfigured(className)) {
			throw new RuntimeException(
				"Class "
				+ className
				+ " is not configured. Did you remember to initialize Reladomo with a runtime configuration?"
			);
		}

		String finderClassName = className + "Finder";
		Class<?> finderClass = Class.forName(finderClassName);

		Method allPersistentAttributesMethod = finderClass.getMethod("allPersistentAttributes", NO_PARAMS);
		var attributeArray = (Attribute<?, ?>[]) allPersistentAttributesMethod.invoke(null, NO_ARGS);
		List<Attribute<?, ?>> attributes = Arrays.asList(attributeArray);

		Method method = finderClass.getMethod("getMithraObjectPortal", NO_PARAMS);
		var mithraObjectPortal = (MithraObjectPortal) method.invoke(null, NO_ARGS);

		MithraDatabaseObject databaseObject = mithraObjectPortal.getDatabaseObject();
		var databaseObjectConnectionManager = (SourcelessConnectionManager) databaseObject.getConnectionManager();

		LOGGER.debug(
			"Loading JSON test data for class {} using connection manager: {}",
			className,
			databaseObjectConnectionManager
		);

		Class<? extends MithraDatabaseObject> databaseObjectClass = databaseObject.getClass();

		Method insertDataMethod = databaseObjectClass.getMethod("insertData", List.class, List.class, Object.class);
		insertDataMethod.invoke(databaseObject, attributes, dataObjects, null);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		MithraManagerProvider.getMithraManager().clearAllQueryCaches();
		MithraManagerProvider.getMithraManager().cleanUpPrimaryKeyGenerators();
		MithraManagerProvider.getMithraManager().cleanUpRuntimeCacheControllers();
		MithraManagerProvider.getMithraManager().getConfigManager().resetAllInitializedClasses();
	}
}
