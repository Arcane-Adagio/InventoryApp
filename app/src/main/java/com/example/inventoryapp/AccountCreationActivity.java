package com.example.inventoryapp;

import static com.example.inventoryapp.GlobalActions.online;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AccountCreationActivity extends AppCompatActivity {

    private static final String TAG = "Account Creation Activity";
    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase mydatabase;
    Button createBtn;
    static Activity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_createaccount);
        _this = this;
        if(!online){
            mydatabase = openOrCreateDatabase(LocalDBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");
            Refresh();
        }
        SetupUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ListView lv =(ListView) findViewById(R.id.db_listview);
    }

    public void CreateAccountBehavior(View view){
        /* Reads input from textbox and creates user account in database */
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        if(LocalDBActions.AddUserToDatabase(dataAdapter, uname, pass, this))
            GlobalActions.NavigateToActivity(this, LoginActivity.class);
    }

    public static void FinishAccountCreation(){
        /* this function is called by the async task, once an account is
        * successfullly created */
        GlobalActions.NavigateToActivity(_this, LoginActivity.class);
    }

    private void SetupUI(){
        createBtn = (Button) findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(view -> CreateAccountBehavior(view));
    }


    private Cursor getAllUsers(){
        /* Helper function to Refresh func */
        return LocalDBActions.GetAllUsersFromDatabase(this);
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        if(!online)
            mydatabase.close();
        super.onDestroy();
    }

    private void Refresh(){
        /*Function used for offline debugging*/

        /*
         * Boiler Plate Code
         * to setup database
         * or something */
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
}