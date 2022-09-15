package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_INVENTORYNAME;
import static com.example.inventoryapp.GlobalConstants.db_max_code_length;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class ItemFragmentOnline extends OnlineFragment implements FirebaseHandler.OnlineFragmentBehavior{

    private final String TAG = "Inventory Item Activity Online";
    FloatingActionButton addition_fab;
    FloatingActionButton rename_fab;
    ItemRVAOnline invItem_rva;
    int rv_id = R.id.inventoryitemlist_view;
    RecyclerView rv;
    Activity cActivity;
    FloatingActionButton moreOptions_fab;
    Boolean isOpen_FABMenu;

    public ItemFragmentOnline() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            RenameAppBar(currentInventoryName);
        }
        cActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate menu
        requireActivity().addMenuProvider(new OnlineMenuProvider(this), getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        //inflate fragment layout
        return inflater.inflate(R.layout.frag_online_item, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        SetupFloatingActionButtons();
        SetupRecyclerView();
        SetupBottomNav();
        RenameAppBar(currentGroupName+"#"+currentInventoryName);
    }

    private void ToggleFABMenu(){
        isOpen_FABMenu = ExpandableFABDefaultBehavior(isOpen_FABMenu, moreOptions_fab,
                new FloatingActionButton[] {addition_fab, rename_fab}, getContext());
    }

    private void SetupFloatingActionButtons(){
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.inventoryitem_fab);
        addition_fab.setOnClickListener(view -> AddInventoryItem());
        rename_fab = (FloatingActionButton) getView().findViewById(R.id.fab_renameInventory);
        rename_fab.setOnClickListener(view -> RenameInventory());
        moreOptions_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_moreOptions);
        moreOptions_fab.setOnClickListener(view -> ToggleFABMenu());
        isOpen_FABMenu = addition_fab.isShown();
    }

    private void SetupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(rv_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(cActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        invItem_rva =new ItemRVAOnline(cActivity, rv, this);
        rv.setAdapter(invItem_rva);
    }

    private void AddInventoryItem(){
        new FirebaseHandler().AddInventoryItemToInventory(
                currentGroupID, currentInventoryID, new FirebaseHandler.InventoryItem(""), this);
    }

    private void RenameInventory(){
        Dialogs.RenameInventoryDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String newName = args[0];
                new FirebaseHandler().RenameInventory(currentGroupID, currentInventoryID, newName, FirebaseAuth.getInstance().getCurrentUser(), ItemFragmentOnline.this);
                RenameAppBar(newName);
                return true;
            }

            @Override
            public void cancelCallback() {}});
    }
}