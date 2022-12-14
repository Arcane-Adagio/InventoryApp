package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPNAME;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.PopUpToBuilder;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

// This file handles the logic of the fragment containing the inventory recycler view

public class InventoryFragmentOnline extends OnlineFragment {

    private final String TAG = "Inventory Activity Online";
    RecyclerView rv;
    InventoryRVAOnline inv_rva;
    FloatingActionButton addition_fab;
    FloatingActionButton edit_fab;
    FloatingActionButton moreOptions_fab;
    Boolean isOpen_FABMenu;

    public InventoryFragmentOnline() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the menu
        requireActivity().addMenuProvider(new OnlineMenuProvider(this), getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_online_inventory, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        SetupFloatingActionButtons();
        RenameAppBar(currentGroupName);
        SetupRecyclerView();
        SetupBottomNav();
    }

    public void SetupFloatingActionButtons(){
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.fab_inventory);
        addition_fab.setOnClickListener(view -> AddInventory());
        edit_fab = (FloatingActionButton) getView().findViewById(R.id.fab_renameGroup);
        edit_fab.setOnClickListener(view -> RenameGroup());
        moreOptions_fab = (FloatingActionButton) getView().findViewById(R.id.fab_moreOptions);
        moreOptions_fab.setOnClickListener(view -> ToggleFABMenu());
        isOpen_FABMenu = addition_fab.isShown();
    }

    private void ToggleFABMenu(){
        /* Minimizes or Expands sub-menu of FABs */
        // function uses:
        //     a boolean to keep track of the fragment's menu status
        // function takes:
        //     the main FAB and a sub menu of FABs to be displayed

        //only the owner should be allow to rename the inventory with the edit FAB
        FloatingActionButton[] submenu = (currentGroupOwner.equals(currentUser.getUid())) ?
                new FloatingActionButton[] {addition_fab, edit_fab} :
                new FloatingActionButton[] {addition_fab} ;
        isOpen_FABMenu = ExpandableFABDefaultBehavior(isOpen_FABMenu, moreOptions_fab, submenu, getContext());
    }

    private void NavigateToItemFragment(String inventoryID, String inventoryName){
        currentInventoryName = inventoryName;
        currentInventoryID = inventoryID;
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineInventoryFragment_to_onlineItemFragment);
    }


    private void SetupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_inventory);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        inv_rva =new InventoryRVAOnline(getActivity(), args -> NavigateToItemFragment(args[0], args[1]));
        rv.setAdapter(inv_rva);
    }

    private void AddInventory(){
        /* Prompts dialog and then calls firebase method if user proceeds */
        Dialogs.CreateInventoryDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String proposedName = args[0];
                try{
                    if(proposedName != null && !proposedName.isEmpty())
                        FirebaseHandler.AddInventoryToGroup(currentGroupID,
                                new FirebaseHandler.Inventory(proposedName), InventoryFragmentOnline.this);
                    return true;
                }
                catch (Exception e){
                    Log.d(TAG, "submissionCallabck: "+e.getMessage());
                    return false;
                }
            }

            @Override
            public void cancelCallback() {}});
    }

    private void RenameGroup(){
        /* Prompts dialog and then calls firebase method if user proceeds */
        Dialogs.RenameGroupDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String newName = args[0];
                FirebaseHandler.RenameGroup(currentGroupID, newName, FirebaseAuth.getInstance().getCurrentUser(), InventoryFragmentOnline.this);
                return true;
            }

            @Override
            public void cancelCallback() {}});
    }
}