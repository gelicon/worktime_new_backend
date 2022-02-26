package biz.gelicon.core.dialect;

import biz.gelicon.core.MainApplication;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DialectFactory {

    public static final String SPRING_DATASOURCE_HIKARI_SCHEMA = "spring.datasource.hikari.schema";

    public enum DatabaseType {
        NONE,
        POSTGRESQL,
        ORACLE,
        SQLSERVER,
        MYSQL,
        FIREBIRD
    }

    static private DBDialect dialect;
    static private DatabaseType dbType = DatabaseType.NONE;

    public static DBDialect getDialect() {
        if(dialect==null) {
            DataSource ds = MainApplication.getApplicationContext().getBean(DataSource.class);
            String url = null;
            try {
                url = ds.getConnection().getMetaData().getURL();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(),ex);
            }
            if(url.startsWith("jdbc:postgresql")) {
                String schema = MainApplication.getApplicationContext().getEnvironment().getProperty(SPRING_DATASOURCE_HIKARI_SCHEMA);
                dialect = new PostgreDialect(schema==null?"dbo":schema);
                dbType = DatabaseType.POSTGRESQL;
            } else
            if(url.startsWith("jdbc:firebirdsql")) {
                dialect = new FireBirdDialect();
                dbType = DatabaseType.FIREBIRD;
            } else {
                throw new RuntimeException(String.format("Unknow database type by uri %s", url));
            }
        }
        return dialect;
    }

    public static DatabaseType getDatabaseType() {
        return dbType;
    }

}
