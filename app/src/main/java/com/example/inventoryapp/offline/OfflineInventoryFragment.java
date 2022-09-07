package com.example.inventoryapp.offline;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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


public class OfflineInventoryFragment extends Fragment implements MenuProvider {
    Activity cActivity;
    private final String TAG = "Local Inventory Fragment";
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
        OfflineInventoryManager.LoadUserInventory(getActivity());
        setHasOptionsMenu(true);
    }

    public static Fragment GetFragmentReference(){
        return mThis;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_offline_inventory, container, false);
    }

    private void SetupInventoryRecyclerView(){
        recyclerView = requireView().findViewById(R.id.inventorylist_view);
        InventoryRVAdapter adapter = InventoryRVAdapter.ConstructHomeRecyclerViewIfNotCreated( OfflineInventoryManager.GetInventoryNames(), cActivity);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(cActivity));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        SetupInventoryRecyclerView();
        InventoryRVAdapter adapter = InventoryRVAdapter.GetHomeRecyclerViewINSTANCE();
        fab = (FloatingActionButton) requireView().findViewById(R.id.inventory_fab);
        if(adapter == null)
            Log.d(TAG, "onStart: unable to get adapter reference");
        else
            fab.setOnClickListener(view -> OfflineInventoryManager.AddInventoryAndNotifyAdapter(adapter));
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }



    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.home_appbar_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return GlobalActions.DefaultMenuOptionSelection(menuItem, cActivity, mThis);
    }
}