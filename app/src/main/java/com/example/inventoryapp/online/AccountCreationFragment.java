package com.example.inventoryapp.online;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.inventoryapp.R;


public class AccountCreationFragment extends Fragment {

    private static final String TAG = "Account Creation Activity";
    Button createBtn;
    static Fragment _this;

    public AccountCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
    }

    @Override
    public void onStart() {
        super.onStart();
        SetupUI();
    }

    public void CreateAccountBehavior(View view){
        /* Reads input from textbox and creates user account in database
         * obsolete */
        EditText username = (EditText) getView().findViewById(R.id.username_editText);
        EditText password = (EditText) getView().findViewById(R.id.password_edittext);
        String uname = username.getText().toString();
        String pass = password.getText().toString();
        //TODO firebase create account here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_acc_creation, container, false);
    }

    private void SetupUI(){
        createBtn = (Button) getView().findViewById(R.id.btn_createaccount);
        createBtn.setOnClickListener(view -> CreateAccountBehavior(view));
    }

    private void NavigateToLoginFragment(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_accountCreationFragment_to_onlineLoginFragment);
    }

}