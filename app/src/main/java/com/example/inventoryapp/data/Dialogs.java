package com.example.inventoryapp.data;

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

    InputFilter[] textboxLength = new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) };

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
}
