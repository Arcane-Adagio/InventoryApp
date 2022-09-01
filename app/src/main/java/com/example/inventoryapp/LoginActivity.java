package com.example.inventoryapp;
import static com.example.inventoryapp.GlobalActions.online;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";
    SQLiteDatabase accountDatabase;
    TextView createAccount_text;
    Button loginBtn;
    EditText usernameTB;
    EditText passwordTB;
    TextView forgotPasswordTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);
        if(!online){
            accountDatabase = openOrCreateDatabase(DBActions.ACCOUNT_DATABASE_NAME, MODE_PRIVATE, null);
            accountDatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, InventoryJSON VARCHAR);");
        }
    }

    @Override
    protected void onPostResume() {
        /* I dont know if it works, but what i do know is:
        * it's not broke */
        super.onPostResume();
        GlobalActions.logoutInProgress = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (online)
            SetupUIOnline();
        else
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
                LoginBehaviorOffline();
            }
        });
        passwordTB = (EditText) findViewById(R.id.edittext_TextPassword);
        passwordTB.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    LoginBehaviorOffline();
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
        forgotPasswordTV = (TextView) findViewById(R.id.forgotPassword_TV);
        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uInput = usernameTB.getText().toString();
                if (DBActions.IsUserInDataBase(uInput, view.getContext())){
                    Toast.makeText(LoginActivity.this, "Password is: "+DBActions.GetUserPassword(uInput, view.getContext()), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "User does not exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void SetupUIOnline(){
        createAccount_text = (TextView) findViewById(R.id.createAccount_textbtn);
        createAccount_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!GlobalActions.isNetworkConnected(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                GlobalActions.NavigateToActivity(LoginActivity.this, AccountCreationActivity.class);
            }
        });
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginBehaviorOnline();
            } //changed
        });
        passwordTB = (EditText) findViewById(R.id.edittext_TextPassword);
        passwordTB.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    LoginBehaviorOnline(); //changed
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
        forgotPasswordTV = (TextView) findViewById(R.id.forgotPassword_TV);
        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                String uInput = usernameTB.getText().toString();
                if (DBActions.IsUserInDataBase(uInput, view.getContext())){
                    Toast.makeText(LoginActivity.this, "Password is: "+DBActions.GetUserPassword(uInput, view.getContext()), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "User does not exists", Toast.LENGTH_SHORT).show();
                }
                 */
            }
        });
    }

    public void LoginBehaviorOffline(){
        String username =  usernameTB.getText().toString();
        String password =  passwordTB.getText().toString();
        if(!DBActions.IsUserInDataBase(username, this))
            Toast.makeText(this, getString(R.string.Toast_UserNotFound), Toast.LENGTH_SHORT).show();
        else if (!DBActions.GetUserPassword(username, this).equals(password)) // check password
            Toast.makeText(this, getString(R.string.Toast_IncorrectPassword), Toast.LENGTH_SHORT).show();
        else{
            //perform login
            User.setUserCredentials(username, password);
            User.setInventorys(DBActions.GetJSONString(username, this));
            GlobalActions.NavigateToActivity(this, InventoryActivity.class);
        }
    }

    public void LoginBehaviorOnline(){
        if(!GlobalActions.isNetworkConnected(getApplicationContext())){
            Toast.makeText(this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String username =  usernameTB.getText().toString();
        String password =  passwordTB.getText().toString();
        new ServerHandler.Login(this, username, password).execute();
    }

    private Cursor getAllUsers(){
        return DBActions.GetAllUsersFromDatabase(this);
    }

    @Override
    protected void onDestroy(){
        /* Perform cleanup if phone terminates activity prematurely */
        if(!online)
            accountDatabase.close();
        super.onDestroy();
    }

}