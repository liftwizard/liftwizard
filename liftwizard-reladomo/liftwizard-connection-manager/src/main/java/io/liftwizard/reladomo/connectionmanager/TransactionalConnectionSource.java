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

package io.liftwizard.reladomo.connectionmanager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraTransaction;
import com.gs.fw.common.mithra.connectionmanager.PostTransactionAction;
import com.gs.fw.common.mithra.connectionmanager.PostTransactionExecutor;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.transaction.LocalTm;
import com.gs.fw.common.mithra.transaction.TransactionLocal;
import com.gs.fw.common.mithra.util.WrappedConnection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hands out connections from a plain {@link DataSource} that take part in the current Reladomo transaction.
 *
 * <p>Outside a transaction, and inside one that has not written yet, connections come straight from the data source.
 * Once a Reladomo transaction writes, its connections are borrowed with auto-commit off, enlisted in the transaction
 * through an {@link XAResource}, and committed or rolled back together when the transaction ends. Each connection
 * gets its original auto-commit and isolation settings back before it returns to the data source.
 *
 * <p>This mirrors what Reladomo's {@code XAConnectionPoolingDataSource} does for its own connection pool.
 */
public final class TransactionalConnectionSource
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalConnectionSource.class);

	private final TransactionLocal transactionResources = new TransactionLocal();
	private final DataSource dataSource;
	private final DatabaseType databaseType;

	public TransactionalConnectionSource(DataSource dataSource, DatabaseType databaseType)
	{
		this.dataSource = Objects.requireNonNull(dataSource);
		this.databaseType = Objects.requireNonNull(databaseType);
	}

	public Connection getConnection()
		throws SQLException
	{
		MithraTransaction transaction = MithraManagerProvider.getMithraManager().getCurrentTransaction();
		if (transaction == null)
		{
			return this.dataSource.getConnection();
		}

		var resource = (TransactionResource) this.transactionResources.get(transaction);
		if (resource == null)
		{
			if (!transaction.isWriteOperationMode())
			{
				return this.dataSource.getConnection();
			}
			resource = this.enlistNewResource(transaction);
		}
		return resource.getConnection();
	}

	private TransactionResource enlistNewResource(MithraTransaction transaction)
	{
		var resource = new TransactionResource(transaction);
		try
		{
			transaction.enlistResource(resource);
		}
		catch (SystemException | RollbackException e)
		{
			throw new RuntimeException(
				"Unable to enlist connection resource in transaction " + transaction.getTransactionName(),
				e
			);
		}
		this.transactionResources.set(transaction, resource);
		return resource;
	}

	private final class TransactionResource
		implements XAResource, LocalTm.SinglePhaseResource
	{
		private final MithraTransaction ownerTransaction;
		private final MutableList<TransactionConnection> availableConnections = Lists.mutable.empty();
		private final MutableList<TransactionConnection> connectionsInUse = Lists.mutable.empty();
		private boolean committed;
		private boolean rolledBack;

		private TransactionResource(MithraTransaction ownerTransaction)
		{
			this.ownerTransaction = ownerTransaction;
		}

		private Connection getConnection()
			throws SQLException
		{
			TransactionConnection connection = this.availableConnections.isEmpty() ?
				this.borrowConnection() :
				this.availableConnections.removeLast();
			this.connectionsInUse.add(connection);
			return connection;
		}

		private TransactionConnection borrowConnection()
			throws SQLException
		{
			Connection connection = TransactionalConnectionSource.this.dataSource.getConnection();
			try
			{
				var transactionConnection = new TransactionConnection(
					connection,
					this,
					connection.getAutoCommit(),
					connection.getTransactionIsolation()
				);
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(TransactionalConnectionSource.this.databaseType.zGetTxLevel());
				return transactionConnection;
			}
			catch (SQLException e)
			{
				connection.close();
				throw e;
			}
		}

		private void release(TransactionConnection connection)
		{
			if (!this.availableConnections.contains(connection))
			{
				this.availableConnections.add(connection);
			}
			this.connectionsInUse.remove(connection);
		}

		@Override
		public void commit(Xid xid, boolean onePhase)
			throws XAException
		{
			if (this.committed)
			{
				throw new XAException("Transaction is already committed");
			}
			if (this.connectionsInUse.notEmpty())
			{
				throw new XAException(
					"Detected " +
						this.connectionsInUse.size() +
						" open jdbc connections. " +
						"Close all connections borrowed from the connection manager before the transaction ends."
				);
			}
			try
			{
				for (TransactionConnection connection : this.availableConnections)
				{
					connection.commitAndRelease();
				}
				this.committed = true;
			}
			catch (SQLException e)
			{
				LOGGER.error("Commit failed", e);
				this.rollbackConnections();
				var xaException = new XAException("Commit failed - " + e.getMessage());
				xaException.initCause(e);
				throw xaException;
			}
		}

		@Override
		public void rollback(Xid xid)
			throws XAException
		{
			if (this.committed)
			{
				throw new XAException("Transaction is already committed");
			}
			if (this.rolledBack)
			{
				return;
			}
			if (!this.rollbackConnections())
			{
				throw new XAException("Rollback failed");
			}
		}

		private boolean rollbackConnections()
		{
			var success = true;
			for (TransactionConnection connection : Lists.mutable
				.withAll(this.availableConnections)
				.withAll(this.connectionsInUse))
			{
				try
				{
					connection.rollbackAndRelease();
				}
				catch (SQLException e)
				{
					LOGGER.error("Rollback failed", e);
					success = false;
				}
			}
			this.availableConnections.clear();
			this.connectionsInUse.clear();
			this.rolledBack = true;
			return success;
		}

		@Override
		public void end(Xid xid, int flags)
		{
			TransactionalConnectionSource.this.transactionResources.remove(this.ownerTransaction);
		}

		@Override
		public void start(Xid xid, int flags)
		{
		}

		@Override
		public int prepare(Xid xid)
		{
			return XA_OK;
		}

		@Override
		public void forget(Xid xid)
		{
		}

		@Override
		public Xid[] recover(int flag)
		{
			return new Xid[0];
		}

		@Override
		public boolean isSameRM(XAResource xaResource)
		{
			return this == xaResource;
		}

		@Override
		public int getTransactionTimeout()
		{
			return -1;
		}

		@Override
		public boolean setTransactionTimeout(int seconds)
		{
			return false;
		}
	}

	private static final class TransactionConnection
		extends WrappedConnection
		implements PostTransactionExecutor
	{
		private final TransactionResource resource;
		private final boolean originalAutoCommit;
		private final int originalTransactionIsolation;
		private final MutableList<PostTransactionAction> postTransactionActions = Lists.mutable.empty();

		private TransactionConnection(
			Connection connection,
			TransactionResource resource,
			boolean originalAutoCommit,
			int originalTransactionIsolation
		)
		{
			super(connection);
			this.resource = resource;
			this.originalAutoCommit = originalAutoCommit;
			this.originalTransactionIsolation = originalTransactionIsolation;
		}

		@Override
		public void addPostTransactionAction(PostTransactionAction action)
		{
			this.postTransactionActions.add(action);
		}

		@Override
		public void close()
		{
			this.resource.release(this);
		}

		@Override
		public void commit()
			throws SQLException
		{
			throw new SQLException("Commit on connection not allowed. Commit the transaction instead.");
		}

		@Override
		public void rollback()
			throws SQLException
		{
			throw new SQLException("Rollback on connection not allowed. Roll back the transaction instead.");
		}

		private void commitAndRelease()
			throws SQLException
		{
			this.getUnderlyingConnection().commit();
			this.releaseToDataSource();
		}

		private void rollbackAndRelease()
			throws SQLException
		{
			Connection connection = this.getUnderlyingConnection();
			if (connection.isClosed())
			{
				return;
			}
			try
			{
				connection.rollback();
			}
			finally
			{
				this.releaseToDataSource();
			}
		}

		private void releaseToDataSource()
			throws SQLException
		{
			for (PostTransactionAction action : this.postTransactionActions)
			{
				try
				{
					action.execute(this);
				}
				catch (RuntimeException e)
				{
					LOGGER.error("Ignoring post transaction error", e);
				}
			}

			Connection connection = this.getUnderlyingConnection();
			try
			{
				connection.setTransactionIsolation(this.originalTransactionIsolation);
				connection.setAutoCommit(this.originalAutoCommit);
			}
			finally
			{
				connection.close();
			}
		}
	}
}
