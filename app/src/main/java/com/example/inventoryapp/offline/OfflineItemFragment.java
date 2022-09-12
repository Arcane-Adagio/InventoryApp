package com.example.inventoryapp.offline;

import static com.example.inventoryapp.GlobalConstants.FRAGMENT_ARG_INVENTORY_NAME;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
import android.widget.Toast;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class OfflineItemFragment extends OfflineFragment implements OfflineFragment.MenuCallback {

    private static FloatingActionButton rv_fab;
    private String mCurrentInventory;
    EditText mNameChangeEditText;
    ImageButton mConfirmNameButton;
    ImageButton mCancelNameButton;
    LinearLayout mNameChangeLayout;
    private static final int rvID = R.id.inventoryitemlist_view;
    Activity cActivity;

    public OfflineItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            mCurrentInventory = this.getArguments().getString(FRAGMENT_ARG_INVENTORY_NAME);
            Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(mCurrentInventory);
        }
        SetupItemRecyclerView();
        rv_fab = (FloatingActionButton) requireView().findViewById(R.id.inventoryitem_fab);
        rv_fab.setOnClickListener(view -> ItemRVAdapter.GetItemRecyclerViewINSTANCE().AddItemToInventory());
        SetupNameChangeDialog();
        SetupBottomNav();
    }

    private void RenameInventory(String newName){
        /* The background data must also be updated */
        int position = OfflineInventoryManager.GetPositionOfInventory(mCurrentInventory);
        OfflineInventoryManager.RenameInventory(mCurrentInventory, newName);
        mCurrentInventory = newName;
        ItemRVAdapter.UpdateCurrentInventoryName(mCurrentInventory);
        Objects.requireNonNull(InventoryRVAdapter.GetHomeRecyclerViewINSTANCE()).RenameInventory(position, newName);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(newName);
    }

    private void SetupItemRecyclerView(){
        RecyclerView recyclerView = requireView().findViewById(rvID);
        ItemRVAdapter adapter =  ItemRVAdapter.ConstructItemRecyclerView(
                mCurrentInventory, OfflineInventoryManager.GetInventoryItems(mCurrentInventory), cActivity, rvID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(cActivity));
    }

    private void SetupNameChangeDialog(){
        mCancelNameButton = (ImageButton) requireView().findViewById(R.id.inv_btn_namechange_cancel);
        mCancelNameButton.setOnClickListener(view -> {
            mNameChangeEditText.setText("");
            mNameChangeLayout.setVisibility(View.GONE);
        });
        mConfirmNameButton = (ImageButton) requireView().findViewById(R.id.inv_btn_namechange_confirm);
        mConfirmNameButton.setOnClickListener(view -> {
            RenameInventory(mNameChangeEditText.getText().toString());
            mNameChangeLayout.setVisibility(View.GONE);
        });
        mNameChangeEditText = (EditText) requireView().findViewById(R.id.newInventoryNameEditText);
        mNameChangeEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                RenameInventory(mNameChangeEditText.getText().toString());
                mNameChangeLayout.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
        mNameChangeLayout = (LinearLayout) requireView().findViewById(R.id.namechange_view);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requireActivity().addMenuProvider(new OfflineFragmentHandler(this, this),
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED);
        return inflater.inflate(R.layout.frag_offline_item, container, false);
    }

    @Override
    public int customMenuOptions() {
        return R.menu.inventory_appbar_menu;
    }

    @Override
    public boolean customOnItemSelected(MenuItem menuItem) {
        return MenuOptionsSelected(menuItem);
    }
}