package com.example.inventoryapp.online;

import android.app.Activity;
import android.os.Bundle;

import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

// This file handles the logic of the fragment containing the group recycler view

public class GroupFragmentOnline extends OnlineFragment{

    public final String TAG = "Online Group Fragment";
    RecyclerView rv;
    GroupRVAOnline group_rva;
    FloatingActionButton createGroup_fab;
    FloatingActionButton addGroup_fab;
    FloatingActionButton moreOptions_fab;
    Activity cActivity;
    Boolean fab_open;

    private static final String APPBAR_TITLE_FOR_FRAGMENT = "Groups";

    public GroupFragmentOnline() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate Menu
        requireActivity().addMenuProvider(new OnlineMenuProvider(this), getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return inflater.inflate(R.layout.frag_online_group, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        SetupFloatingActionButtons();
        SetupGroupRecyclerView();
        RenameAppBar(APPBAR_TITLE_FOR_FRAGMENT);
        SetupBottomNav();
    }

    public void SetupFloatingActionButtons(){
        createGroup_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_createGroup);
        createGroup_fab.setOnClickListener(view -> CreateGroup());
        addGroup_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_joinGroup);
        addGroup_fab.setOnClickListener(view -> JoinGroup());
        moreOptions_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_moreOptions);
        moreOptions_fab.setOnClickListener(view -> ToggleFABMenu());
        fab_open = createGroup_fab.isShown();
    }

    private void ToggleFABMenu(){
        fab_open = ExpandableFABDefaultBehavior(fab_open, moreOptions_fab,
              new FloatingActionButton[] {createGroup_fab, addGroup_fab}, getContext());
    }


    public void CreateGroup(){
        /* Prompts dialog and then calls firebase method if user proceeds */
        Dialogs.CreateGroupDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String name = args[0], code = args[1], passwordText = args[2];
                new FirebaseHandler().AddGroup(new FirebaseHandler.Group(name, code, passwordText, currentUser.getUid()));
                return true;
            }
            @Override
            public void cancelCallback() {

            }
        });
    }

    public void JoinGroup(){
        /* Prompts dialog and then calls firebase method if user proceeds */
        Dialogs.JoinGroupDialog(getContext(), new Dialogs.DialogListener() {
            @Override
            public boolean submissionCallback(String[] args) {
                String groupID = args[0];
                new FirebaseHandler().AddMemberToGroup(groupID, currentUser);
                return true;
            }
            @Override
            public void cancelCallback() {}
        });
    }

    private void SetupGroupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        group_rva =new GroupRVAOnline(getContext(), args -> NavigateToInventoryFragment(args[0], args[1]));
        rv.setAdapter(group_rva);
    }

    private void NavigateToInventoryFragment(String groupID, String groupName){
        currentGroupID = groupID;
        currentGroupName = groupName;
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineGroupFragment_to_onlineInventoryFragment);
    }
}