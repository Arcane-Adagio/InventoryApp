package com.example.inventoryapp;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase accountDatabase;
    TextView createAccount_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);
        accountDatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
        Refresh();
        SetupUI();
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
    }

    public void SetupUI(){
        createAccount_text = (TextView) findViewById(R.id.createAccount_textbtn);
        createAccount_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalActions.NavigateToActivity(LoginActivity.this, AccountCreationActivity.class);
            }
        });
    }

    public void LoginBehavior(View view){
        EditText username_et = (EditText) findViewById(R.id.edittext_loginUsername);
        String username =  username_et.getText().toString();
        if(!DBActions.IsUserInDataBase(username, this))
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
        else if (false) // check password
            Toast.makeText(this, "Password was incorrect", Toast.LENGTH_SHORT).show();
        else{
            //perform login
            DBActions.loggedInUser = username;
            GlobalActions.NavigateToActivity(this, MainActivity.class);
        }
    }

    private Cursor getAllUsers(){
        return DBActions.GetAllUsersFromDatabase(this);
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely*/
        accountDatabase.close();
        super.onDestroy();
    }

}