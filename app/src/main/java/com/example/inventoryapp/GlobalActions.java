package com.example.inventoryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.concurrent.Callable;

public class GlobalActions {


    public static boolean DefaultMenuOptionSelection(@NonNull MenuItem item, Context context, FragmentManager fM) {
        // This function is called when an icon is selected in the Actionbar
        /*
        switch (item.getItemId()){
            case R.id.home_btn_settings:
                //Intent intent = new Intent(context, SettingsActivity.class);
                //context.startActivity(intent);
                return true;
            case R.id.copy:
                Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show();
                return true;
            case R.id.home_btn_share: //Popup QR dialog
                //DialogFragment newFragment = GlobalActions.MyAlertDialogFragmentWithCustomLayout.newInstance(R.layout.qr_code_display);
                //newFragment.show(fM, context.getString(R.string.fragment_tag_dialog));
                return true;
            case R.id.home_btn_info:
                //Toast.makeText(context, context.getString(R.string.btn_info_text), Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
         */
        Toast.makeText(context, "menu tapped", Toast.LENGTH_LONG).show();
        return true;
    }

    public static void SetupToolbar(AppCompatActivity activity, int toolbarID){
        /*
         * This function is called When the activity is created
         * The purpose of this function is to manage the Actionbar
         * */
        Toolbar toolbar = (Toolbar) activity.findViewById(toolbarID);
        activity.setSupportActionBar(toolbar);
        ActionBar actBar = activity.getSupportActionBar();
        if (actBar == null)
            return;
    }

    public static class MyAlertDialogFragmentWithCustomLayout extends DialogFragment {
        static Callable<Void> positiveCallback;

        public static MyAlertDialogFragmentWithCustomLayout newInstance(int layout, Callable<Void> positiveFunc){
            MyAlertDialogFragmentWithCustomLayout frag = new MyAlertDialogFragmentWithCustomLayout();
            positiveCallback = positiveFunc;
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            int layout = getArguments().getInt("layout");
            final int list = getArguments().getInt("list");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View view = layoutInflater.inflate(layout, null);
            return builder.setView(view)
                    .setPositiveButton("Create", (dialogInterface, i) -> {
                        try {
                            positiveCallback.call();
                        } catch (Exception e) {
                            Log.d("error", "onCreateDialog: "+e.toString());
                            e.printStackTrace();
                        }
                    }).setNegativeButton("cancel", (dialogInterface, i) -> {
                        //do nothing
                    }).create();
        }
    }

    public static void NavigateToActivity(Context context, Class ActivityClass){
        Intent intent = new Intent(context, ActivityClass);
        context.startActivity(intent);
    }

    public static void LogAllKeysinBundle(Intent intent){
        Bundle b = intent.getExtras();
        if (b == null){
            Log.d("debug", "There are no Extras");
            return;
        }
        for (String key : b.keySet()) {
            Log.e("debug", key + " : " + (b.get(key) != null ? b.get(key) : "NULL"));
        }
    }
}
