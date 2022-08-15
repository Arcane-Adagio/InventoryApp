package com.example.inventoryapp;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
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

    public static Drawable GetDrawableFromInt(Context c, int id){
        return AppCompatResources.getDrawable(c, id);
    }

    public static Cursor GetAllUsersFromDatabase(SQLiteDatabase database, String databaseName){
        /* SimpleCursorAdapter requires that the cursor;s result set must include a column
         * name exactly "_id". Don't hast to change schema if you didn't define the "_id" column
         * in your table. SQLite automatically added a hidden column called "rowid" for every table.
         * All you need to do is that - just select rowid explicitly and alias it as '_id' */
        Cursor resultSet = database.rawQuery("select rowid _id,* from "+databaseName+";",null);
        if (resultSet.moveToFirst()){
            do {
                //DisplayUser(resultSet);
            } while (resultSet.moveToNext());
        }
        return resultSet;
    }

    public static void RemoveUserFromDatabase(SQLiteDatabase database, String databaseName, SimpleCursorAdapter adapter, String username, Context context){
        /* Deletes user from database when provided with username */
        Cursor resultSet = database.rawQuery("select * from Users where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            database.execSQL("DELETE FROM Users WHERE Username='"+username+"';");
            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show();
            adapter.changeCursor(GetAllUsersFromDatabase(database, databaseName));
        }
        Toast.makeText(context, "Username does not exists", Toast.LENGTH_SHORT).show();
    }

    public static boolean IsUserInDataBase(SQLiteDatabase database, String databaseName, SimpleCursorAdapter adapter, String username){
        return database.rawQuery("select * from Users where Username='"+username+"'",null).getCount() > 0;
    }

    public static void AddUserToDatabase(SQLiteDatabase database, String databaseName, SimpleCursorAdapter adapter, String username, String password, Context context){
        /* Reads Username and Password from Create Account page
         *  then adds account if account isn't already in the database
         *  */
        Cursor resultSet = database.rawQuery("select * from Users where Username='"+username+"'",null);
        if(resultSet.getCount()>0){
            Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show();
        }
        else{
            database.execSQL("INSERT INTO Users VALUES('"+username+"','"+password+"');");
            Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show();
        }
        resultSet.close();
        adapter.changeCursor(GetAllUsersFromDatabase(database, databaseName));
    }

    public static void CreateTableInDatabase(SQLiteDatabase database, String tableName, String columnArgs){
        /* Column Args should be surrounded by parenthesis and separated by commas for each arg
        *  Ex: (Username VARCHAR, Password VARCHAR)
        * */
        database.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+columnArgs+";");
    }
}
