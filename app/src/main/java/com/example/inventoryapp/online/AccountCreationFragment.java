package com.example.inventoryapp.online;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inventoryapp.R;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class AccountCreationFragment extends Fragment {

    private static final String TAG = "Account Creation Activity";
    Button createBtn;
    Fragment _this;
    FirebaseAuth mAuth;

    public AccountCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        SetupUI();
    }

    public void CreateAccountBehavior(View view){
        /* Reads input from textbox and creates user account in database */
        EditText email = (EditText) requireActivity().findViewById(R.id.email_editText);
        EditText password = (EditText) requireActivity().findViewById(R.id.password_edittext);
        if(!isEmailValid(email)){
            Toast.makeText(getContext(), R.string.error_invalidEmail, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isPasswordValid(password))
            return;
        String validEmail = email.getText().toString();
        String pass = password.getText().toString();
        mAuth.createUserWithEmailAndPassword(validEmail, pass)
                .addOnSuccessListener(this::onLoginSuccess)
                .addOnFailureListener(this::onCreationFailure);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_acc_creation, container, false);
    }

    private void SetupUI(){
        createBtn = (Button) requireActivity().findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(view -> CreateAccountBehavior(view));
    }

    boolean isEmailValid(EditText email_et) {
        CharSequence emailInput = email_et.getText().toString();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches();
    }

    boolean isPasswordValid(EditText password_et) {
        boolean isValid = true;
        String passwordInput;
        String dialogResponse = "";

        if(password_et.getText() == null){
            dialogResponse = getString(R.string.error_emptytextbox);
            return false;
        }
        passwordInput = password_et.getText().toString();
        if(passwordInput.length() < 6){
            dialogResponse = getString(R.string.error_shortpassword);
            isValid = false;
        }
        if(!dialogResponse.isEmpty())
            Toast.makeText(getContext(), dialogResponse, Toast.LENGTH_SHORT).show();
        return isValid;
    }

    private void onCreationFailure(Exception e) {
        Log.d(TAG, "createAccount: failure");
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void onLoginSuccess(AuthResult authResult) {
        Log.d(TAG, "createAccount: success");
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_accountCreationFragment_to_onlineLoginFragment);
        Toast.makeText(getContext(), getContext().getString(R.string.toast_accountcreated), Toast.LENGTH_SHORT).show();
    }
}