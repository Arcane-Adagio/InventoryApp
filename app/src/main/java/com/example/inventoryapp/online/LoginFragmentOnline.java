package com.example.inventoryapp.online;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.inventoryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginFragmentOnline extends OnlineFragment {

    private FirebaseAuth mAuth;
    private EditText email_tb;
    private EditText password_tb;
    private TextView createAccount_tbtn;
    private TextView forgotPassword_tv;
    private final String TAG = "OnlineLoginActivity";
    private FirebaseUser mCurrentUser;
    private Button login_btn;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Activity cActivity;

    public LoginFragmentOnline() {
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
        email_tb = (EditText) requireView().findViewById(R.id.edittext_loginEmail);
        password_tb = (EditText) requireView().findViewById(R.id.edittext_TextPassword);
        forgotPassword_tv = (TextView) requireView().findViewById(R.id.textbtn_forgotPassword);
        forgotPassword_tv.setOnClickListener(v -> {NavigateToPasswordResetFragment();});
        login_btn = (Button) requireView().findViewById(R.id.login_btn);
        login_btn.setOnClickListener(view -> Login(null));
        createAccount_tbtn = (TextView) requireView().findViewById(R.id.textbtn_createAccount);
        createAccount_tbtn.setOnClickListener(view -> NavigateToAccountCreationFragment());
        if(mCurrentUser != null)
            AutoLogin();
        RenameAppBar("");
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

    private void NavigateToPasswordResetFragment(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineLoginFragment_to_accountResetFragment);
    }

    public void Login (View view){
        if(!textboxValidation())
            return;
        mAuth.signInWithEmailAndPassword(email_tb.getText().toString(), password_tb.getText().toString())
                .addOnSuccessListener(cActivity, authResult -> {
                    NavigateToGroupsFragment();
                }).addOnFailureListener(cActivity, e -> Toast.makeText(cActivity, "Failed to sign in", Toast.LENGTH_SHORT).show());
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