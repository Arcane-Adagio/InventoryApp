package com.example.inventoryapp.data;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dialogs {

    private static final String TAG = "Dialogs";
    public interface DialogListener{
        static List<Objects> receivedObjects = new ArrayList<Objects>();
        boolean submissionCallback(String[] args);
        void cancelCallback();
    }

    static InputFilter[] textboxLength = new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) };
    //TO DO make edit text a singlar line in xml for rename

    public static void AreYouSureDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlog_sure);
        //very important line - removes background to allow corner
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_cancel);
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            if (callbackObj.submissionCallback(new String[] {} ))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void RenameInventoryDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlog_renameinventory);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameInventory);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(textboxLength);
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            if(!(isTextboxValid(nameEditText)))
                return;
            if(callbackObj.submissionCallback(new String[] {nameEditText.getText().toString()}))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void CreateInventoryDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlog_createinventory);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_nameInventory);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(textboxLength);
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            if (callbackObj.submissionCallback(new String[] {nameEditText.getText().toString()} ))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void CreateGroupDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.frag_creategroup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.creategroup_submit_Btn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.creategroup_cancel_Btn);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_groupName);
        EditText passwordEditText = (EditText)dialog.findViewById(R.id.edittext_groupPassword);
        EditText codeEditText = (EditText)dialog.findViewById(R.id.edittext_groupCode);

        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_groupname_length) });
        passwordEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_password_length) });
        codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });

        //when focus has been lost, check if code is valid
        codeEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if(!hasFocus){
                Query query = FirebaseDatabase.getInstance().getReference("Groups")
                        .orderByChild("groupCode")
                        .equalTo(codeEditText.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long queryResultCount = snapshot.getChildrenCount();
                        if(queryResultCount == 0)
                            codeEditText.getBackground().clearColorFilter();
                        else {
                            codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                            codeEditText.setText("");
                            codeEditText.setHint("Group Code Already Taken");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        submitBtn.setOnClickListener(v -> {
            String nameText = nameEditText.getText().toString();
            String passwordText = passwordEditText.getText().toString();
            String codeText = codeEditText.getText().toString();
            if(nameText.equals("") || passwordText.equals("") || codeText.equals(""))
                return;
            Query query = FirebaseDatabase.getInstance().getReference("Groups")
                    .orderByChild("groupCode")
                    .equalTo(codeEditText.getText().toString());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long queryResultCount = snapshot.getChildrenCount();
                    if(queryResultCount == 0){
                        Log.d(TAG, "onDataChange: called");
                        if(callbackObj.submissionCallback(new String[] {nameText, codeText, passwordText}))
                            dialog.dismiss();
                    }
                    else {
                        codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        codeEditText.setText("");
                        codeEditText.setHint("Group Code Already Taken");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            codeEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void JoinGroupDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlog_addgroup);
        //very important line - removes background to allow corner
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_joinGroup_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_joinGroup_cancel);
        EditText passwordEditText = (EditText) dialog.findViewById(R.id.edittext_groupPassword);
        EditText codeEditText = (EditText)dialog.findViewById(R.id.edittext_groupCode);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            if(!(isTextboxValid(passwordEditText) || isTextboxValid(codeEditText)))
                return;
            int givenPasswordHashed = passwordEditText.getText().toString().hashCode();
            Query query = FirebaseDatabase.getInstance().getReference("Groups")
                    .orderByChild("groupCode")
                    .equalTo(codeEditText.getText().toString());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getChildrenCount() == 0){
                        codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        codeEditText.setText("");
                        codeEditText.setHint("Group Does Not Exist");
                        return;
                    }
                    for(DataSnapshot snap : snapshot.getChildren())
                        if(snap.hasChild("groupPasswordHashed")){
                            if(snap.child("groupPasswordHashed").getValue().equals(String.valueOf(givenPasswordHashed))){
                                if(callbackObj.submissionCallback(new String[] {snap.getKey()})){
                                    Toast.makeText(context, "joining group", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    break;
                                }
                            }
                            else {
                                passwordEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                                passwordEditText.setText("");
                                passwordEditText.setHint(context.getString(R.string.hint_passwordincorrect));
                                break;
                            }
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            codeEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void RenameGroupDialog(Context context, DialogListener callbackObj){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlog_renamegroup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameGroup);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            if(!(isTextboxValid(nameEditText)))
                return;
            if(callbackObj.submissionCallback(new String[] {nameEditText.getText().toString()}))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });
        dialog.show();
    }


    private static boolean isTextboxValid(EditText textbox){
        if(textbox.getText() == null)
            return false;
        if (textbox.getText().toString().isEmpty())
            return false;
        return true;
    }
}
