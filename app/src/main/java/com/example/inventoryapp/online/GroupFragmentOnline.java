package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
        createGroup_fab.setOnClickListener(view -> CreateGroupDialog());
        addGroup_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_joinGroup);
        addGroup_fab.setOnClickListener(view -> JoinGroupDialog());
        moreOptions_fab = (FloatingActionButton) requireView().findViewById(R.id.fab_moreOptions);
        moreOptions_fab.setOnClickListener(view -> ToggleFABMenu());
        fab_open = createGroup_fab.isShown();
    }

    private void ToggleFABMenu(){
        fab_open = GlobalActions.ExpandableFABDefaultBehavior(fab_open, moreOptions_fab,
              new FloatingActionButton[] {createGroup_fab, addGroup_fab}, getContext());
    }


    public void CreateGroupDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.frag_creategroup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.creategroup_submit_Btn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.creategroup_cancel_Btn);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_groupName);
        EditText passwordEditText = (EditText)dialog.findViewById(R.id.edittext_groupPassword);
        EditText codeEditText = (EditText)dialog.findViewById(R.id.edittext_groupCode);

        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_groupname_length) });
        passwordEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_password_length) });
        codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });

        //when focus has been lost, check if code is valid
        codeEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if(!hasFocus){
                Query query = FirebaseDatabase.getInstance().getReference("Groups")
                        .orderByChild("groupCode")
                        .equalTo(codeEditText.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long queryResultCount = snapshot.getChildrenCount();
                        if(queryResultCount == 0)
                            codeEditText.getBackground().clearColorFilter();
                        else {
                            codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                            codeEditText.setText("");
                            codeEditText.setHint("Group Code Already Taken");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        submitBtn.setOnClickListener(v -> {
            String nameText = nameEditText.getText().toString();
            String passwordText = passwordEditText.getText().toString();
            String codeText = codeEditText.getText().toString();
            if(nameText.equals("") || passwordText.equals("") || codeText.equals(""))
                return;
            Query query = FirebaseDatabase.getInstance().getReference("Groups")
                    .orderByChild("groupCode")
                    .equalTo(codeEditText.getText().toString());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long queryResultCount = snapshot.getChildrenCount();
                    if(queryResultCount == 0){
                        CreateOnlineGroup(nameText, codeText, passwordText);
                        dialog.dismiss();
                    }
                    else {
                        codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        codeEditText.setText("");
                        codeEditText.setHint("Group Code Already Taken");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        cancelBtn.setOnClickListener(v -> {
            codeEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void JoinGroupDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.dlog_addgroup);
        //very important line - removes background to allow corner
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_joinGroup_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_joinGroup_cancel);
        EditText passwordEditText = (EditText) dialog.findViewById(R.id.edittext_groupPassword);
        EditText codeEditText = (EditText)dialog.findViewById(R.id.edittext_groupCode);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(GlobalConstants.db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
        AttemptToJoinGroup(codeEditText.getText().toString(), passwordEditText, dialog);
        });
        cancelBtn.setOnClickListener(v -> {
            codeEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void AttemptToJoinGroup(String gCode, EditText passwordEditText, Dialog dialog){
        if(gCode.equals("") || passwordEditText.getText().toString().isEmpty())
            return;
        int givenPasswordHashed = passwordEditText.getText().toString().hashCode();
        Query query = FirebaseDatabase.getInstance().getReference("Groups")
                .orderByChild("groupCode")
                .equalTo(gCode);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 0){
                    passwordEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    passwordEditText.setText("");
                    passwordEditText.setHint("Group Does Not Exist");
                    return;
                }
                for(DataSnapshot snap : snapshot.getChildren())
                    if(snap.hasChild("groupPasswordHashed")){
                        if(snap.child("groupPasswordHashed").getValue().equals(String.valueOf(givenPasswordHashed))){
                            new FirebaseHandler().AddMemberToGroup(snap.getKey(), currentUser);
                            Toast.makeText(cActivity, "joining group", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            break;
                        }
                        else {
                            passwordEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                            passwordEditText.setText("");
                            passwordEditText.setHint("Password is incorrect");
                            break;
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void CreateOnlineGroup(String name, String code, String passwordText){
        new FirebaseHandler().AddGroup(
                new FirebaseHandler.Group(name, code, passwordText, currentUser.getUid()));
    }


    private void SetupGroupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(cActivity);
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