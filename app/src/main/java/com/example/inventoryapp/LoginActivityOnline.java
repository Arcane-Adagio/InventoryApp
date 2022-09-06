package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivityOnline extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email_tb;
    private EditText password_tb;
    private final String TAG = "OnlineLoginActivity";
    private FirebaseUser mCurrentUser;
    private Button login_btn;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_loginonline);
        mAuth = FirebaseAuth.getInstance();
        email_tb = (EditText) findViewById(R.id.edittext_loginEmail);
        password_tb = (EditText) findViewById(R.id.edittext_TextPassword);
        login_btn = (Button) findViewById(R.id.login_btn);
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser != null)
            AutoLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if use is signed in (non-null and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login(null);
            }
        });
    }

    public void Register(View view){
        String email = email_tb.getText().toString();
        String pasword = password_tb.getText().toString();
        createAccount(email, pasword);
    }

    public void AutoLogin(){
        Log.d(TAG, "AutoLogin: called");
        Intent intent = new Intent(LoginActivityOnline.this, GroupActivity.class);
        startActivity(intent);
    }

    public void createAccount(String email, String password){
        //could add SendVerificationEMail
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d(TAG, "createAccount: success");
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(LoginActivityOnline.this, "Authentication Succeeded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "createAccount: failure");
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(LoginActivityOnline.this, "Auth Failure"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Login (View view){
        if(!textboxValidation())
            return;
        mAuth.signInWithEmailAndPassword(email_tb.getText().toString(), password_tb.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Intent intent = new Intent(LoginActivityOnline.this, GroupActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivityOnline.this, "Failed to sign in", Toast.LENGTH_SHORT).show();
                }
        });
    }

    public void UpdateUserProfile(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null)
            return;
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("DisplayName")
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener((task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(this, "Update User es Success", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void SendVerificationEmail(){
        mCurrentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivityOnline.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void UpdateUserPassword(String password){
        mCurrentUser.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivityOnline.this, "Password has been updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void ReauthenticateUser(String email, String password){
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mCurrentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "User re-authenticated");
                        Toast.makeText(LoginActivityOnline.this, "user re-authenticated", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public OnCompleteListener getDefaultOnCompleteListener(String successString, String failureString){
        return new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                    Toast.makeText(LoginActivityOnline.this, successString, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(LoginActivityOnline.this, failureString, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public DatabaseReference.CompletionListener getDefaultOnCompletionListener(String successString, String failureString) {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null)
                    Toast.makeText(LoginActivityOnline.this, successString, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(LoginActivityOnline.this, failureString, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void UpdateUserEmail(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null || !textboxValidation())
            return;
        user.updateEmail(email_tb.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivityOnline.this, "Update was a success", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivityOnline.this, "Operation was a failure", Toast.LENGTH_SHORT).show();
                }
        });
    }

    public boolean isUserEmailVerified(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user==null)
            return false;
        return user.isEmailVerified();
    }

    public FirebaseAuth.AuthStateListener getAuthVerifiedListener(){
        //register the listener to make sure user cant login without being verified
        //exaple: user.addAuthStateListener(getAuthVerifiedListener);

        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(isUserEmailVerified())
                    Toast.makeText(LoginActivityOnline.this, "Email is verified", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(LoginActivityOnline.this, "Email is NOT verified", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private boolean textboxValidation(){
        String emailInput = email_tb.getText().toString();
        String passwordInput = password_tb.getText().toString();
        if(emailInput.equals("") || passwordInput.equals("")){
            Toast.makeText(this, "Enter both Email and Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
}