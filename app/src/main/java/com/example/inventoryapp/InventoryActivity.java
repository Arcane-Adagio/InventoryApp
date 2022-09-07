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

}