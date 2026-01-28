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

package io.liftwizard.junit.extension.liquibase.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.Scope.Attr;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.ui.LoggerUIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for verifying that all Liquibase changesets can be applied, rolled back,
 * and reapplied successfully. This helps ensure that all migrations have proper rollback
 * support and that the rollback scripts actually work.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Test
 * void testRollbacks() {
 *     LiquibaseRollbackVerifier.verifyAllChangesets("migrations.xml");
 * }
 * }</pre>
 */
public final class LiquibaseRollbackVerifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseRollbackVerifier.class);

	private LiquibaseRollbackVerifier() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * Verifies all changesets in the specified migrations file by:
	 * <ol>
	 *   <li>Iterating through each changeset</li>
	 *   <li>Applying the changeset</li>
	 *   <li>Rolling back the changeset</li>
	 *   <li>Re-applying the changeset</li>
	 * </ol>
	 *
	 * <p>This ensures that:
	 * <ul>
	 *   <li>Each changeset can be applied successfully</li>
	 *   <li>Each changeset has a working rollback</li>
	 *   <li>The changeset can be reapplied after rollback (verifying rollback completeness)</li>
	 * </ul>
	 *
	 * @param migrationsFile the path to the Liquibase migrations XML file (e.g., "migrations.xml")
	 * @throws RuntimeException if any changeset fails to apply, rollback, or reapply
	 */
	public static void verifyAllChangesets(String migrationsFile) {
		Objects.requireNonNull(migrationsFile, "migrationsFile is required");

		try {
			Scope.child(Attr.ui, new LoggerUIService(), () -> runVerification(migrationsFile));
		} catch (Exception e) {
			throw new RuntimeException("Failed to verify changesets in " + migrationsFile, e);
		}
	}

	private static void runVerification(String migrationsFile) throws SQLException, LiquibaseException {
		try (Connection connection = H2InMemoryConnectionManager.getInstance().getConnection()) {
			Database database = createDatabase(connection);

			try (Liquibase liquibase = createLiquibase(migrationsFile, database)) {
				liquibase.dropAll();

				DatabaseChangeLog databaseChangeLog = liquibase.getDatabaseChangeLog();
				List<ChangeSet> changeSets = databaseChangeLog.getChangeSets();

				LOGGER.info("Verifying {} changesets from {}", changeSets.size(), migrationsFile);

				for (int i = 0; i < changeSets.size(); i++) {
					ChangeSet changeSet = changeSets.get(i);
					String changeSetId = changeSet.getId();
					String author = changeSet.getAuthor();

					LOGGER.info("Verifying changeset {}/{}: {}::{}", i + 1, changeSets.size(), author, changeSetId);

					verifyChangeset(liquibase, changeSet);
				}

				LOGGER.info("Successfully verified all {} changesets", changeSets.size());
			}
		}
	}

	private static void verifyChangeset(Liquibase liquibase, ChangeSet changeSet) throws LiquibaseException {
		String changeSetId = changeSet.getId();
		String author = changeSet.getAuthor();

		LOGGER.debug("Applying changeset {}::{}", author, changeSetId);
		liquibase.update(1, "");

		LOGGER.debug("Rolling back changeset {}::{}", author, changeSetId);
		liquibase.rollback(1, "");

		LOGGER.debug("Re-applying changeset {}::{}", author, changeSetId);
		liquibase.update(1, "");
	}

	private static Liquibase createLiquibase(String migrationsFile, Database database) throws LiquibaseException {
		var liquibase = new Liquibase(migrationsFile, new ClassLoaderResourceAccessor(), database);
		liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.LOG);
		return liquibase;
	}

	private static Database createDatabase(Connection connection) throws LiquibaseException {
		DatabaseConnection jdbcConnection = new JdbcConnection(connection);
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
		database.supportsCatalogs();
		database.supportsSchemas();
		return database;
	}
}
