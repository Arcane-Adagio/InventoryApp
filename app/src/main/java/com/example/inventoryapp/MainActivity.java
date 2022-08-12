package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment;
    private static final String EMPTY_FRAG_TAG = "EmptyRV";
    private static final String LIST_FRAG_TAG = "ListRV";
    private static boolean emptyList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //if(emptyList){
            //SetupInitFragment();
        //}
        //else
           // RecyclerViewFragment (savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.d("life", "HomeActivity: onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("life", "HomeActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("life", "HomeActivity: onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("life", "HomeActivity: onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d("life", "HomeActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d("life", "HomeActivity: onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_appbar_menu, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (GlobalActions.DefaultMenuOptionSelection(item,this, getSupportFragmentManager()))
            return true;
        return super.onOptionsItemSelected(item);
    }

    /*
    public void RecyclerViewFragment(Bundle savedInstanceState){
        if(savedInstanceState == null)
            fragment = new RecyclerViewFragment();
        else
            fragment = getSupportFragmentManager().findFragmentByTag(EMPTY_FRAG_TAG);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container, fragment, EMPTY_FRAG_TAG)
                .commit();
    }*/

    /*
    public void SetupInitFragment(){
        fragment = new InitFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container, fragment, EMPTY_FRAG_TAG)
                .commit();
    }
     */

    public void Testb(View view){
        Intent intent = new Intent(this, InventoryActivity.class);
        intent.putExtra("name","Sample Inventory");
        startActivity(intent);
    }
}