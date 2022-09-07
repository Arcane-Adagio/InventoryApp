package com.example.inventoryapp.online;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.MainActivity;
import com.example.inventoryapp.R;
import com.example.inventoryapp.offline.OfflineInventoryFragment;
import com.example.inventoryapp.online.OnlineGroupFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class OnlineLoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText email_tb;
    private EditText password_tb;
    private TextView createAccount_tbtn;
    private final String TAG = "OnlineLoginActivity";
    private FirebaseUser mCurrentUser;
    private Button login_btn;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Activity cActivity;

    public OnlineLoginFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser(); //check if use is signed in (non-null and update UI accordingly
        cActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_online_login, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        email_tb = (EditText) getView().findViewById(R.id.edittext_loginEmail);
        password_tb = (EditText) getView().findViewById(R.id.edittext_TextPassword);
        login_btn = (Button) getView().findViewById(R.id.login_btn);
        login_btn.setOnClickListener(view -> Login(null));
        createAccount_tbtn = (TextView) getView().findViewById(R.id.textbtn_createAccount);
        createAccount_tbtn.setOnClickListener(view -> NavigateToAccountCreationFragment());
        if(mCurrentUser != null)
            AutoLogin();
    }

    public void Register(View view){
        String email = email_tb.getText().toString();
        String pasword = password_tb.getText().toString();
        createAccount(email, pasword);
    }

    public void AutoLogin(){
        Log.d(TAG, "AutoLogin: called");
        NavigateToGroupsFragment();
    }

    private void NavigateToGroupsFragment(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineLoginFragment_to_onlineGroupFragment);
    }

    private void NavigateToAccountCreationFragment(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineLoginFragment_to_accountCreationFragment);
    }

    public void createAccount(String email, String password){
        //could add SendVerificationEMail
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(cActivity, authResult -> {
            Log.d(TAG, "createAccount: success");
            FirebaseUser user = mAuth.getCurrentUser();
            Toast.makeText(cActivity, "Authentication Succeeded.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(cActivity, e -> {
            Log.d(TAG, "createAccount: failure");
            FirebaseUser user = mAuth.getCurrentUser();
            Toast.makeText(cActivity, "Auth Failure"+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void Login (View view){
        if(!textboxValidation())
            return;
        mAuth.signInWithEmailAndPassword(email_tb.getText().toString(), password_tb.getText().toString())
                .addOnSuccessListener(cActivity, authResult -> {
                    NavigateToGroupsFragment();
                }).addOnFailureListener(cActivity, e -> Toast.makeText(cActivity, "Failed to sign in", Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(cActivity, "Update User es Success", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void SendVerificationEmail(){
        mCurrentUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(cActivity, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void UpdateUserPassword(String password){
        mCurrentUser.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(cActivity, "Password has been updated", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void ReauthenticateUser(String email, String password){
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mCurrentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "User re-authenticated");
                    Toast.makeText(cActivity, "user re-authenticated", Toast.LENGTH_SHORT).show();
                });
    }

    public OnCompleteListener getDefaultOnCompleteListener(String successString, String failureString){
        return task -> {
            if(task.isSuccessful())
                Toast.makeText(cActivity, successString, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(cActivity, failureString, Toast.LENGTH_SHORT).show();
        };
    }

    public DatabaseReference.CompletionListener getDefaultOnCompletionListener(String successString, String failureString) {
        return (error, ref) -> {
            if (error != null)
                Toast.makeText(cActivity, successString, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(cActivity, failureString, Toast.LENGTH_SHORT).show();
        };
    }

    public void UpdateUserEmail(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null || !textboxValidation())
            return;
        user.updateEmail(email_tb.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(cActivity, "Update was a success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(cActivity, "Operation was a failure", Toast.LENGTH_SHORT).show());
    }

    public boolean isUserEmailVerified(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user==null)
            return false;
        return user.isEmailVerified();
    }

    public FirebaseAuth.AuthStateListener getAuthVerifiedListener(){
        //register the listener to make sure user cant login without being verified
        //example: user.addAuthStateListener(getAuthVerifiedListener);

        return firebaseAuth -> {
            if(isUserEmailVerified())
                Toast.makeText(cActivity, "Email is verified", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(cActivity, "Email is NOT verified", Toast.LENGTH_SHORT).show();
        };
    }

    private boolean textboxValidation(){
        String emailInput = email_tb.getText().toString();
        String passwordInput = password_tb.getText().toString();
        if(emailInput.equals("") || passwordInput.equals("")){
            Toast.makeText(cActivity, "Enter both Email and Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
}