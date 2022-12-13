package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {

    private DBHelper database;

    public PersistentAccountDAO(DBHelper database) {
        this.database = database;
    }

    @Override
    public List<String> getAccountNumbersList() {
        return database.getAccountNumbersList();
    }

    @Override
    public List<Account> getAccountsList() {
        return database.getAccountsList();
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        return database.getAccount(accountNo);
    }

    @Override
    public void addAccount(Account account) {
        database.addAccount(account);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        database.removeAccount(accountNo);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        database.updateBalance(accountNo,expenseType,amount);
    }
}
