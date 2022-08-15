package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment;
    private static final String EMPTY_FRAG_TAG = "EmptyRV";
    private static final String LIST_FRAG_TAG = "ListRV";
    private static boolean emptyList = true;
    EditText dialogEditText;

    //vars
    private ArrayList<String> mNames = new ArrayList<>(); //passed to adapter
    private ArrayList<String> mImageUrls = new ArrayList<>(); //same

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //if(emptyList)
            //SetupInitFragment();
        //}
        //else
           // RecyclerViewFragment (savedInstanceState);
        //initImageBitmaps();
    }

    private void Add(View view){

    }

    private void initImageBitmaps(){
        mImageUrls.add("https://c1.staticflickr.com/5/4636/25316407448_de5fbf183d_o.jpg");
        mNames.add("Havasu Falls");

        mImageUrls.add("https://i.redd.it/tpsnoz5bzo501.jpg");
        mNames.add("Trondheim");

        mImageUrls.add("https://i.redd.it/qn7f9oqu7o501.jpg");
        mNames.add("Portugal");

        mImageUrls.add("https://i.redd.it/j6myfqglup501.jpg");
        mNames.add("Rocky Mountain National Park");


        mImageUrls.add("https://i.redd.it/0h2gm1ix6p501.jpg");
        mNames.add("Mahahual");

        mImageUrls.add("https://i.redd.it/k98uzl68eh501.jpg");
        mNames.add("Frozen Lake");


        mImageUrls.add("https://i.redd.it/glin0nwndo501.jpg");
        mNames.add("White Sands Desert");

        mImageUrls.add("https://i.redd.it/obx4zydshg601.jpg");
        mNames.add("Austrailia");

        mImageUrls.add("https://i.imgur.com/ZcLLrkY.jpg");
        mNames.add("Washington");

        mImageUrls.add("https://c1.staticflickr.com/5/4636/25316407448_de5fbf183d_o.jpg");
        mNames.add("Havasu Falls");

        mImageUrls.add("https://i.redd.it/tpsnoz5bzo501.jpg");
        mNames.add("Trondheim");

        mImageUrls.add("https://i.redd.it/qn7f9oqu7o501.jpg");
        mNames.add("Portugal");

        mImageUrls.add("https://i.redd.it/j6myfqglup501.jpg");
        mNames.add("Rocky Mountain National Park");


        mImageUrls.add("https://i.redd.it/0h2gm1ix6p501.jpg");
        mNames.add("Mahahual");

        mImageUrls.add("https://i.redd.it/k98uzl68eh501.jpg");
        mNames.add("Frozen Lake");


        mImageUrls.add("https://i.redd.it/glin0nwndo501.jpg");
        mNames.add("White Sands Desert");

        mImageUrls.add("https://i.redd.it/obx4zydshg601.jpg");
        mNames.add("Austrailia");

        mImageUrls.add("https://i.imgur.com/ZcLLrkY.jpg");
        mNames.add("Washington");

        initRecyclerView();
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.inventorylist_view);
        InvLstRecyclerViewAdapter adapter = new InvLstRecyclerViewAdapter( mNames, mImageUrls, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    public void SetupInitFragment(){
        fragment = new InitFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rv_container, fragment, EMPTY_FRAG_TAG)
                .commit();
    }

    public void Testb(View view){
        dialogEditText = (EditText) view.findViewById(R.id.newInventoryNameEditText);
        String inventoryName = String.valueOf(dialogEditText.getText());
        Toast.makeText(this, inventoryName, Toast.LENGTH_SHORT).show();
        //use the text entered as the header for a new activity
        Intent intent = new Intent(this, InventoryActivity.class);
        Toast.makeText(this, inventoryName, Toast.LENGTH_SHORT).show();
        intent.putExtra("name","Sample Inventory");
        startActivity(intent);
    }

}