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
        SetupUI();
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
            User.setUsername(username);
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