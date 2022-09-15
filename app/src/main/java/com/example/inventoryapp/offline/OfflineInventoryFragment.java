package com.example.inventoryapp.offline;

import static com.example.inventoryapp.GlobalConstants.FRAGMENT_ARG_INVENTORY_NAME;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPNAME;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.example.inventoryapp.online.FirebaseHandler;
import com.example.inventoryapp.online.InventoryFragmentOnline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/* this file contains the logic for the fragment which displays the inventory recycler view */

public class OfflineInventoryFragment extends OfflineFragment {
    private final String TAG = "Local Inventory Fragment";
    FloatingActionButton fab;
    static RecyclerView recyclerView;
    private InventoryRVAdapter adapter;

    public OfflineInventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfflineInventoryManager.LoadUserInventory(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().addMenuProvider(new OfflineFragmentHandler(this), getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_offline_inventory, container, false);
    }

    private void SetupInventoryRecyclerView(){
        recyclerView = requireView().findViewById(R.id.inventorylist_view);
        adapter = InventoryRVAdapter.ConstructHomeRecyclerViewIfNotCreated
                (OfflineInventoryManager.GetInventoryNames(), args -> NavigateToItemFragment(args[0]));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void ResetInventoryRecyclerView(){
        recyclerView = requireView().findViewById(R.id.inventorylist_view);
        adapter = InventoryRVAdapter.ReconstructRecyclerView( OfflineInventoryManager.GetInventoryNames());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        SetupInventoryRecyclerView();
        fab = (FloatingActionButton) requireView().findViewById(R.id.inventory_fab);
        if(adapter == null)
            Log.d(TAG, "onStart: unable to get adapter reference");
        else
            fab.setOnClickListener(view ->{ AddInventory(); });
        RenameAppBar(getString(R.string.title_offlineinventory));
        SetupBottomNav();
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    private void AddInventory(){
        Dialogs.CreateInventoryDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String proposedName = args[0];
                try{
                    OfflineInventoryManager.AddInventoryAndNotifyAdapter(adapter, proposedName, getContext());
                    if(adapter.getItemCount() == 0)
                        ResetInventoryRecyclerView();
                    return true;
                }
                catch (Exception e){
                    Log.d(TAG, "submissionCallabck: "+e.getMessage());
                    return false;
                }
            }

            @Override
            public void cancelCallback() {

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void NavigateToItemFragment(String inventoryName){
        Bundle bundle = new Bundle();
        NavController navController = NavHostFragment.findNavController(this);
        bundle.putString(FRAGMENT_ARG_INVENTORY_NAME, inventoryName);
        navController.navigate(R.id.action_offlineInventoryFragment_to_offlineItemFragment, bundle);
    }

}