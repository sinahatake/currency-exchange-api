package org.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static final HikariDataSource ds;

    static {
        try {
            String dbPath = System.getProperty("user.home") + File.separator + "currency-exchange.db";
            String url = "jdbc:sqlite:" + dbPath;

            DatabaseInitializer.initialize(dbPath, url);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(10);
            config.addDataSourceProperty("journal_mode", "WAL");

            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize ConnectionManager: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}