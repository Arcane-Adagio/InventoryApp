package com.example.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SQLiteActivity extends AppCompatActivity {

    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase mydatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_createaccount);
        mydatabase = openOrCreateDatabase("Account", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Users(Username VARCHAR, Password VARCHAR);");
        Refresh();
        //DBDelete("Patrick");
    }

    private void Refresh(){
        // The desired columns to be bound
        String[] columns = new String[] {
                "Username", "Password"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[]{
            R.id.sample_username_view,
            R.id.sample_password_view
        };

        // create the adapter using the cursor pointing to the desired data
        // as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this,
                R.layout.sample_account_listitem,
                getAllUsers(),
                columns,
                to,
                0
        );

        ListView listView = (ListView) findViewById(R.id.db_listview);
        listView.setAdapter(dataAdapter);
    }

    public void DBinsert(View view){
        /* Reads Username and Password from Create Account page
        *  then adds account if account isn't already in the database
        *  */
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        Cursor resultSet = mydatabase.rawQuery("select * from Users where Username='"+uname+"'",null);
        if(resultSet.getCount()>0){
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
        else{
            mydatabase.execSQL("INSERT INTO Users VALUES('"+uname+"','"+pass+"');");
            Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
        }
        resultSet.close();
        dataAdapter.changeCursor(getAllUsers());
    }

    public void DBDelete(String username){
        /* Deletes user from database when provided with username */

        Cursor resultSet = mydatabase.rawQuery("select * from Users where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            mydatabase.execSQL("DELETE FROM Users WHERE Username='"+username+"';");
            Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
            dataAdapter.changeCursor(getAllUsers());
        }
        Toast.makeText(this, "Username does not exists", Toast.LENGTH_SHORT).show();
    }

    private Cursor getAllUsers(){
        /* SimpleCursorAdapter requires that the cursor;s result set must include a column
        * name exactly "_id". Don't hast to change schema if you didn't define the "_id" column
        * in your table. SQLite automatically added a hidden column called "rowid" for every table.
        * All you need to do is that - just select rowid explicitly and alias it as '_id' */
        Cursor resultSet = mydatabase.rawQuery("select rowid _id,* from Users;",null);
        if (resultSet.moveToFirst()){
            do {
                //DisplayUser(resultSet);
            } while (resultSet.moveToNext());
        }
        return resultSet;
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        mydatabase.close();
        super.onDestroy();
    }
}