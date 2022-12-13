package lk.ac.mrt.cse.dbs.simpleexpensemanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context,String name,SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table account(accountNo varchar(20) primary key, bankName varchar(50), accountHolderName varchar(50), balance double)");
        DB.execSQL("create Table transactions(accountNo varchar(20) primary key, bankName varchar(50), accountHolderName varchar(50), balance double)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists account");
    }

    public Boolean addAccount(Account account){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",account.getAccountNo());
        contentValues.put("bankName",account.getBankName());
        contentValues.put("accountHolderName",account.getAccountHolderName());
        contentValues.put("balance",account.getBalance());
        long result = DB.insert("account",null,contentValues);
        return result != -1;
    }

    public List<String> getAccountNumbersList(){
        ArrayList<String> accountNumberList = new ArrayList<>();

        SQLiteDatabase DB = this.getReadableDatabase();
        String [] column = {"accountNo"};
        Cursor cursor = DB.query("account",column,null,null,null,null,null,null);

        while (cursor.moveToNext()){
            String acc_Num = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            accountNumberList.add(acc_Num);
        }

        cursor.close();
        return accountNumberList;
    }

    public List<Account> getAccountsList(){
        ArrayList<Account> accountList = new ArrayList<>();

        SQLiteDatabase DB = this.getReadableDatabase();
        String[] columns = {"accountNo","bankName","accountHolderName","balance"};
        Cursor cursor = DB.query("account",columns,null,null,null,null,null,null);

        String accountNo = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
        String bankName = cursor.getString(cursor.getColumnIndexOrThrow("bankName"));
        String accountHolderName = cursor.getString(cursor.getColumnIndexOrThrow("accountHolderName"));
        double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

        Account account = new Account(accountNo, bankName, accountHolderName, balance);
        accountList.add(account);

        cursor.close();
        return accountList;
    }

    public Account getAccount(String accountNo) throws InvalidAccountException{
        SQLiteDatabase DB = this.getReadableDatabase();
        String[] selectionArgs = {accountNo};
        String [] acc = {"accountNo","bankName","accountHolderName","balance"};
        Cursor cursor = DB.query("account",acc,"accountNo = ?",selectionArgs,null,null,null,null);

        Account account = null;
        if (cursor.getCount() == 1){
            String acc_No = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            String bankName = cursor.getString(cursor.getColumnIndexOrThrow("bankName"));
            String accountHolderName = cursor.getString(cursor.getColumnIndexOrThrow("accountHolderName"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            account = new Account(acc_No,bankName,accountHolderName,balance);
        }else {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

        cursor.close();
        return account;


    }

    public void removeAccount(String accountNo) throws InvalidAccountException{
        SQLiteDatabase DB = this.getWritableDatabase();

        String [] selectionArgs = {accountNo};
        String [] acc = {"accountNo","bankName","accountHolderName","balance"};
        Cursor cursor = DB.query("account", acc,"accountNo = ?",selectionArgs,null,null,null,null);

        if (cursor.getCount() == 1) {
            DB.delete("account", "accountNo = ?",selectionArgs);
        }else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
    }

    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException{
        SQLiteDatabase db = this.getWritableDatabase();
        Account account = getAccount(accountNo);

        double balance = 0;

        switch (expenseType) {
            case EXPENSE:
                balance = account.getBalance() - amount;
                break;
            case INCOME:
                balance = account.getBalance() + amount;
                break;
        }

        ContentValues values = new ContentValues();
        values.put("balance" , balance );

        long result  = db.update("Accounts", values, "accountNo = ?", new String[]{accountNo});
    }

    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",accountNo);
        contentValues.put("date", date.toString());
        contentValues.put("expenseType",expenseType.toString());
        contentValues.put("amount",amount);

        DB.insert("Transaction",null,contentValues);
    }

    public List<Transaction> getAllTransactionLogs() {
        ArrayList<Transaction> transactions = new ArrayList<>();

        SQLiteDatabase DB = this.getReadableDatabase();
        String[] columns = {"date","accountNo", "expenceType","amount"};

        Cursor cursor = DB.query("Transaction",columns,null,null,null,null,null);

        while (cursor.moveToNext()){
            String date = cursor.getString(cursor.getColumnIndexOrThrow("data"));
            String accountNo = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            String expenceType = cursor.getString(cursor.getColumnIndexOrThrow("expenceType"));
            Double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

            ExpenseType expense = null;

            if (expenceType.equals("EXPENSE")){
                expense = ExpenseType.EXPENSE;
            }else{
                expense = ExpenseType.INCOME;
            }

            Date d= null;
            try {
                d = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy").parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Transaction trans = new Transaction(d,accountNo,expense,amount);
            transactions.add(trans);
        }

        cursor.close();
        return transactions;
    }

    public List<Transaction> getPaginatedTransactionLogs(int limit){
        SQLiteDatabase db=this.getReadableDatabase();

        long cnt  = DatabaseUtils.queryNumEntries(db, "transactions");

        if(limit<=cnt){
            return getAllTransactionLogs();
        }
        else {
            String[] columns = {"date", "accountNo", "expenceType", "amount"};
            Cursor cursor = db.query("transactions", columns, null, null, null, null, null);
            List<Transaction> transactions = new ArrayList<>();
            int count = 0;

            while (cursor.moveToNext() && count < limit) {

                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String accountNo = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
                String expenceType = cursor.getString(cursor.getColumnIndexOrThrow("expenceType"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

                ExpenseType expense = null;

                if (expenceType.equals("EXPENSE")) {
                    expense = ExpenseType.EXPENSE;
                } else {
                    expense = ExpenseType.INCOME;
                }

                Date d = null;
                try {
                    d = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy").parse(date);

                } catch (ParseException e) {

                    e.printStackTrace();
                }

                Transaction trnas = new Transaction(d, accountNo, expense, amount);
                transactions.add(trnas);
                count++;
            }

            cursor.close();
            return transactions;
        }
    }
}

