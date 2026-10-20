package com.example.helloworld.reladomo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Reads and writes the PERSON table with plain JDBC, bypassing Reladomo's time zone conversions.
 *
 * <p>Timestamps are exchanged as {@link LocalDateTime} so that the values are the literal wall-clock values stored in
 * the database, regardless of the JVM default time zone.</p>
 */
final class PersonTable
{
	static final LocalDateTime INFINITY_UTC_WALL_CLOCK = LocalDateTime.of(9999, 12, 1, 23, 59, 0);

	private PersonTable()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	static ImmutableList<PersonRow> selectAll()
		throws SQLException
	{
		try (
			Connection connection = H2InMemoryConnectionManager.getInstance().getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(
				"SELECT id, full_name, system_from, system_to FROM PERSON ORDER BY id, system_from"
			)
		)
		{
			MutableList<PersonRow> result = Lists.mutable.empty();
			while (resultSet.next())
			{
				result.add(
					new PersonRow(
						resultSet.getLong(1),
						resultSet.getString(2),
						resultSet.getObject(3, LocalDateTime.class),
						resultSet.getObject(4, LocalDateTime.class)
					)
				);
			}
			return result.toImmutable();
		}
	}

	static void insert(PersonRow personRow)
		throws SQLException
	{
		try (
			Connection connection = H2InMemoryConnectionManager.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO PERSON (id, full_name, job_title, system_from, system_to) VALUES (?, ?, 'Job Title', ?, ?)"
			)
		)
		{
			statement.setLong(1, personRow.id());
			statement.setString(2, personRow.fullName());
			statement.setObject(3, personRow.systemFrom());
			statement.setObject(4, personRow.systemTo());
			statement.executeUpdate();
		}
	}

	record PersonRow(long id, String fullName, LocalDateTime systemFrom, LocalDateTime systemTo)
	{
	}
}
