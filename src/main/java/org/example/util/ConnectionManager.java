package org.example.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String DB_NAME = "currency-exchange.db";
    private static final String DB_PATH = System.getProperty("user.home") + File.separator + DB_NAME;
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private static final int POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;

    static {
        synchronized (ConnectionManager.class) {
            try {
                Class.forName("org.sqlite.JDBC");
                ensureDatabaseInitialized();
                initConnections();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Критическая ошибка инициализации ConnectionManager", e);
            }
        }
    }

    private static void ensureDatabaseInitialized() {
        File dbFile = new File(DB_PATH);
        if (!dbFile.exists()) {
            System.out.println("Инициализация новой БД...");
            try (Connection conn = DriverManager.getConnection(URL)) {
                runScript(conn, "database/schema.sql");
                runScript(conn, "database/data.sql");
                System.out.println("БД успешно создана и заполнена.");
            } catch (Exception e) {
                if (dbFile.exists()) dbFile.delete();
                throw new RuntimeException("Ошибка при создании таблиц", e);
            }
        }
    }

    private static void runScript(Connection conn, String path) throws Exception {
        try (InputStream is = ConnectionManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Файл не найден: " + path);

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Разбиваем строго по ";" и фильтруем пустые строки
            String[] queries = content.split(";");

            try (Statement stmt = conn.createStatement()) {
                for (String query : queries) {
                    if (!query.trim().isEmpty()) {
                        stmt.execute(query.trim());
                    }
                }
            }
        }
    }

    private static void initConnections() {
        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            Connection connection = openConnection();
            Connection proxy = (Connection) Proxy.newProxyInstance(
                    ConnectionManager.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxyObj, method, args) ->
                            method.getName().equals("close")
                                    ? pool.add((Connection) proxyObj)
                                    : method.invoke(connection, args)
            );
            pool.add(proxy);
        }
    }

    public static Connection getConnection() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static Connection openConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ConnectionManager() {
    }
}