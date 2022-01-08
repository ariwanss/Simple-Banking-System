package banking;

public class Account {
    private final String accountNumber;
    private int balance;
    private String pin;

    public Account(String accountNumber, String pin) {
        this.accountNumber = accountNumber;
        this.balance = 0;
        this.pin = pin;
    }

    public Account(String accountNumber, String pin, int balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.pin = pin;
    }

    public void receive(int amount) {
        this.balance += amount;
    }

    public void spend(int amount) {
        this.balance -= amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getBalance() {
        return balance;
    }

    public String getPin() {
        return pin;
    }
}
