package com.example.inventoryapp;

import static com.example.inventoryapp.GlobalActions.online;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccountCreationActivity extends AppCompatActivity {

    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase mydatabase;
    Button createBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_createaccount);
        if(!online){
            mydatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");
            Refresh();
        }
        else {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            refreshDB();
        }
        SetupUI();
    }

    private void Refresh(){
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

    private void refreshDB()
    {
        String db_result = ServerHandler.GetListOfAccountsInDatabase();
        String result = "";
        try
        {
            JSONArray jsonArray = new JSONArray(db_result);
            ArrayList<String> names = new ArrayList<String>();
            for(int i=0; i<jsonArray.length(); i++)
            {
                JSONObject obj = jsonArray.getJSONObject(i);
                names.add("username: "+obj.getString("username")+", password: "+obj.getString("password"));
            }
            ArrayAdapter nameadapter = new ArrayAdapter(this, R.layout.sample_accounts_listtextview, names);
            ListView listView = (ListView) findViewById(R.id.db_listview);
            listView.setAdapter(nameadapter);
        }
        catch(Exception ex)
        {
            Log.d("JSONObject", "You had an exception");
            ex.printStackTrace();
        }
    }

    public void CreateAccountBehavior(View view){
        /* Reads input from textbox and creates user account in database */
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        if(DBActions.AddUserToDatabase(dataAdapter, uname, pass, this))
            GlobalActions.NavigateToActivity(this, LoginActivity.class);
    }

    public void CreateAccountBehaviorOnline(View view){
        /* Reads input from textbox and creates user account in database */
        EditText username = (EditText) findViewById(R.id.username_editText);
        EditText password = (EditText) findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        if (uname.equals("") || pass.equals(""))
            return;
        if(ServerHandler.CreateUser(uname,pass))
            GlobalActions.NavigateToActivity(this, LoginActivity.class);
    }


    private void SetupUI(){
        createBtn = (Button) findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(online)
                    CreateAccountBehaviorOnline(view);
                else
                    CreateAccountBehavior(view);
            }
        });
    }

    private Cursor getAllUsers(){
        /* Helper function to Refresh func */
        return DBActions.GetAllUsersFromDatabase(this);
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        if(!online)
            mydatabase.close();
        super.onDestroy();
    }
}