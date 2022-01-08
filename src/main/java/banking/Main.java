package banking;

import java.sql.*;
import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String databaseName = args[1];
        String tableName = "card";
        createDatabase(databaseName);
        createTable(databaseName, tableName);

        Bank bank = new Bank(databaseName, tableName);
        bank.run();

    }

    public static void createDatabase(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTable(String databaseName, String tableName) {
        String url = "jdbc:sqlite:" + databaseName;
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "id INTEGER PRIMARY KEY," +
                "number TEXT," +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0" +
                ");";
        try (Connection connection = DriverManager.getConnection(url)) {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}