package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.online.OnlineFragment.currentUser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupRVAOnline extends RecyclerView.Adapter<GroupRVAOnline.ViewHolder>{
    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mGroupsReference = mRootReference.child("Groups");
    List<FirebaseHandler.Group> groupData = new ArrayList<FirebaseHandler.Group>();
    Context mContext;
    Drawable delete_draw;
    Drawable exit_draw;
    RecyclerView rv;
    OnlineFragment.SimpleCallback navigationCallback;

    public GroupRVAOnline(Context context, OnlineFragment.SimpleCallback navCallback){
        mContext = context;
        rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_group);
        delete_draw = AppCompatResources.getDrawable(context, R.drawable.ic_delete_default);
        exit_draw = AppCompatResources.getDrawable(context, R.drawable.ic_exit_default);
        navigationCallback = navCallback;
        Query query = FirebaseDatabase.getInstance().getReference("Groups")
                .orderByChild("groupCode")
                .equalTo("");

        mGroupsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(isNotAGroupMemberOf(snapshot))
                    return;
                groupData.add(datasnapshotToGroupConverter(snapshot));
                rv.scrollToPosition(groupData.size()-1); //todo: take out if annoying
                GroupRVAOnline.this.notifyItemInserted(groupData.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String changedGroupID = snapshot.getKey().toString();
                int position = getPositionInRecyclerViewByID(changedGroupID);
                if(isNotAGroupMemberOf(snapshot)){
                    if(position != OUT_OF_BOUNDS){
                        //user is no longer apart of a group
                        //so it should be removed from recycler view
                        groupData.remove(position);
                        GroupRVAOnline.this.notifyItemRemoved(position);
                    }
                }
                else{
                    if(position == OUT_OF_BOUNDS){
                        //user has joined the group, so
                        //the group needs to be displayed
                        groupData.add(datasnapshotToGroupConverter(snapshot));
                        GroupRVAOnline.this.notifyItemInserted(groupData.size()-1);
                    }
                    else {
                        //user is apart of the group and the group is displayed
                        //but the value needs to be updated
                        groupData.remove(position);
                        groupData.add(position, datasnapshotToGroupConverter(snapshot));
                        GroupRVAOnline.this.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String changedGroupID = snapshot.getKey().toString();
                int position = getPositionInRecyclerViewByID(changedGroupID);
                if(position != OUT_OF_BOUNDS){
                    groupData.remove(position);
                    GroupRVAOnline.this.notifyItemRemoved(position);
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

    private boolean isNotAGroupMemberOf(DataSnapshot snap){
        if(!snap.hasChild("groupOwner"))
            return true; // means its not a group object
        if(Objects.equals((String) snap.child("groupOwner").getValue(), currentUser.getUid()))
            return false;
        if(snap.hasChild("Members"))
            if(snap.child("Members").hasChild(currentUser.getUid())){
                return false;
            }
        return true;
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
        holder.edit_btn.setOnClickListener(view -> navigationCallback.CallableFunction(new String[] {
                groupData.get(holder.getAdapterPosition()).getGroupID(),
                groupData.get(holder.getAdapterPosition()).getGroupName()}));
        holder.delete_btn.setOnClickListener(view ->
                new FirebaseHandler().RemoveGroup(groupData.get(holder.getAdapterPosition())));
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
        int position = OUT_OF_BOUNDS;
        for (int i = 0; i<groupData.size(); i++){
            if(groupData.get(i).getGroupID().equals(id)){
                position = i;
                break;
            }
        }
        return position;
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
}
