package com.example.inventoryapp.online;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inventoryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;


public class AccountCreationFragment extends OnlineFragment {

    private static final String TAG = "Account Creation Activity";
    Button createBtn;
    Fragment _this;
    UserProfileChangeRequest.Builder request;

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
        SetupBottomNav();
    }

    private void SetupUI(){
        createBtn = (Button) requireActivity().findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(view -> CreateAccountBehavior(view));
    }

    public void CreateAccountBehavior(View view){
        /* Reads input from textbox and creates user account in database */
        EditText email = (EditText) requireActivity().findViewById(R.id.email_editText);
        EditText displayName = (EditText) requireActivity().findViewById(R.id.displayName_edittext);
        EditText password = (EditText) requireActivity().findViewById(R.id.password_edittext);
        EditText passwordDup = (EditText) requireActivity().findViewById(R.id.retypePassword_edittext);
        if(!isEmailValid(email)){
            Toast.makeText(getContext(), R.string.error_invalidEmail, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!arePasswordsValid(password, passwordDup) || !isDisplayNameValid(displayName))
            return;
        String validEmail = email.getText().toString();
        String pass = password.getText().toString();
        String name = displayName.getText().toString();
        request = new UserProfileChangeRequest.Builder().setDisplayName(name);
        mAuth.createUserWithEmailAndPassword(validEmail, pass)
                .addOnSuccessListener(this::onCreationSuccess)
                .addOnFailureListener(this::onCreationFailure);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_acc_creation, container, false);
    }


    boolean isEmailValid(EditText email_et) {
        CharSequence emailInput = email_et.getText().toString();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches();
    }

    boolean arePasswordsValid(EditText password_et, EditText retypedPassword_ET) {
        if(isTextboxNullorEmpty(password_et)){
            Toast.makeText(getContext(), getString(R.string.error_emptytextbox), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(isTextboxTextTooShort(password_et, 6)){
            Toast.makeText(getContext(), getString(R.string.error_shortpassword), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(isTextboxNullorEmpty(retypedPassword_ET)){
            Toast.makeText(getContext(), "Please re-enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!password_et.getText().toString().equals(retypedPassword_ET.getText().toString())){
            Toast.makeText(getContext(), "The passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    boolean isDisplayNameValid(EditText displayName_ET){
        if(isTextboxNullorEmpty(displayName_ET)){
            Toast.makeText(getContext(), "Please enter a display name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(isTextboxTextTooShort(displayName_ET, 3)){
            Toast.makeText(getContext(), "Display name is too short", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    boolean isTextboxNullorEmpty(EditText textbox){
        if (textbox.getText() == null)
            return true;
        return textbox.getText().toString().isEmpty();
    }

    boolean isTextboxTextTooShort(EditText textbox, int maxLength){
        try{ // an exception can be thrown if the textbox is null
            return textbox.getText().toString().length() < maxLength;}
        catch (Exception e){
            return true;
        }
    }


    private void onCreationFailure(Exception e) {
        Log.d(TAG, "createAccount: failure");
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void onCreationSuccess(AuthResult authResult) {
        Log.d(TAG, "createAccount: success");
        mAuth.getCurrentUser().updateProfile(request.build());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_accountCreationFragment_to_onlineLoginFragment);
        Toast.makeText(getContext(), getContext().getString(R.string.toast_accountcreated), Toast.LENGTH_SHORT).show();
    }

    public void SetupBottomNav(){
        BottomNavigationView nav = getActivity().findViewById(R.id.bottomnav_app);
        MenuItem item = nav.getMenu().findItem(R.id.onlineLoginFragment);
        item.setChecked(true);
    }
}