package com.example.inventoryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/* This class handles all calls and queries to the User Database */

public class DBActions {
    public static final String ACCOUNT_DATABASE_NAME = "Account";
    public static final String ACCOUNT_COLUMN_PASSWORD = "Password";
    public static final String ACCOUNT_COLUMN_INVENTORY = "InventoryJSON";
    public static final String ACCOUNTDB_TABLE_NAME = "Users";


    public static Cursor GetAllUsersFromDatabase(Context context){
        /* SimpleCursorAdapter requires that the cursor;s result set must include a column
         * name exactly "_id". Don't hast to change schema if you didn't define the "_id" column
         * in your table. SQLite automatically added a hidden column called "rowid" for every table.
         * All you need to do is that - just select rowid explicitly and alias it as '_id' */
        Cursor resultSet = getUserDatabase(context).rawQuery("select rowid _id,* from "+ ACCOUNTDB_TABLE_NAME +";",null);
        resultSet.moveToFirst();
        return resultSet;
    }

    public static void RemoveUserFromDatabase(String username, Context context){
        /* Deletes user from database when provided with username */
        SQLiteDatabase database = getUserDatabase(context);
        Cursor resultSet = database.rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            database.execSQL("DELETE FROM "+ ACCOUNTDB_TABLE_NAME +" WHERE Username='"+username+"';");
            Toast.makeText(context, context.getString(R.string.Toast_AccountDeleted), Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(context, context.getString(R.string.Toast_UserNotFound), Toast.LENGTH_SHORT).show();
    }

    public static boolean IsUserInDataBase(String username, Context context){
        /* Helper function */
        return getUserDatabase(context)
                .rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null).getCount() > 0;
    }

    public static boolean AddUserToDatabase(SimpleCursorAdapter adapter, String username, String password, Context context){
        /* Reads Username and Password from Create Account page
         *  then adds account if account isn't already in the database
         *  */
        boolean response;
        SQLiteDatabase database = getUserDatabase(context);
        Cursor resultSet = database.rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            Toast.makeText(context, context.getString(R.string.Toast_DuplicateUser), Toast.LENGTH_SHORT).show();
            response = false;
        }
        else{
            database.execSQL("INSERT INTO "+ ACCOUNTDB_TABLE_NAME +" VALUES('"+username+"','"+password+"','');");
            Toast.makeText(context, context.getString(R.string.Toast_AccountCreated), Toast.LENGTH_SHORT).show();
            response = true;
        }
        resultSet.close();
        adapter.changeCursor(GetAllUsersFromDatabase(context));
        return response;
    }

    private static SQLiteDatabase getUserDatabase(Context context){
        /* Helper Function */
        return context.openOrCreateDatabase(ACCOUNT_DATABASE_NAME, Context.MODE_PRIVATE, null);
    }

    public static Cursor RunSQLQueryOnDataBase(String string, Context context) {
        return getUserDatabase(context).rawQuery(string, null);
    }

    public static String GetJSONString(String username, Context context){
        Cursor cuss = RunSQLQueryOnDataBase("SELECT * FROM Users WHERE Username = '"+username+"';", context);
        cuss.moveToFirst(); // a must
        int colIndex = cuss.getColumnIndex(ACCOUNT_COLUMN_INVENTORY);
        if (colIndex != -1 && cuss.getString(colIndex) != null)
            return cuss.getString(colIndex);
        else
            return "";
    }

    public static String GetUserPassword(String username, Context context){
        Cursor cuss = RunSQLQueryOnDataBase("SELECT * FROM Users WHERE Username = '"+username+"';", context);
        cuss.moveToFirst(); // a must
        int colIndex = cuss.getColumnIndex(ACCOUNT_COLUMN_PASSWORD);
        if (colIndex != -1 && cuss.getString(colIndex) != null)
            return cuss.getString(colIndex);
        else
            return "";
    }

    public static void SaveInventoryJSON(Context context){
        getUserDatabase(context).execSQL("UPDATE '"+ACCOUNTDB_TABLE_NAME+"' SET InventoryJSON = '"+User.getInventoryJSON()+"' WHERE Username = '"+User.getUsername()+"';");
    }

    public static void RemoveStringFromDatabaseTable(SQLiteDatabase database, String tableName, String columnName, SimpleCursorAdapter adapter, String string, Context context){
        /* Template to Remember */
        Cursor resultSet = database.rawQuery("select * from "+tableName+" where "+columnName+"='"+string+"'",null);
        if(resultSet.getCount()>0){
            database.execSQL("DELETE FROM "+tableName+" WHERE "+columnName+"='"+string+"';");
            adapter.changeCursor(GetAllUsersFromDatabase(context));
        }
        Log.d("db", "Item was not found in database");
    }

    public static void CreateTableInDatabase(SQLiteDatabase database, String tableName, String columnArgs){
        /* Template to Remember */
        database.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+columnArgs+";");
    }

    public static void AddStringsToDatabase(SQLiteDatabase database, String tableName, SimpleCursorAdapter adapter, String string1, String string2, Context context){
        /* Template to Remember */
        database.execSQL("INSERT INTO "+tableName+" VALUES('"+string1+"','"+string2+"');");
        adapter.changeCursor(GetAllUsersFromDatabase(context));
    }

    public static boolean IsStringInDataBaseTable(SQLiteDatabase database, String tableName, String string){
        /* Template to Remember */
        return database.rawQuery("select * from "+tableName+" where Username='"+string+"'",null).getCount() > 0;
    }

    public static void ExecSQLOnDataBase(SQLiteDatabase database, SimpleCursorAdapter adapter, String string, Context context)
    { /* Template to Remember */
        database.execSQL(string);
        adapter.changeCursor(GetAllUsersFromDatabase(context));
    }

    public static void ExecSQLOnDataBase(SimpleCursorAdapter adapter, String string, Context context)
    { /* Template to Remember */
        getUserDatabase(context).execSQL(string);
        adapter.changeCursor(GetAllUsersFromDatabase(context));
    }

    public static void PrintCursorToLogcat(Cursor curse){
        /* Debugging Method */
        Log.d("db", DatabaseUtils.dumpCursorToString(curse));
    }


    public static String GetJSONString(Context context){
        Cursor cuss = RunSQLQueryOnDataBase("SELECT * FROM Users WHERE Username = '"+User.getUsername()+"';", context);
        cuss.moveToFirst(); // a must
        int colIndex = cuss.getColumnIndex(ACCOUNT_COLUMN_INVENTORY);
        if (colIndex != -1 && cuss.getString(colIndex) != null)
            return cuss.getString(colIndex);
        else
            return "";
    }
}
