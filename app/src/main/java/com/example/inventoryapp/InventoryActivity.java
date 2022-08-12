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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class InventoryActivity extends AppCompatActivity {

    private ImageButton addItem_btn;
    private Fragment fragment;
    private FragmentManager fm;
    private static final String LIST_FRAG_TAG = "ListRV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("life", "onCreate: alive");
        setContentView(R.layout.inventory_page);
        HandleExtras();
        FragmentManager fm = getSupportFragmentManager();
        RecyclerViewFragment(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        addItem_btn = (ImageButton) findViewById(R.id.add_item_btn);
        addItem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSampleTask();
            }
        });
        super.onStart();
    }

    private void AddSampleTask(){

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
        //.makeText(this, "here", Toast.LENGTH_SHORT).show();
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
        /*
        if(savedInstanceState == null)
            fragment = new RecyclerViewFragment();
        else
        {
            Log.d("error","bulsh");
            fragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAG_TAG);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container2, fragment, LIST_FRAG_TAG)
                .commit();
         */
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container2, new RecyclerViewFragment(), LIST_FRAG_TAG)
                .commit();
    }
}