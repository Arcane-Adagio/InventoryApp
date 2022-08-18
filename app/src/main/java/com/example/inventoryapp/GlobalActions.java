package com.example.inventoryapp;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import static com.example.inventoryapp.DBActions.RemoveUserFromDatabase;
import static com.example.inventoryapp.DBActions.SaveInventoryJSON;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.concurrent.Callable;
import java.util.logging.FileHandler;

public class GlobalActions {


    public static boolean DefaultMenuOptionSelection(@NonNull MenuItem item, Context context, FragmentManager fM) {
        // This function is called when an icon is selected in the Actionbar

        switch (item.getItemId()){
            case R.id.menu_logout:
                User.LogoutUser();
                NavigateToActivity(context, LoginActivity.class);
                return true;
            case R.id.menu_delete_account:
                if(User.getUsername() != null){
                    RemoveUserFromDatabase(User.getUsername(), context);
                    NavigateToActivity(context, LoginActivity.class);
                }
                else
                    Toast.makeText(context, "No", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_user_save:
                SaveInventoryJSON(context);
                return true;
            case R.id.menu_inv_file_save:
                new StorageHandler((Activity) context).WriteToFile();
                return true;
            case R.id.menu_inv_share:
                String testString = "aaa";
                Intent intent = new Intent(context, ServiceHandler.class);
                intent.putExtra("SharedInventory", testString);
                context.startService(intent);
                return true;
            default:
                Toast.makeText(context, "menu tapped", Toast.LENGTH_LONG).show();
                return false;
        }
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
            LayoutInflater layoutInflater = getLayoutInflater();
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

    public static Drawable GetDrawableFromInt(Context c, int id){
        return AppCompatResources.getDrawable(c, id);
    }


    public static void Alert(Context context, String alertMessage){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(alertMessage);
        alertDialogBuilder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}
