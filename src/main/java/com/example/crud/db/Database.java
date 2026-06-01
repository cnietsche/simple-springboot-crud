package com.example.crud.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static final String JDBC_URL = "jdbc:h2:file:./data/users;AUTO_SERVER=TRUE";

    private Database() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, "sa", "");
    }

    public static void initSchema() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id   UUID PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
                    )
                    """);
        }
    }
}
