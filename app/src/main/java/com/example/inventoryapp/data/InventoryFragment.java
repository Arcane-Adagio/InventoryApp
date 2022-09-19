package com.example.inventoryapp.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.inventoryapp.R;
import com.example.inventoryapp.StorageHandler;
import com.example.inventoryapp.offline.OfflineInventoryManager;
import com.example.inventoryapp.online.FirebaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;
import java.util.Objects;

/* This is the ancestor abstract class that all fragments within the app inherit
* so that common functions don't have to be retyped as to be compliant with the DRY principle */

public class InventoryFragment extends Fragment {

    public static boolean DefaultMenuOptionSelection(@NonNull MenuItem item, Context context, Fragment callingFragment) {
        // This function is called when an icon is selected in the Actionbar
        switch (item.getItemId()){
            case R.id.online_viewaccount:
                Dialogs.ProfileDialog(context, FirebaseAuth.getInstance().getCurrentUser());
                return true;
            case R.id.online_logout:
                FirebaseHandler.LogoutBehavior(callingFragment);
                return true;
            case R.id.online_deleteaccount:
                FirebaseHandler.DeleteAccountBehavior(context, callingFragment);
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

    public void RenameAppBar(String newName){
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(newName);
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
        //Re-drafted function because I cant remember "AppCompatResources"
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
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public static boolean ExpandableFABDefaultBehavior(Boolean menuOpen
            , FloatingActionButton moreOptions_fab, FloatingActionButton[] subMenu, Context context){
        /* Function used to expand and minimize a floating action button menu */

        //Animations Used;
        Animation rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim);
        Animation rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim);
        Animation expandOpen = AnimationUtils.loadAnimation(context, R.anim.expand_anim);
        Animation minimizeClose = AnimationUtils.loadAnimation(context, R.anim.minimize_anim);

        //if menu is closed, make submenu open and clickable
        //and play respective animation
        if(!menuOpen){
            for(int i = 0; i<subMenu.length; i++){
                FloatingActionButton fab = subMenu[i];
                fab.setVisibility(View.VISIBLE);
                fab.setAnimation(expandOpen);
                fab.setEnabled(true);
            }
            moreOptions_fab.startAnimation(rotateOpen);
        }
        //if menu is open, make submenu closed and un-clickable
        //and play respective animation
        else {
            for(int i = 0; i<subMenu.length; i++){
                FloatingActionButton fab = subMenu[i];
                fab.setVisibility(View.GONE);
                fab.setAnimation(minimizeClose);
                fab.clearAnimation();
                fab.setEnabled(false);
            }
            moreOptions_fab.startAnimation(rotateClose);
        }
        //Return boolean to indicate new menu state
        return !menuOpen;
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
