package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class DBActions {
    public static String loggedInUser;
    public static final String ACCOUNT_DATABASE_NAME = "Account";
    public static final String ACCOUNTDB_TABLE_NAME = "Users";


    public static Cursor GetAllUsersFromDatabase(Context context){
        /* SimpleCursorAdapter requires that the cursor;s result set must include a column
         * name exactly "_id". Don't hast to change schema if you didn't define the "_id" column
         * in your table. SQLite automatically added a hidden column called "rowid" for every table.
         * All you need to do is that - just select rowid explicitly and alias it as '_id' */
        Cursor resultSet = getUserDatabase(context).rawQuery("select rowid _id,* from "+ ACCOUNTDB_TABLE_NAME +";",null);
        if (resultSet.moveToFirst()){
            do {
                //DisplayUser(resultSet);
            } while (resultSet.moveToNext());
        }
        return resultSet;
    }

    public static void RemoveUserFromDatabase(String username, Context context){
        /* Deletes user from database when provided with username */
        SQLiteDatabase database = getUserDatabase(context);
        Cursor resultSet = database.rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            database.execSQL("DELETE FROM "+ ACCOUNTDB_TABLE_NAME +" WHERE Username='"+username+"';");
            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(context, "Username does not exists", Toast.LENGTH_SHORT).show();
    }

    public static boolean IsUserInDataBase(String username, Context context){
        return getUserDatabase(context)
                .rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null).getCount() > 0;
    }

    public static void AddUserToDatabase(SimpleCursorAdapter adapter, String username, String password, Context context){
        /* Reads Username and Password from Create Account page
         *  then adds account if account isn't already in the database
         *  */
        SQLiteDatabase database = getUserDatabase(context);
        Cursor resultSet = database.rawQuery("select * from "+ ACCOUNTDB_TABLE_NAME +" where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show();
        }
        else{
            database.execSQL("INSERT INTO "+ ACCOUNTDB_TABLE_NAME +" VALUES('"+username+"','"+password+"');");
            Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show();
        }
        resultSet.close();
        adapter.changeCursor(GetAllUsersFromDatabase(context));
    }

    private static SQLiteDatabase getUserDatabase(Context context){
        return context.openOrCreateDatabase(ACCOUNT_DATABASE_NAME, Context.MODE_PRIVATE, null);
    }

    public static void setLoggedInUser(String username){
        loggedInUser = username;
    }

    public static String getLoggedInUser(){
        return loggedInUser;
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

}
