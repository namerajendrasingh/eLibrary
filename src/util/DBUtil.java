package util;


import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/elibrary");
        config.setUsername("postgres");
        config.setPassword("public");
        
        // ✅ CORRECT HikariCP Properties (NO housekeeperInterval)
        config.setMaximumPoolSize(200);                    // ✅ Max connections
        config.setMinimumIdle(10);                         // ✅ Min idle connections
        config.setConnectionTimeout(30_000);              // ✅ 30s connect timeout
        config.setIdleTimeout(5 * 60 * 1000);             // ✅ 5min idle timeout
        config.setMaxLifetime(20 * 60 * 1000);            // ✅ 20min max lifetime
        config.setLeakDetectionThreshold(60_000);         // ✅ Detect leaks > 1min
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setDriverClassName("org.postgresql.Driver");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
