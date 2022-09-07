package com.example.inventoryapp.online;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.MainActivity;
import com.example.inventoryapp.R;
import com.example.inventoryapp.offline.OfflineInventoryFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class OnlineGroupFragment extends Fragment {

    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    RecyclerView rv;
    GroupRVAdapter group_rva;
    FirebaseUser currentUser;
    FloatingActionButton addition_fab;
    Activity cActivity;
    private static final String APPBAR_TITLE_FOR_FRAGMENT = "Groups";

    public OnlineGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cActivity = getActivity();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.online_menu, menu);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_online_group, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.fab_group);
        addition_fab.setOnClickListener(view -> CreateGroupDialog());
        SetupGroupRecyclerView();
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(APPBAR_TITLE_FOR_FRAGMENT);
    }

    public class GroupRVAdapter extends RecyclerView.Adapter<ViewHolder>{
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mGroupsReference = mRootReference.child("Groups");
        List<FirebaseHandler.Group> groupData = new ArrayList<FirebaseHandler.Group>();
        Context mContext;
        Drawable delete_draw;
        Drawable exit_draw;
        RecyclerView rv;

        public GroupRVAdapter(Context context){
            mContext = context;
            rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_group);
            delete_draw = AppCompatResources.getDrawable(context, R.drawable.ic_delete_default);
            exit_draw = AppCompatResources.getDrawable(context, R.drawable.ic_exit_default);
            mGroupsReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    groupData.add(datasnapshotToGroupConverter(snapshot));
                    rv.scrollToPosition(groupData.size()-1); //todo: take out if annoying
                    GroupRVAdapter.this.notifyItemInserted(groupData.size()-1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String changedGroupID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedGroupID);
                    if(position != -1){
                        groupData.remove(position);
                        groupData.add(position, datasnapshotToGroupConverter(snapshot));
                        GroupRVAdapter.this.notifyItemChanged(position);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String changedGroupID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedGroupID);
                    if(position != 1){
                        groupData.remove(position);
                        GroupRVAdapter.this.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Toast.makeText(context, "onChildMove Not Implemented", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Database cancelled updating RecyclerView", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_group, parent, false);
            final ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.groupName_et.setText(groupData.get(position).getGroupName());
            holder.groupCode_tv.setText(groupData.get(position).getGroupCode());
            holder.edit_btn.setOnClickListener(view -> NavigateToInventoryFragment(
                    groupData.get(holder.getAdapterPosition()).getGroupID(),
                    groupData.get(holder.getAdapterPosition()).getGroupName()));
            holder.delete_btn.setOnClickListener(view ->
                    new FirebaseHandler().RemoveGroup(groupData.get(holder.getAdapterPosition()).getGroupID()));
            holder.delete_btn.setImageDrawable(
                    (Objects.equals(groupData.get(holder.getAdapterPosition()).getGroupOwner(), currentUser.getUid())) ? delete_draw : exit_draw
            );
        }

        @Override
        public int getItemCount() {
            return groupData.size();
        }

        private FirebaseHandler.Group datasnapshotToGroupConverter(DataSnapshot snap){
            String groupName = Objects.requireNonNull(snap.child("groupName").getValue()).toString();
            String groupCode = Objects.requireNonNull(snap.child("groupCode").getValue()).toString();
            String password = Objects.requireNonNull(snap.child("groupPasswordHashed").getValue()).toString();
            String owner = Objects.requireNonNull(snap.child("groupOwner").getValue()).toString();
            FirebaseHandler.Group groupObj = new FirebaseHandler.Group(groupName, groupCode, password, owner);
            groupObj.setGroupID(snap.getKey());
            return groupObj;
        }

        private int getPositionInRecyclerViewByID(String id){
            int position = -1;
            for (int i = 0; i<groupData.size(); i++){
                if(groupData.get(i).getGroupID().equals(id)){
                    position = i;
                    break;
                }
            }
            return position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView groupName_et;
        public TextView groupCode_tv;
        public ImageButton edit_btn;
        public ImageButton delete_btn;
        public ViewHolder(View view){
            super(view);
            groupName_et = (TextView) view.findViewById(R.id.edittext_groupName);
            groupCode_tv = (TextView) view.findViewById(R.id.textview_groupCode);
            edit_btn = (ImageButton) view.findViewById(R.id.group_edit_btn);
            delete_btn = (ImageButton) view.findViewById(R.id.group_delete_btn);
        }
    }

    public void CreateGroupDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.frag_creategroup);
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
                        new FirebaseHandler().AddGroup(
                                new FirebaseHandler.Group(nameText, codeText, passwordText, currentUser.getUid()));
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

    private void SetupGroupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(R.id.recyclerview_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(cActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        group_rva =new GroupRVAdapter(cActivity);
        rv.setAdapter(group_rva);
    }

    private void NavigateToInventoryFragmentOLD(String groupID, String groupName){
        Bundle bundle = new Bundle();
        bundle.putString("groupID", groupID);
        bundle.putString("groupName", groupName);
        OnlineInventoryFragment frag = new OnlineInventoryFragment();
        frag.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(MainActivity.fragmentContainerID, frag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void NavigateToInventoryFragment(String groupID, String groupName){
        Bundle bundle = new Bundle();
        bundle.putString("groupID", groupID);
        bundle.putString("groupName", groupName);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineGroupFragment_to_onlineInventoryFragment, bundle);
    }
}