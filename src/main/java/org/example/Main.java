package org.example;

import org.example.jdbc.util.ConnectionManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        System.out.println(ConnectionManager.getConnection().getTransactionIsolation());
    }
}