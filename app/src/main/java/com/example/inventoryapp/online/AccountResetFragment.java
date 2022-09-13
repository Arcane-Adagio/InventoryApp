package com.example.inventoryapp.online;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.inventoryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class AccountResetFragment extends Fragment {

    EditText email_et;
    Button reset_btn;

    public AccountResetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        email_et = (EditText) requireView().findViewById(R.id.editText_email);
        reset_btn = (Button) requireView().findViewById(R.id.btn_resetPassword);
        reset_btn.setOnClickListener(view -> SendVerificationEmail());
        SetupBottomNav();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_acc_reset, container, false);
    }

    boolean isEmailValid(EditText email_et) {
        CharSequence emailInput = email_et.getText().toString();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches();
    }

    private void SendVerificationEmail(){
        if(!isEmailValid(email_et)){
            Toast.makeText(getContext(), R.string.error_invalidEmail, Toast.LENGTH_SHORT).show();
            return;
        }
        String validEmail = email_et.getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(validEmail)
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), getContext().getString(R.string.toast_emailSent), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void SetupBottomNav(){
        BottomNavigationView nav = getActivity().findViewById(R.id.bottomnav_app);
        MenuItem item = nav.getMenu().findItem(R.id.onlineLoginFragment);
        item.setChecked(true);
    }
}