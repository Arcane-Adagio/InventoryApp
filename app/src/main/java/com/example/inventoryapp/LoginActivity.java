package com.example.inventoryapp;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private SimpleCursorAdapter dataAdapter;
    SQLiteDatabase accountDatabase;
    TextView createAccount_text;
    Button loginBtn;
    EditText usernameTB;
    EditText passwordTB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);
        accountDatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
        accountDatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");

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
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginBehavior();
            }
        });
        passwordTB = (EditText) findViewById(R.id.edittext_TextPassword);
        passwordTB.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    LoginBehavior();
                    return true;
                }
                return false;
            }
        });
        usernameTB = (EditText) findViewById(R.id.edittext_loginUsername);
        usernameTB.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    passwordTB.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }

    public void LoginBehavior(){
        EditText username_et = (EditText) findViewById(R.id.edittext_loginUsername);
        String username =  username_et.getText().toString();
        if(!DBActions.IsUserInDataBase(username, this))
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
        else if (false) // check password
            Toast.makeText(this, "Password was incorrect", Toast.LENGTH_SHORT).show();
        else{
            //perform login
            User.setUsername(username);
            User.setInventorys(DBActions.GetJSONString(username, this));
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