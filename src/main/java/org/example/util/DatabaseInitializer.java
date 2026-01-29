package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    static void initialize(String dbPath, String url) throws ClassNotFoundException {
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            Class.forName("org.sqlite.JDBC");
            logger.info("Initializing new database at: {}", dbPath);
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);

                runScript(conn, "database/schema.sql");
                runScript(conn, "database/data.sql");

                conn.commit();
                logger.info("Database initialized successfully.");
            } catch (Exception e) {
                logger.error("Database initialization failed. Deleting corrupted file.", e);
                if (dbFile.exists()) {
                    boolean deleted = dbFile.delete();
                    if (deleted) {
                        logger.info("Corrupted database file was successfully deleted.");
                    } else {
                        logger.warn("Failed to delete corrupted database file: {}. Check file permissions or locks.", dbPath);
                    }
                }
                throw new RuntimeException("Critical error during DB initialization", e);
            }
        } else {
            logger.debug("Database file already exists. Skipping initialization.");
        }
    }

    private static void runScript(Connection conn, String path) throws IOException, SQLException {
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("SQL script file not found: " + path);
            }

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String cleanSql = content.replaceAll("--.*", "");
            String[] queries = cleanSql.split(";");

            for (String query : queries) {
                String trimmed = query.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }
}