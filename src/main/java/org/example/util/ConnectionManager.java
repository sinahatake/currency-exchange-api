package org.example.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String URL = "jdbc:sqlite:src/main/resources/currency-exchange.db";
    private static final int POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;

    static {
        initConnections();
    }

    private static void initConnections() {
        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            Connection connection = openConnection();
            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close") ?
                            pool.add((Connection) proxy) :
                            method.invoke(connection, args));
            pool.add(proxyConnection);
        }
    }

    public static Connection getConnection() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection openConnection() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private ConnectionManager() {
    }
}
