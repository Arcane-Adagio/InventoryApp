package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    private final String TAG = "Inventory Activity";
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_home);
        SetupInventoryRecyclerView();
        this.registerReceiver(BroadcastHandler.GetBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(BroadcastHandler.SharedInventoryReceiver, new IntentFilter(GlobalActions.EXPORT_ACTION));

    }

    private void SetupInventoryRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.inventorylist_view);
        InventoryRecyclerViewerAdapter adapter = InventoryRecyclerViewerAdapter.ConstructHomeRecyclerViewIfNotCreated( User.GetInventoryNames(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        fab = (FloatingActionButton) findViewById(R.id.inventory_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryRecyclerViewerAdapter.GetHomeRecyclerViewINSTANCE().AddInventory();
            }
        });
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void createInventoryGroup (View view){
        //communicates with the server to add inventory group
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        /* Documentation says to make sure receivers get unregistered */
        Log.d(TAG, "onDestroy");
        unregisterReceiver(BroadcastHandler.GetBatteryReceiver);
        unregisterReceiver(BroadcastHandler.SharedInventoryReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalActions.LogoutBehavior(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflates appbar to include menu options */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_appbar_menu, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Handles behavior for when a menu option is selected */
        if (GlobalActions.DefaultMenuOptionSelection(item,this, getSupportFragmentManager()))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void ShowGroupCreationDialog(View view){
        CreateGroupDialog();
    }

    public void CreateGroupDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.frag_creategroup);
        Button submitBtn = (Button) dialog.findViewById(R.id.creategroup_submit_Btn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.creategroup_cancel_Btn);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_groupName);
        EditText passwordEditText = (EditText)dialog.findViewById(R.id.edittext_groupPassword);
        EditText codeEditText = (EditText)dialog.findViewById(R.id.edittext_groupCode);

        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_groupname_length) });
        passwordEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_groupname_length) });
        codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_groupname_length) });

        codeEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if(!hasFocus)
                new ServerHandler.isGroupCodeValid(codeEditText.getText().toString(), codeEditText).execute();
        });
        submitBtn.setOnClickListener(v -> {
            String nameText = nameEditText.getText().toString();
            String passwordText = passwordEditText.getText().toString();
            String codeText = codeEditText.getText().toString();

            if(nameText.equals("") || passwordText.equals("") || codeText.equals(""))
                return;
            new ServerHandler.CreateGroup(codeText, nameText, passwordText, this).execute();
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            codeEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    /*
    final static class GetInventorys extends AsyncTask<Void, Integer, String> {
        // Function used for online debugging /
        private final WeakReference<Activity> parentRef;
        private final WeakReference<ListView> listViewRef;

        public refreshDB(final Activity parent, final ListView listView)
        {
            parentRef = new WeakReference<Activity>(parent);
            listViewRef = new WeakReference<ListView>(listView);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return ServerHandler.GetListOfAccountsInDatabase();
        }

        @Override
        protected void onPostExecute(String db_result)
        {
            Log.d(TAG, "onPostExecute: "+db_result);
            Activity parent = parentRef.get();
            ListView listView = listViewRef.get();

            try
            {
                JSONArray jsonArray = new JSONArray(db_result);
                ArrayList<String> names = new ArrayList<String>();
                for(int i=0; i<jsonArray.length(); i++)
                {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    names.add("username: "+obj.getString("username")+", password: "+obj.getString("password"));
                }
                ArrayAdapter nameadapter = new ArrayAdapter(parent.getApplicationContext(), R.layout.sample_accounts_listtextview, names);
                listView.setAdapter(nameadapter);
            }
            catch(Exception ex)
            {
                Log.d("JSONObject", "You had an exception");
                ex.printStackTrace();
            }
        }
    }
    */
}