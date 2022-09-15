package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.FIREBASE_KEY_GROUPS;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_KEY_MEMBERS;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_GROUPCODE;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_GROUPNAME;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_GROUPOWNER;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_HASHEDPASSWORD;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupID;
import static com.example.inventoryapp.online.OnlineFragment.currentUser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.inventoryapp.data.Dialogs;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Adapter class that handles the data between the recycler view displaying group information and
* the firebase input
* */


public class GroupRVAOnline extends RecyclerView.Adapter<GroupRVAOnline.ViewHolder>{
    private static final String TAG = "Group Recyclerview Adapter";
    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mGroupsReference = mRootReference.child(FIREBASE_KEY_GROUPS);
    List<FirebaseHandler.Group> groupData = new ArrayList<FirebaseHandler.Group>();
    Context mContext;
    Drawable delete_draw;
    Drawable exit_draw;
    RecyclerView rv;
    OnlineFragment.SimpleCallback navigationCallback;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public GroupRVAOnline(Context context, OnlineFragment.SimpleCallback navCallback){
        mContext = context;
        rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_group);
        delete_draw = AppCompatResources.getDrawable(context, R.drawable.ic_delete_default);
        exit_draw = AppCompatResources.getDrawable(context, R.drawable.ic_exit_default);
        navigationCallback = navCallback;
        mGroupsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildAdded: an object was added to the firebase tree
                executor.execute(() -> { //Background work
                    boolean notForMe = isNotAGroupMemberOf(snapshot);
                    handler.post(() -> { //UI work
                        if(notForMe)
                            return;
                        groupData.add(datasnapshotToGroupConverter(snapshot));
                        rv.scrollToPosition(groupData.size()-1); //todo: take out if annoying
                        GroupRVAOnline.this.notifyItemInserted(groupData.size()-1);
                    });
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildChanged: an object or its sub-objects was changed in the firebase tree
                executor.execute(() -> { //Background work here
                    String changedGroupID = snapshot.getKey().toString();
                    boolean notForMe = isNotAGroupMemberOf(snapshot);
                    handler.post(() -> { //UI Thread work here
                        /* Position function is here because a race condition happens if it isn't */
                        int position = getPositionInRecyclerViewByID(changedGroupID);
                        if(notForMe){
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
                    });
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //onChildRemoved: an object was removed from the firebase tree
                executor.execute(() -> {
                    String changedGroupID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedGroupID);
                    handler.post(() -> {
                        if(position != OUT_OF_BOUNDS){
                            //if the object in the recycler view is no longer in firebase
                            //from the object from the recycler view
                            groupData.remove(position);
                            GroupRVAOnline.this.notifyItemRemoved(position);
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

    private boolean isNotAGroupMemberOf(DataSnapshot snap){
        /* Checks snap sub-tree to determine whether the user is listed as a member */
        if(!snap.hasChild(FIREBASE_SUBKEY_GROUPOWNER))
            return true; // means its not a group object
        if(Objects.equals((String) snap.child(FIREBASE_SUBKEY_GROUPOWNER).getValue(), currentUser.getUid()))
            return false;
        if(snap.hasChild(FIREBASE_KEY_MEMBERS))
            if(snap.child(FIREBASE_KEY_MEMBERS).hasChild(currentUser.getUid())){
                return false;
            }
        return true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /* links the xml file */
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_group, parent, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* Connects the data to the view based on position every time a tile is reconstructed */
        holder.groupName_et.setText(groupData.get(position).getGroupName());
        holder.groupCode_tv.setText(groupData.get(position).getGroupCode());
        holder.edit_btn.setOnClickListener(view -> navigationCallback.CallableFunction(new String[] {
                groupData.get(holder.getAdapterPosition()).getGroupID(),
                groupData.get(holder.getAdapterPosition()).getGroupName()}));
        holder.delete_btn.setOnClickListener(view -> {
            try {
                //potential runtime exception if user presses button too fast
                //technically, it's no longer an issue since a dialog prevents rapid deletion,
                //but im not taking any chances
                Dialogs.AreYouSureDialog(view.getContext(), new Dialogs.DialogListener() {
                    @Override
                    public boolean submissionCallback(String[] args) {
                        FirebaseHandler.RemoveGroup(groupData.get(holder.getAdapterPosition()));
                        return true;
                    }

                    @Override
                    public void cancelCallback() {

                    }
                });
            }
            catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e.getMessage());
            }
        });
        holder.delete_btn.setImageDrawable(
                (Objects.equals(groupData.get(holder.getAdapterPosition()).getGroupOwner(), currentUser.getUid())) ? delete_draw : exit_draw
        );
    }

    @Override
    public int getItemCount() {
        return groupData.size();
    }

    private FirebaseHandler.Group datasnapshotToGroupConverter(DataSnapshot snap){
        /* takes a firebase dataSnapshot and converts it to a data object for the recycler view */
        String groupName = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_GROUPNAME).getValue()).toString();
        String groupCode = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_GROUPCODE).getValue()).toString();
        String password = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_HASHEDPASSWORD).getValue()).toString();
        String owner = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_GROUPOWNER).getValue()).toString();
        FirebaseHandler.Group groupObj = new FirebaseHandler.Group(groupName, groupCode, password, owner);
        groupObj.setGroupID(snap.getKey());
        return groupObj;
    }

    private int getPositionInRecyclerViewByID(String id){
        /* returns the position of the tile in the recyclerview based on the id given */
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
        /* Data container for the recyclerview */
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
