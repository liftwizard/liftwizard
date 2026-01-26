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

package io.liftwizard.reladomo.rollback;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dropwizard CLI command to roll back all bitemporal Reladomo tables to a specified point in time.
 *
 * <p>Usage:
 * <pre>
 * java -jar app.jar rollback-temporal config.yml --date=2026-01-18T00:00:00Z
 * </pre>
 *
 * <p>This command extends {@link EnvironmentCommand} which initializes all bundles (including
 * Reladomo bundles) before executing, ensuring the MithraManager is properly configured.
 *
 * <p>This enables developers to undo temporal history for testing re-imports or recovering from bad data.
 *
 * @param <T> the configuration type
 */
public class ReladomoRollbackCommand<T extends Configuration> extends EnvironmentCommand<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoRollbackCommand.class);

	private final Application<T> application;

	public ReladomoRollbackCommand(Application<T> application) {
		super(application, "rollback-temporal", "Roll back all bitemporal tables to a specified point in time");
		this.application = application;
	}

	@Override
	protected Class<T> getConfigurationClass() {
		return this.application.getConfigurationClass();
	}

	@Override
	public void configure(Subparser subparser) {
		super.configure(subparser);
		subparser
			.addArgument("--date")
			.dest("date")
			.required(true)
			.help("The target date/time to roll back to in ISO-8601 format (e.g., 2026-01-18T00:00:00Z)");
	}

	@Override
	protected void run(Environment environment, Namespace namespace, T configuration) throws Exception {
		String dateString = namespace.getString("date");
		Instant targetDate = this.parseDate(dateString);

		LOGGER.info("Starting temporal rollback to: {}", targetDate);

		ReladomoTemporalRollback rollback = new ReladomoTemporalRollback(targetDate);
		rollback.rollbackAllTables();

		LOGGER.info("Temporal rollback completed successfully");
	}

	private Instant parseDate(String dateString) {
		try {
			return Instant.parse(dateString);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(
				"Invalid date format: " + dateString + ". Expected ISO-8601 format (e.g., 2026-01-18T00:00:00Z)",
				e
			);
		}
	}
}
