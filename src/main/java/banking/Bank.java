package banking;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class Bank {

    private final Scanner scanner = new Scanner(System.in);
    private List<String> availableNumber = new ArrayList<>();
    private final String databaseName;
    private final String tableName;

    public Bank(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        generateNumbers();
    }

    public void run() {
        while (true) {
            System.out.println("1. Create account\n" +
                    "2. Log into account\n" +
                    "0. Exit");
            int response = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (response) {
                case 0:
                    System.out.println("Bye!");
                    return;
                case 1:
                    createAccount();
                    break;
                case 2:
                    if (!loginMenu()) {
                        return;
                    }
            }
            System.out.println();
        }
    }

    private void generateNumbers() {
        Set<String> generatedNumbers = new HashSet<>();
        while (generatedNumbers.size() < 1000) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                stringBuilder.append((int) (Math.random() * 10));
            }
            String number = generateNumberWithChecksum("400000" + stringBuilder);
            generatedNumbers.add(number);
            availableNumber = new ArrayList<>(generatedNumbers);
        }
    }

    private String generateNumberWithChecksum(String number) {
        return number + checksum(number);
    }

    private String checksum(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Integer.parseInt(String.valueOf(number.charAt(i)));
            if ((i + 1) % 2 != 0) {
                digit *= 2;
            }
            if (digit > 9) {
                digit -= 9;
            }
            sum += digit;
        }
        return String.valueOf(sum % 10 == 0 ? 0 : 10 - (sum % 10));
    }

    private boolean isValid(String number) {
        String lastDigit = String.valueOf(number.charAt(number.length() - 1));
        String check = checksum(number.substring(0, number.length() - 1));
        return lastDigit.equals(check);
    }

    private String getANumber() {
        Collections.shuffle(availableNumber);
        return availableNumber.remove(0);
    }

    private String generatePIN() {
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            pin.append((int) (Math.random() * 10));
        }
        return pin.toString();
    }

    private Connection connect() {
        String url = "jdbc:sqlite:" + databaseName;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    private void insertToDatabase(String number, String pin) {
        String sql = "INSERT INTO " + tableName + "(number, pin) VALUES(?, ?);";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            statement.setString(2, pin);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Account selectAccountFromDatabase(String number, String pin) {
        String sql = "SELECT * FROM " + tableName + " WHERE number LIKE ? AND pin LIKE ?;";
        Account account = null;
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            statement.setString(2, pin);
            //Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    account = new Account(resultSet.getString("number"),
                            resultSet.getString("pin"),
                            resultSet.getInt("balance"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return account;
    }

    private Account selectAccountFromDatabase(String number) {
        String sql = "SELECT * FROM " + tableName + " WHERE number LIKE ?;";
        Account account = null;
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            //Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    account = new Account(resultSet.getString("number"),
                            resultSet.getString("pin"),
                            resultSet.getInt("balance"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return account;
    }

    private void updateAccountBalance(Account account) {
        String number = account.getAccountNumber();
        int balance = account.getBalance();
        String sql = "UPDATE " + tableName + " SET balance = ? WHERE number LIKE ?;";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, balance);
            statement.setString(2, number);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSenderAndRecipientBalance(Account sender, Account recipient) {
        String senderNumber = sender.getAccountNumber();
        int senderBalance = sender.getBalance();
        String recipientNumber = recipient.getAccountNumber();
        int recipientBalance = recipient.getBalance();

        String sql = "UPDATE " + tableName + " SET balance = ? WHERE number LIKE ?" +
                "UPDATE " + tableName + " SET balance = ? WHERE number LIKE ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, senderNumber);
            statement.setInt(2, senderBalance);
            statement.setString(3, recipientNumber);
            statement.setInt(4, recipientBalance);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteAccountFromDatabase(String number) {
        String sql = "DELETE FROM " + tableName + " WHERE number LIKE ?;";
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, number);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createAccount() {
        String number = getANumber();
        String pin = generatePIN();
        insertToDatabase(number, pin);
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(number);
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }

    private Account logIntoAccount() {
        System.out.println("Enter your card number:");
        String inputNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String inputPin = scanner.nextLine();
        System.out.println();
        return selectAccountFromDatabase(inputNumber, inputPin);
    }

    private void addIncome(Account account) {
        System.out.println("Enter income:");
        int income = Integer.parseInt(scanner.nextLine());
        account.receive(income);
        updateAccountBalance(account);
        System.out.println("Income was added!");
    }

    private void transfer(Account sender) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String destinationNumber = scanner.nextLine();
        if (!isValid(destinationNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return;
        }
        if (destinationNumber.equals(sender.getAccountNumber())) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }
        Account recipient = selectAccountFromDatabase(destinationNumber);
        if (recipient == null) {
            System.out.println("Such a card does not exist.");
            return;
        }
        System.out.println("Enter how much money you want to transfer:");
        int amount = Integer.parseInt(scanner.nextLine());
        if (amount > sender.getBalance()) {
            System.out.println("Not enough money!");
            return;
        }
        sender.spend(amount);
        recipient.receive(amount);
        updateAccountBalance(sender);
        updateAccountBalance(recipient);
        //updateSenderAndRecipientBalance(sender, recipient);
        System.out.println("Success!");
        Account send = selectAccountFromDatabase(sender.getAccountNumber());
        Account test = selectAccountFromDatabase(destinationNumber);
        System.out.println(send.getBalance());
        System.out.println(test.getBalance());
    }

    private void closeAccount(Account account) {
        deleteAccountFromDatabase(account.getAccountNumber());
        System.out.println("The account has been closed!");
    }

    public boolean loginMenu() {
        Account account = logIntoAccount();

        if (account != null) {
            System.out.println("You have successfully logged in!");
            System.out.println();
        } else {
            System.out.println("Wrong card number or PIN!");
            return true;
        }

        while (true) {
            System.out.println("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit");
            int response = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (response) {
                case 0:
                    System.out.println("Bye!");
                    return false;
                case 1:
                    System.out.println("Balance: " + account.getBalance());
                    break;
                case 2:
                    addIncome(account);
                    break;
                case 3:
                    transfer(account);
                    break;
                case 4:
                    closeAccount(account);
                    return true;
                case 5:
                    System.out.println("You have successfully logged out!");
                    return true;
            }
            System.out.println();
        }
    }
}