package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryActivityOnline extends AppCompatActivity {

    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mGroupsReference = mRootReference.child("Groups");
    RecyclerView rv;
    GroupRVAdapter inv_rva;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_homeonline);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SetupGroupRecyclerView();
    }

    private void SetupGroupRecyclerView(){
        rv=(RecyclerView) findViewById(R.id.recyclerview_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        inv_rva=new GroupRVAdapter(this);
        rv.setAdapter(inv_rva);
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    public class GroupRVAdapter extends RecyclerView.Adapter<InventoryActivityOnline.ViewHolder>{
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mGroupsReference = mRootReference.child("Groups");
        List<FirebaseHandler.Group> groupData = new ArrayList<FirebaseHandler.Group>();
        Context mContext;
        RecyclerView rv;

        public GroupRVAdapter(Context context){
            mContext = context;
            rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_group);
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
                    if(position != 1){
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
            holder.groupCode_tv.setText(groupData.get(position).getGroupID());
        }

        @Override
        public int getItemCount() {
            return groupData.size();
        }

        private FirebaseHandler.Group datasnapshotToGroupConverter(DataSnapshot snap){
            String groupName = Objects.requireNonNull(snap.child("groupName").getValue()).toString();
            FirebaseHandler.Group groupObj = new FirebaseHandler.Group(groupName);
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
        public EditText groupName_et;
        public TextView groupCode_tv;
        public ViewHolder(View view){
            super(view);
            groupName_et = (EditText) view.findViewById(R.id.edittext_groupName);
            groupCode_tv = (TextView) view.findViewById(R.id.textview_groupCode);
        }
    }

    public ValueEventListener CustomTextviewValueEventListener(TextView ui){
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ui.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }


    public void ShowGroupCreationDialog(View view){
        Toast.makeText(this, "nothing to see here", Toast.LENGTH_SHORT).show();
    }


}