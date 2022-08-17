package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;
import java.util.concurrent.Callable;

public class InventoryItemActivity extends AppCompatActivity {

    private static FloatingActionButton rv_fab;
    private String mCurrentInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_inventoryitem);
    }



    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString("inventoryName") != null){
                mCurrentInventory = extras.getString("inventoryName");
                initrv(mCurrentInventory);
                Objects.requireNonNull(getSupportActionBar()).setTitle(mCurrentInventory);
            }
        }
        rv_fab = (FloatingActionButton) findViewById(R.id.inventoryitem_fab);
        rv_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryItemRecyclerAdapter.GetItemRecyclerViewINSTANCE().AddInventory2();
            }
        });
    }

    private void initrv(String inventoryName){
        RecyclerView recyclerView = findViewById(R.id.inventoryitemlist_view);
        InventoryItemRecyclerAdapter adapter = InventoryItemRecyclerAdapter.ConstructItemRecyclerView(inventoryName, User.GetInventoryItems(inventoryName), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private boolean MenuOptionsSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_inv_edit_title:
                String oldInventoryName = mCurrentInventory;
                //update user class and recycler view
                RenameInventory();
                getSupportActionBar().setTitle(mCurrentInventory);
                return true;
            case R.id.menu_inv_delete:
                User.RemoveInventory(mCurrentInventory);
                GlobalActions.NavigateToActivity(this, MainActivity.class);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inventory_appbar_menu, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(MenuOptionsSelected(item))
            return true;
        else if (GlobalActions.DefaultMenuOptionSelection(item,this, getSupportFragmentManager()))
            return true;
        else
            return super.onOptionsItemSelected(item);
    }

    private void RenameInventory(){
        // Displays a form for a user to Title and navigate to a newly created Inventory
        int layout = R.layout.dialog_inventorycreation;
        String newInventoryName;
        DialogFragment newFragment = GlobalActions.MyAlertDialogFragmentWithCustomLayout.newInstance(layout,
                new Callable<Void>() {
                    @Override
                    public Void call() {
                        //get the context of the dialog view, and use that to find the edit text
                        View view = getLayoutInflater().inflate(layout,null);
                        final EditText eT = (EditText) view.findViewById(R.id.newInventoryNameEditText);
                        mCurrentInventory = eT.getText().toString();
                        return null;
                    }
                });
        newFragment.show(getSupportFragmentManager(), "dialog");
    }


    public static class MyAlertDialogFragmentWithCustomLayout extends DialogFragment {

        public static GlobalActions.MyAlertDialogFragmentWithCustomLayout newInstance(int layout, Callable<Void> positiveFunc){
            GlobalActions.MyAlertDialogFragmentWithCustomLayout frag = new GlobalActions.MyAlertDialogFragmentWithCustomLayout();
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
                    .setPositiveButton("Set", (dialogInterface, i) -> {
                        try {

                        } catch (Exception e) {
                            Log.d("error", "onCreateDialog: "+e.toString());
                            e.printStackTrace();
                        }
                    }).setNegativeButton("cancel", (dialogInterface, i) -> {
                        //do nothing
                    }).create();
        }
    }

}