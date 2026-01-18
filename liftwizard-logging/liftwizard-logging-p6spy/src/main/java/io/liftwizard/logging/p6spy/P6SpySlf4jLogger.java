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

package io.liftwizard.logging.p6spy;

import com.google.common.collect.ImmutableList;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.FormattedLogger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P6SpySlf4jLogger extends FormattedLogger {

	private static final Logger LOG = LoggerFactory.getLogger(P6SpySlf4jLogger.class);

	@Override
	public void logException(Exception e) {
		LOG.warn("", e);
	}

	@Override
	public void logText(String text) {
		LOG.debug(text);
	}

	@Override
	public void logSQL(
		int connectionId,
		String now,
		long elapsed,
		Category category,
		String prepared,
		String sql,
		String url
	) {
		Map<String, Object> structuredArgumentsMap = new LinkedHashMap<>();
		structuredArgumentsMap.put("liftwizard.p6spy.connectionId", connectionId);
		structuredArgumentsMap.put("liftwizard.p6spy.elapsedMillis", elapsed);
		structuredArgumentsMap.put("liftwizard.p6spy.elapsedNanos", elapsed * 1_000_000);
		structuredArgumentsMap.put("liftwizard.p6spy.category", category.getName());
		structuredArgumentsMap.put("liftwizard.p6spy.prepared", prepared);
		structuredArgumentsMap.put("liftwizard.p6spy.sql", sql);
		structuredArgumentsMap.put("liftwizard.p6spy.url", url);

		if (category.equals(Category.ERROR)) {
			LOG.error(sql, StructuredArguments.entries(structuredArgumentsMap));
		} else if (category.equals(Category.WARN)) {
			LOG.warn(sql, StructuredArguments.entries(structuredArgumentsMap));
		} else if (category.equals(Category.INFO)) {
			LOG.info(sql, StructuredArguments.entries(structuredArgumentsMap));
		} else if (category.equals(Category.DEBUG)) {
			LOG.debug(sql, StructuredArguments.entries(structuredArgumentsMap));
		} else if (ImmutableList.of(Category.ROLLBACK, Category.COMMIT).contains(category)) {
			if (!sql.isBlank()) {
				throw new AssertionError("Unexpected SQL for category " + category + ": " + sql);
			}
			LOG.debug(category.toString(), StructuredArguments.entries(structuredArgumentsMap));
		} else {
			LOG.debug(sql, StructuredArguments.entries(structuredArgumentsMap));
		}
	}

	@Override
	public boolean isCategoryEnabled(Category category) {
		if (category.equals(Category.ERROR)) {
			return LOG.isErrorEnabled();
		}

		if (category.equals(Category.WARN)) {
			return LOG.isWarnEnabled();
		}

		if (category.equals(Category.DEBUG)) {
			return LOG.isDebugEnabled();
		}

		return LOG.isInfoEnabled();
	}
}
