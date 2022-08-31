package com.example.inventoryapp;

import static com.example.inventoryapp.GlobalActions.online;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import org.json.JSONException;
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
            mydatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");
            Refresh();
        }
        SetupUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ListView lv =(ListView) findViewById(R.id.db_listview);
        new refreshDB(this, lv).execute();
        new ServerHandler.TestServerConnection(this).execute();
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
        new CreateUser(uname, pass).execute();
    }

    public static void FinishAccountCreation(){
        /* this function is called by the async task, once an account is
        * successfullly created */
        GlobalActions.NavigateToActivity(_this, LoginActivity.class);
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

    final static class CreateUser extends AsyncTask<Void, Void, Pair<Boolean, String>> {
        /* Performs post request to the remote server */
        private final String mUsername, mPassword;
        public CreateUser(String uname, String pass) {
            mUsername = uname; mPassword = pass;
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... args) {
            return ServerHandler.CreateUser(mUsername, mPassword);
        }

        @Override /* Run on the UI thread */
        protected void onPostExecute(Pair<Boolean, String> loginSuccessful) {
            if (loginSuccessful.first){
                FinishAccountCreation();
                Toast.makeText(_this, "Account Created", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(_this, loginSuccessful.second, Toast.LENGTH_SHORT).show();
            }
        }
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

    final static class refreshDB extends AsyncTask<Void, Integer, String> {
        /* Function used for online debugging */
        private final WeakReference<Activity> parentRef;
        private final WeakReference<ListView> listViewRef;

        public refreshDB(final Activity parent, final ListView listView)
        {
            parentRef = new WeakReference<Activity>(parent);
            listViewRef = new WeakReference<ListView>(listView);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return ServerHandler.GetListOfAccountsInDatabase();
        }

        @Override
        protected void onPostExecute(String db_result)
        {
            Activity parent = parentRef.get();
            ListView listView = listViewRef.get();

            try
            {
                JSONArray jsonArray = new JSONArray(db_result);
                ArrayList<String> names = new ArrayList<String>();
                for(int i=0; i<jsonArray.length(); i++)
                {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    names.add("username: "+obj.getString("username")+", password: "+obj.getString("password"));
                }
                ArrayAdapter nameadapter = new ArrayAdapter(parent.getApplicationContext(), R.layout.sample_accounts_listtextview, names);
                listView.setAdapter(nameadapter);
            }
            catch(Exception ex)
            {
                Log.d("JSONObject", "You had an exception");
                ex.printStackTrace();
            }
        }
    }
}