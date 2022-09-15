package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_INVENTORYNAME;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupID;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Adapter class that handles the data between the recycler view displaying inventory information and
 * the firebase input
 * */

public class InventoryRVAOnline extends RecyclerView.Adapter<InventoryRVAOnline.ViewHolder>{
    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    public final String TAG = "Inventory Recyclerview Adaptor - Online";
    DatabaseReference groupsRef = mRootReference.child("Groups");
    DatabaseReference groupRef = groupsRef.child(currentGroupID);
    DatabaseReference mInventoriesReference = groupRef.child("Inventories");
    List<FirebaseHandler.Inventory> inventoryData = new ArrayList<FirebaseHandler.Inventory>();
    Context mContext;
    RecyclerView rv;
    OnlineFragment.SimpleCallback mCallback;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public InventoryRVAOnline(Context context, OnlineFragment.SimpleCallback sCallback){
        mContext = context;
        mCallback = sCallback;
        rv = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recyclerview_inventory);
        mInventoriesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildAdded: an object was added to the firebase tree
                inventoryData.add(datasnapshotToInventoryConverter(snapshot));
                rv.scrollToPosition(inventoryData.size()-1); //todo: take out if annoying
                InventoryRVAOnline.this.notifyItemInserted(inventoryData.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildChanged: an object or its sub-objects was changed in the firebase tree
                executor.execute(() -> { // done on background thread
                    String changedInventoryID = snapshot.getKey().toString();
                    /* Position function is here because a race condition happens if it isn't */
                    int position = getPositionInRecyclerViewByID(changedInventoryID);
                    handler.post(() -> { // done on ui thread
                        if(position != OUT_OF_BOUNDS){
                            //if a displayed object in firebase has been changed,
                            //remove the displayed object from the recycler view
                            //and display the new object that is in firebase
                            inventoryData.remove(position);
                            inventoryData.add(position, datasnapshotToInventoryConverter(snapshot));
                            InventoryRVAOnline.this.notifyItemChanged(position);
                        }
                    });
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //onChildRemoved: an object was removed from the firebase tree
                executor.execute(() -> {
                    String changedInventoryID = snapshot.getKey().toString();
                    handler.post(() -> {
                        int position = getPositionInRecyclerViewByID(changedInventoryID);
                        if(position != OUT_OF_BOUNDS){
                            //if the object in the recycler view is no longer in firebase
                            //from the object from the recycler view
                            inventoryData.remove(position);
                            InventoryRVAOnline.this.notifyItemRemoved(position);
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
        //links the xml file
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory, parent, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* Connects the data to the view based on position every time a tile is reconstructed */
        holder.inventoryName.setText(inventoryData.get(position).getInventoryName());
        holder.editBtn.setOnClickListener(view -> mCallback.CallableFunction(new String[]{
                inventoryData.get(holder.getAdapterPosition()).getInventoryID(),
                inventoryData.get(holder.getAdapterPosition()).getInventoryName()}
        ));
        holder.deleteBtn.setOnClickListener(view -> {
            try{ //potential runtime exception if user presses button too fast
                String inventoryID = inventoryData.get(holder.getAdapterPosition()).getInventoryID();
                FirebaseHandler.RemoveInventoryFromGroup(currentGroupID, inventoryID);
            }
            catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryData.size();
    }

    private FirebaseHandler.Inventory datasnapshotToInventoryConverter(DataSnapshot snap){
        /* takes a firebase dataSnapshot and converts it to a data object for the recycler view */
        if(!snap.hasChild(FIREBASE_SUBKEY_INVENTORYNAME)){
            Log.d(TAG, "datasnapshotToInventoryConverter: no child");
            return null;
        }
        String inventoryName = Objects.requireNonNull(snap.child("inventoryName").getValue()).toString();
        FirebaseHandler.Inventory inventoryObj = new FirebaseHandler.Inventory(inventoryName);
        inventoryObj.setInventoryID(snap.getKey());
        return inventoryObj;
    }

    private int getPositionInRecyclerViewByID(String id){
        /* returns the position of the tile in the recyclerview based on the id given */
        int position = GlobalConstants.OUT_OF_BOUNDS;
        for (int i = 0; i< inventoryData.size(); i++){
            if(inventoryData.get(i).getInventoryID().equals(id)){
                position = i;
                break;
            }
        }
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        /* Data container for the recyclerview */
        TextView inventoryName;
        LinearLayout parentLayout;
        ImageButton editBtn;
        ImageButton deleteBtn;
        ImageButton reorderBtn;

        public ViewHolder(View inventoryView) {
            super(inventoryView);
            inventoryName = inventoryView.findViewById(R.id.inventory_title_edittext);
            parentLayout = inventoryView.findViewById(R.id.tile_inventory);
            editBtn = (ImageButton) inventoryView.findViewById(R.id.inv_edit_btn);
            deleteBtn = (ImageButton) inventoryView.findViewById(R.id.inv_delete_btn);
            reorderBtn = (ImageButton) inventoryView.findViewById(R.id.inv_reorder_btn);
        }
    }
}
