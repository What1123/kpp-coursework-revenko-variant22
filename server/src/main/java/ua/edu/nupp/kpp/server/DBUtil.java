package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Утилітний клас підключення до бази даних MySQL (car_service).
 * Метод {@link #getConnection()} використовується всіма DAO-класами.
 */
public final class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/car_service?characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
