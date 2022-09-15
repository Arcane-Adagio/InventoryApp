package com.example.inventoryapp.data;

import static com.example.inventoryapp.GlobalConstants.db_max_code_length;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;

import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;

public class Dialogs {

    public interface DialogListener{
        boolean submissionCallabck(String[] args);
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
            if (callbackObj.submissionCallabck(new String[] {} ))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void ShowRenameInventoryDialog(Context context, DialogListener callbackObj){
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
            if (callbackObj.submissionCallabck(new String[] {nameEditText.getText().toString()} ))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
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
            if (callbackObj.submissionCallabck(new String[] {nameEditText.getText().toString()} ))
                dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            callbackObj.cancelCallback();
            dialog.dismiss();
        });
        dialog.show();
    }
}
