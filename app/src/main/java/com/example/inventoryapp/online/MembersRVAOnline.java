package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.FIREBASE_KEY_GROUPS;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_MEMBERS;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupID;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupOwner;
import static com.example.inventoryapp.online.OnlineFragment.currentUser;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MembersRVAOnline extends RecyclerView.Adapter<MembersRVAOnline.ViewHolder>{
    private static final String TAG = "Group Recyclerview Adapter";
    //Firebase Variables
    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mGroupsReference = mRootReference.child(FIREBASE_KEY_GROUPS);
    DatabaseReference groupRef = mGroupsReference.child(currentGroupID);
    DatabaseReference membersRef = groupRef.child(FIREBASE_SUBKEY_MEMBERS);
    boolean isGroupOwner = false;


    //Data
    List<FirebaseHandler.User> memberData = new ArrayList<>();
    Context mContext;
    RecyclerView rv;
    //Interface For call
    OnlineFragment.SimpleCallback deletionBtnCallback;
    //For multithreading
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public MembersRVAOnline(Context context, OnlineFragment.SimpleCallback deletionCallback){
        mContext = context;
        deletionBtnCallback = deletionCallback;
        isGroupOwner = currentGroupOwner.equals(currentUser.getUid());
        rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_members);
        membersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildAdded: an object was added to the firebase tree
                memberData.add(datasnapshotToUserConverter(snapshot));
                rv.scrollToPosition(memberData.size()-1); //todo: take out if annoying
                MembersRVAOnline.this.notifyItemInserted(memberData.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildChanged: an object or its sub-objects was changed in the firebase tree
                executor.execute(() -> { // done on background thread
                    String changedMemberID = snapshot.getKey().toString();
                    /* Position function is here because a race condition happens if it isn't */
                    int position = getPositionInRecyclerViewByID(changedMemberID);
                    handler.post(() -> { // done on ui thread
                        if(position != OUT_OF_BOUNDS){
                            //if a displayed object in firebase has been changed,
                            //remove the displayed object from the recycler view
                            //and display the new object that is in firebase
                            memberData.remove(position);
                            memberData.add(position, datasnapshotToUserConverter(snapshot));
                            MembersRVAOnline.this.notifyItemChanged(position);
                        }
                    });
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //onChildRemoved: an object was removed from the firebase tree
                executor.execute(() -> {
                    String changedMemberID = snapshot.getKey().toString();
                    handler.post(() -> {
                        int position = getPositionInRecyclerViewByID(changedMemberID);
                        if(position != OUT_OF_BOUNDS){
                            //if the object in the recycler view is no longer in firebase
                            //from the object from the recycler view
                            memberData.remove(position);
                            MembersRVAOnline.this.notifyItemRemoved(position);
                        }
                    });
                });
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Toast.makeText(context, "onChildMove Not Implemented", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(context, "Database cancelled updating RecyclerView", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /* links the xml file */
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_user, parent, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* Connects the data to the view based on position every time a tile is reconstructed */
        holder.userName_tv.setText(memberData.get(position).getUserDisplayName());
        holder.userID_tv.setText(memberData.get(position).getUserID());
        if(isGroupOwner){ //How the group owner views the members list
            if(!holder.userID_tv.getText().toString().equals(currentUser.getUid())){
                /* Only the group owner should be able to remove a member from the group.
                 * The group owner should not be able to remove themselves */
                holder.remove_btn.setVisibility(View.VISIBLE);
            }
            else{
                holder.remove_btn.setVisibility(View.GONE);
                holder.ownerStatus.setVisibility(View.VISIBLE);
            }
        }
        else { // How everyone else views the members list
            holder.remove_btn.setVisibility(View.GONE); //only the owner can kick people
            if(holder.userID_tv.getText().toString().equals(currentUser.getUid())){
                //the tile for the current signed in user is shown
            }
            else if (holder.userID_tv.getText().toString().equals(currentGroupOwner)){
                //the tile for the group owner is shown
                holder.ownerStatus.setVisibility(View.VISIBLE);
            }
        }
        holder.remove_btn.setOnClickListener(view -> {
            try {
                //potential runtime exception if user presses button too fast
                //technically, it's no longer an issue since a dialog prevents rapid deletion,
                //but im not taking any chances
                deletionBtnCallback.CallableFunction(new String[] {
                        holder.userName_tv.getText().toString(),
                        holder.userID_tv.getText().toString(),
                });
            }
            catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberData.size();
    }


    private FirebaseHandler.User datasnapshotToUserConverter(DataSnapshot snap){
        /* takes a firebase dataSnapshot and converts it to a data object for the recycler view */
        String userID = snap.getKey();
        String userDisplayName = snap.getValue().toString();
        return new FirebaseHandler.User(userID, userDisplayName);
    }

    private int getPositionInRecyclerViewByID(String id){
        /* returns the position of the tile in the recyclerview based on the id given */
        int position = OUT_OF_BOUNDS;
        for (int i = 0; i< memberData.size(); i++){
            if(memberData.get(i).getUserID().equals(id)){
                position = i;
                break;
            }
        }
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        /* Data container for the recyclerview */
        public TextView userName_tv;
        public TextView userID_tv;
        public ImageView ownerStatus;
        public ImageButton remove_btn;
        public ViewHolder(View view){
            super(view);
            userName_tv = (TextView) view.findViewById(R.id.userName_tv);
            userID_tv = (TextView) view.findViewById(R.id.userID_tv);
            ownerStatus = (ImageView) view.findViewById(R.id.icon_groupOwner);
            remove_btn = (ImageButton) view.findViewById(R.id.inv_remove_btn);
        }
    }
}
