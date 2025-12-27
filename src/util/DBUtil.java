package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {

    private static final HikariDataSource dataSource;

    static {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        
        // ✅ LOAD FROM PROPERTIES (DECRYPT PASSWORD)
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(decryptPassword(props.getProperty("db.password")));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        // ✅ HIKARICP POOL SETTINGS FROM PROPERTIES
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("pool.max")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("pool.minIdle")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("pool.timeout")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("pool.idleTimeout")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("pool.maxLifetime")));

        dataSource = new HikariDataSource(config);
    }

    /**
     * ✅ LOAD PROPERTIES FROM db.properties
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DBUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("❌ db.properties not found in resources!");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load db.properties", e);
        }
        return props;
    }

    /**
     * ✅ DECRYPT PASSWORD (remove ENC() prefix)
     */
    private static String decryptPassword(String encrypted) {
        if (encrypted == null || !encrypted.startsWith("ENC(")) {
            throw new IllegalArgumentException("❌ Password must be ENC(encrypted)");
        }
        String encryptedValue = encrypted.substring(4, encrypted.length() - 1);
        return EncryptionUtil.decrypt(encryptedValue);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    // ✅ UTILITY: Test connection
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection successful!");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }
}
