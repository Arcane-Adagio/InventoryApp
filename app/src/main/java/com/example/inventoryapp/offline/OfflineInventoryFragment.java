package com.example.inventoryapp.offline;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class OfflineInventoryFragment extends Fragment {
    static Activity cActivity;
    private final String TAG = "Inventory Fragment";
    FloatingActionButton fab;
    static RecyclerView recyclerView;
    private static Fragment mThis;

    public OfflineInventoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
        mThis = this;
        GlobalActions.LoadUserInventory(getActivity());
        setHasOptionsMenu(true);
    }

    public static Fragment GetFragmentReference(){
        return mThis;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_appbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* Handles behavior for when a menu option is selected */
        if (GlobalActions.DefaultMenuOptionSelection(item,cActivity, getActivity().getSupportFragmentManager()))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_offline_inventory, container, false);
    }

    private void SetupInventoryRecyclerView(){
        recyclerView = getView().findViewById(R.id.inventorylist_view);
        //RecyclerView recyclerView = requireView().findViewById(R.id.inventorylist_view);
        InventoryRVAdapter adapter = InventoryRVAdapter.ConstructHomeRecyclerViewIfNotCreated( User.GetInventoryNames(), cActivity);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(cActivity));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        SetupInventoryRecyclerView();
        fab = (FloatingActionButton) getView().findViewById(R.id.inventory_fab); //TODO replace with requireview
        fab.setOnClickListener(view -> InventoryRVAdapter.GetHomeRecyclerViewINSTANCE().AddInventory());
        super.onStart();
    }
}