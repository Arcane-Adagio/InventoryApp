package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InventoryActivity extends AppCompatActivity {

    private ImageButton addItem_btn;
    private Fragment fragment;
    private FragmentManager fm;
    private static final String LIST_FRAG_TAG = "ListRV";
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("life", "onCreate: alive");
        setContentView(R.layout.page_inventory);
        HandleExtras();
        RecyclerViewFragment(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        addItem_btn = (ImageButton) findViewById(R.id.add_item_btn);
        addItem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSampleTask();
            }
        });
    }

    private void AddSampleTask(){
        RecyclerViewFragment.AddNewItem();
    }

    public void HandleExtras(){
        Bundle extras = getIntent().getExtras();
        if(extras == null)
            return;
        if (extras.getString("name") != null)
            if(getSupportActionBar() != null)
                getSupportActionBar().setTitle(extras.getString("name"));
        else
            GlobalActions.LogAllKeysinBundle(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inventory_appbar_menu, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (GlobalActions.DefaultMenuOptionSelection(item,this, getSupportFragmentManager()))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void RecyclerViewFragment(Bundle savedInstanceState){

        if(savedInstanceState == null)
            fragment = new RecyclerViewFragment();
        else
            fragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAG_TAG);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container2, fragment, LIST_FRAG_TAG)
                .commit();
    }
}