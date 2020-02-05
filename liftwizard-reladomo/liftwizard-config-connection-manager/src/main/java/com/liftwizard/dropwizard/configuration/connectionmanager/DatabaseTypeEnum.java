package com.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.Objects;

import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.DerbyDatabaseType;
import com.gs.fw.common.mithra.databasetype.GenericDatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;
import com.gs.fw.common.mithra.databasetype.MariaDatabaseType;
import com.gs.fw.common.mithra.databasetype.MsSqlDatabaseType;
import com.gs.fw.common.mithra.databasetype.OracleDatabaseType;
import com.gs.fw.common.mithra.databasetype.PostgresDatabaseType;
import com.gs.fw.common.mithra.databasetype.SybaseDatabaseType;
import com.gs.fw.common.mithra.databasetype.SybaseIqDatabaseType;
import com.gs.fw.common.mithra.databasetype.Udb82DatabaseType;

public enum DatabaseTypeEnum
{
    DERBY(DerbyDatabaseType.getInstance()),
    GENERIC(GenericDatabaseType.getInstance()),
    H2(H2DatabaseType.getInstance()),
    MARIA(MariaDatabaseType.getInstance()),
    MSSQL(MsSqlDatabaseType.getInstance()),
    ORACLE(OracleDatabaseType.getInstance()),
    POSTGRES(PostgresDatabaseType.getInstance()),
    SYBASE(SybaseDatabaseType.getInstance()),
    SYBASE_IQ(SybaseIqDatabaseType.getInstance()),
    SYBASE_IQ_NATIVE(SybaseIqDatabaseType.getInstance()),
    UDB_82(Udb82DatabaseType.getInstance()),;

    private final DatabaseType databaseType;

    DatabaseTypeEnum(DatabaseType databaseType)
    {
        this.databaseType = Objects.requireNonNull(databaseType);
    }

    public DatabaseType getDatabaseType()
    {
        return this.databaseType;
    }
}
