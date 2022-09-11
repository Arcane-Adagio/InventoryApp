package com.example.inventoryapp;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import static com.example.inventoryapp.GlobalConstants.FRAGMENT_ARG_INVENTORY_NAME;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.inventoryapp.offline.OfflineInventoryFragment;
import com.example.inventoryapp.offline.OfflineInventoryManager;
import com.example.inventoryapp.online.FirebaseHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.InetAddress;

public class GlobalActions {
    public static final String KEY_SHAREDINVENTORY = "SharedInventory";

    public static boolean DefaultMenuOptionSelection(@NonNull MenuItem item, Context context, Fragment callingFragment) {
        // This function is called when an icon is selected in the Actionbar
        switch (item.getItemId()){
            case R.id.online_logout:
                FirebaseHandler.LogoutBehavior(callingFragment);
                return true;
            case R.id.online_deleteaccount:
                Toast.makeText(context, "online account deletion not implemented", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_user_save:
                OfflineInventoryManager.SaveUserInventory(context);
                return true;
            case R.id.menu_inv_file_save:
                new StorageHandler((Activity) context).WriteToFile();
                return true;
            default:
                Toast.makeText(context, context.getString(R.string.Toast_menu_default), Toast.LENGTH_LONG).show();
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


    public static void LogAllKeysinBundle(Intent intent){
        /* Debug Function */
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
        /* An alert that meant to display a message to a user */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(alertMessage);
        AlertDialog.Builder okay = alertDialogBuilder.setPositiveButton(context.getString(R.string.Dialog_okay),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //DoNothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void ShowCustomAlertToast(LayoutInflater inflater, Context context, View view, int layoutID, int toastID){
        View layout = inflater.inflate(layoutID, (ViewGroup) view.findViewById(toastID));
        final Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}
