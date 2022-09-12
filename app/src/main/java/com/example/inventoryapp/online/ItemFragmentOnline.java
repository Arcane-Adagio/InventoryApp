package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_INVENTORYNAME;
import static com.example.inventoryapp.GlobalConstants.db_max_code_length;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
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

    public static String getCurrentGroupID(){
        /* possible idea to automatically go back to the groups page when
        * group is deleted.
        * Another idea is using the lifecycle
        * Another idea is a static enum in groups, which keeps track of the fragment in view */
        return currentGroupID;
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
    }

    private void ToggleFABMenu(){
        isOpen_FABMenu = GlobalActions.ExpandableFABDefaultBehavior(isOpen_FABMenu, moreOptions_fab,
                new FloatingActionButton[] {addition_fab, rename_fab}, getContext());
    }

    private void SetupFloatingActionButtons(){
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.inventoryitem_fab);
        addition_fab.setOnClickListener(view -> AddInventoryItem());
        rename_fab = (FloatingActionButton) getView().findViewById(R.id.fab_renameInventory);
        rename_fab.setOnClickListener(view -> ShowRenameInventoryDialog());
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


    public void ShowRenameInventoryDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.dlog_renameinventory);
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameInventory);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            RenameInventoryBehavior(nameEditText, dialog);
        });
        cancelBtn.setOnClickListener(v -> {
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void RenameInventoryBehavior(EditText nameEditText, Dialog dialog){
        if(nameEditText.getText() == null)
            return;
        if(nameEditText.getText().toString().isEmpty()){
            nameEditText.setHint("Please Enter A Valid Name");
            nameEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        String newName = nameEditText.getText().toString();
        new FirebaseHandler().RenameInventory(currentGroupID, currentInventoryID, newName, FirebaseAuth.getInstance().getCurrentUser(), this);
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(newName);
        dialog.dismiss();
    }
}