package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class InventoryItemActivity extends AppCompatActivity {

    private static FloatingActionButton rv_fab;
    private String mCurrentInventory;
    EditText mNameChangeEditText;
    ImageButton mConfirmNameButton;
    ImageButton mCancelNameButton;
    LinearLayout mNameChangeLayout;
    public static final String KEY_INVENTORYNAME = "inventoryName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_inventoryitem);
        this.registerReceiver(BroadcastHandler.GetBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString(KEY_INVENTORYNAME) != null){
                mCurrentInventory = extras.getString(KEY_INVENTORYNAME);
                SetupInventoryItemRecyclerView(mCurrentInventory);
                Objects.requireNonNull(getSupportActionBar()).setTitle(mCurrentInventory);
            }
        }
        rv_fab = (FloatingActionButton) findViewById(R.id.inventoryitem_fab);
        rv_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryItemRecyclerAdapter.GetItemRecyclerViewINSTANCE().AddItemToInventory();
            }
        });
        mCancelNameButton = (ImageButton) findViewById(R.id.inv_btn_namechange_cancel);
        mCancelNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameChangeEditText.setText("");
                mNameChangeLayout.setVisibility(View.GONE);
            }
        });
        mConfirmNameButton = (ImageButton) findViewById(R.id.inv_btn_namechange_confirm);
        mConfirmNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RenameInventory(mNameChangeEditText.getText().toString());
                mNameChangeLayout.setVisibility(View.GONE);
            }
        });
        mNameChangeEditText = (EditText) findViewById(R.id.newInventoryNameEditText);
        mNameChangeEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    RenameInventory(mNameChangeEditText.getText().toString());
                    mNameChangeLayout.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
        mNameChangeLayout = (LinearLayout) findViewById(R.id.namechange_view);
    }

    @Override
    protected void onDestroy() {
        /* According to documentation, activities should unregister receivers before destruction */
        super.onDestroy();
        unregisterReceiver(BroadcastHandler.GetBatteryReceiver);
    }

    private void RenameInventory(String newName){
        /* The background data must also be updated */
        int position = User.GetPositionOfInventory(mCurrentInventory);
        User.RenameInventory(mCurrentInventory, newName);
        mCurrentInventory = newName;
        InventoryItemRecyclerAdapter.GetItemRecyclerViewINSTANCE().UpdateCurrentInventoryName(mCurrentInventory);
        InventoryRecyclerViewerAdapter.GetHomeRecyclerViewINSTANCE().notifyItemChanged(position);
        Objects.requireNonNull(getSupportActionBar()).setTitle(newName);
    }

    private void SetupInventoryItemRecyclerView(String inventoryName){
        RecyclerView recyclerView = findViewById(R.id.inventoryitemlist_view);
        InventoryItemRecyclerAdapter adapter = InventoryItemRecyclerAdapter.ConstructItemRecyclerView(inventoryName, User.GetInventoryItems(inventoryName), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private boolean MenuOptionsSelected(@NonNull MenuItem item){
        /* Function to handle menu item behaviors specific to this activity */
        switch (item.getItemId()){
            case R.id.menu_inv_edit_title:
                mNameChangeLayout.setVisibility(View.VISIBLE);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* inflates the appbar to add menu items */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inventory_appbar_menu, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Handles behavior for when an appbar menu item is selected */
        if(MenuOptionsSelected(item))
            return true;
        else if (GlobalActions.DefaultMenuOptionSelection(item,this, getSupportFragmentManager()))
            return true;
        else
            return super.onOptionsItemSelected(item);
    }

}