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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


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
        edit_fab.setOnClickListener(view -> ShowRenameGroupDialog());
        moreOptions_fab = (FloatingActionButton) getView().findViewById(R.id.fab_moreOptions);
        moreOptions_fab.setOnClickListener(view -> ToggleFABMenu());
        isOpen_FABMenu = addition_fab.isShown();
    }

    private void ToggleFABMenu(){
        isOpen_FABMenu = GlobalActions.ExpandableFABDefaultBehavior(isOpen_FABMenu, moreOptions_fab,
                new FloatingActionButton[] {addition_fab, edit_fab}, getContext());
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
        Dialogs.CreateInventoryDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallabck(String[] args) {
                String proposedName = args[0];
                try{
                    if(proposedName != null && !proposedName.isEmpty())
                        new FirebaseHandler().AddInventoryToGroup(currentGroupID,
                                new FirebaseHandler.Inventory(proposedName), InventoryFragmentOnline.this);
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


    public void ShowRenameGroupDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dlog_renamegroup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameGroup_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameGroup);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            RenameGroupBehavior(nameEditText, dialog);
        });
        cancelBtn.setOnClickListener(v -> {
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void RenameGroupBehavior(EditText nameEditText, Dialog dialog){
        if(nameEditText.getText() == null)
            return;
        if(nameEditText.getText().toString().isEmpty()){
            nameEditText.setHint("Please Enter A Valid Name");
            nameEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        String newName = nameEditText.getText().toString();
        new FirebaseHandler().RenameGroup(currentGroupID, newName, FirebaseAuth.getInstance().getCurrentUser(), this);
        RenameAppBar(newName);
        dialog.dismiss();
    }
}