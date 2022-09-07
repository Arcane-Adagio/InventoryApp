package com.example.inventoryapp;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class OfflineItemFragment extends Fragment {

    private static FloatingActionButton rv_fab;
    private String mCurrentInventory;
    EditText mNameChangeEditText;
    ImageButton mConfirmNameButton;
    ImageButton mCancelNameButton;
    LinearLayout mNameChangeLayout;
    public static final String KEY_INVENTORYNAME = "inventoryName";
    private static int rvID = R.id.inventoryitemlist_view;
    Activity cActivity;

    public OfflineItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            mCurrentInventory = this.getArguments().getString(KEY_INVENTORYNAME);
            Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(mCurrentInventory);
        }
        SetupItemRecyclerView();
        rv_fab = (FloatingActionButton) getView().findViewById(R.id.inventoryitem_fab);
        rv_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryItemRecyclerAdapter.GetItemRecyclerViewINSTANCE().AddItemToInventory();
            }
        });
        SetupNameChangeDialog();
    }

    private void RenameInventory(String newName){
        /* The background data must also be updated */
        int position = User.GetPositionOfInventory(mCurrentInventory);
        User.RenameInventory(mCurrentInventory, newName);
        mCurrentInventory = newName;
        InventoryItemRecyclerAdapter.GetItemRecyclerViewINSTANCE().UpdateCurrentInventoryName(mCurrentInventory);
        //InventoryRecyclerViewerAdapter.GetHomeRecyclerViewINSTANCE().notifyItemChanged(position);
        //OfflineInventoryFragment.recyclerView.getAdapter().notifyItemChanged(position);
        InventoryRecyclerViewerAdapter.GetHomeRecyclerViewINSTANCE().RenameInventory(position, newName);

        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(newName);
    }

    private void SetupItemRecyclerView(){
        RecyclerView recyclerView = getView().findViewById(rvID);
        InventoryItemRecyclerAdapter adapter =  InventoryItemRecyclerAdapter.ConstructItemRecyclerView(
                mCurrentInventory, User.GetInventoryItems(mCurrentInventory), cActivity, rvID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(cActivity));
    }

    private void SetupNameChangeDialog(){
        mCancelNameButton = (ImageButton) getView().findViewById(R.id.inv_btn_namechange_cancel);
        mCancelNameButton.setOnClickListener(view -> {
            mNameChangeEditText.setText("");
            mNameChangeLayout.setVisibility(View.GONE);
        });
        mConfirmNameButton = (ImageButton) getView().findViewById(R.id.inv_btn_namechange_confirm);
        mConfirmNameButton.setOnClickListener(view -> {
            RenameInventory(mNameChangeEditText.getText().toString());
            mNameChangeLayout.setVisibility(View.GONE);
        });
        mNameChangeEditText = (EditText) getView().findViewById(R.id.newInventoryNameEditText);
        mNameChangeEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                RenameInventory(mNameChangeEditText.getText().toString());
                mNameChangeLayout.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
        mNameChangeLayout = (LinearLayout) getView().findViewById(R.id.namechange_view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.inventory_appbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Handles behavior for when a menu option is selected */
        /* Handles behavior for when an appbar menu item is selected */
        if(MenuOptionsSelected(item))
            return true;
        else if (GlobalActions.DefaultMenuOptionSelection(item,cActivity, getActivity().getSupportFragmentManager()))
            return true;
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_offline_item, container, false);
    }
}