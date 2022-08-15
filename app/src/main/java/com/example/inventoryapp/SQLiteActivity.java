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
        GlobalActions.CreateTableInDatabase(mydatabase,"Users","(Username VARCHAR, Password VARCHAR)");
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
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        GlobalActions.AddUserToDatabase(mydatabase, "Users", dataAdapter, uname, pass, this);
    }

    public void DBDelete(String username){
        GlobalActions.RemoveUserFromDatabase(mydatabase,"Users", dataAdapter, username, this);
    }

    private Cursor getAllUsers(){
        return GlobalActions.GetAllUsersFromDatabase(mydatabase, "Users");
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        mydatabase.close();
        super.onDestroy();
    }
}