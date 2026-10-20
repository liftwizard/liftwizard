package com.example.helloworld.reladomo;

import java.sql.Connection;
import java.util.TimeZone;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;
import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;

/**
 * Uses the same database as {@link H2InMemoryConnectionManager}, but with Reladomo's own {@link H2DatabaseType}
 * instead of {@code UtcInfinityH2DatabaseType}.
 */
public final class H2DatabaseTypeConnectionManager
	implements SourcelessConnectionManager
{
	private static final H2DatabaseTypeConnectionManager INSTANCE = new H2DatabaseTypeConnectionManager();

	private H2DatabaseTypeConnectionManager()
	{
		// singleton
	}

	@SuppressWarnings("unused")
	public static H2DatabaseTypeConnectionManager getInstance()
	{
		return INSTANCE;
	}

	@Override
	public BulkLoader createBulkLoader()
	{
		return H2InMemoryConnectionManager.getInstance().createBulkLoader();
	}

	@Override
	public Connection getConnection()
	{
		return H2InMemoryConnectionManager.getInstance().getConnection();
	}

	@Override
	public DatabaseType getDatabaseType()
	{
		return H2DatabaseType.getInstance();
	}

	@Override
	public TimeZone getDatabaseTimeZone()
	{
		return H2InMemoryConnectionManager.getInstance().getDatabaseTimeZone();
	}

	@Override
	public String getDatabaseIdentifier()
	{
		return H2InMemoryConnectionManager.getInstance().getDatabaseIdentifier();
	}
}
