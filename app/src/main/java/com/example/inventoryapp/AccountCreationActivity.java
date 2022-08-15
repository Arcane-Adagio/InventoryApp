package com.example.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AccountCreationActivity extends AppCompatActivity {

    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase mydatabase;
    Button createBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_createaccount);
        mydatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");
        Refresh();
        //DBActions.RunSQLQueryOnDataBase(mydatabase, dataAdapter, "UPDATE Users SET InventoryJSON = 'success' WHERE Username = 'dev';", this);
        SetupUI();
    }

    private void Refresh(){
        // The desired columns to be bound
        String[] columns = new String[] {
                "Username", "Password", "InventoryJSON"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[]{
                R.id.sample_username_view,
                R.id.sample_password_view,
                R.id.sample_inventory_view
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

    public void CreateAccount(View view){
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        DBActions.AddUserToDatabase(dataAdapter, uname, pass, this);
        GlobalActions.NavigateToActivity(this, LoginActivity.class);
    }

    private void SetupUI(){
        createBtn = (Button) findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateAccount(view);
            }
        });
    }

    private Cursor getAllUsers(){
        return DBActions.GetAllUsersFromDatabase(this);
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        mydatabase.close();
        super.onDestroy();
    }
}