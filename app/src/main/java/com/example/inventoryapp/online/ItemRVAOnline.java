package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_ITEMDATE;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_ITEMNAME;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_ITEMNEEDFUL;
import static com.example.inventoryapp.GlobalConstants.FIREBASE_SUBKEY_ITEMQUANTITY;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.data.InventoryFragment.GetDrawableFromInt;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupID;
import static com.example.inventoryapp.online.OnlineFragment.currentInventoryID;
import static com.example.inventoryapp.online.OnlineFragment.groupsRef;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalConstants;
import com.example.inventoryapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Adapter class that handles the data between the recycler view displaying items information and
 * the firebase input
 * */

public class ItemRVAOnline extends  RecyclerView.Adapter<ItemRVAOnline.ViewHolder>{

    DatabaseReference groupRef = groupsRef.child(currentGroupID);
    DatabaseReference mInventoriesReference = groupRef.child(GlobalConstants.FIREBASE_KEY_INVENTORIES);
    DatabaseReference mInventoryReference = mInventoriesReference.child(currentInventoryID);
    DatabaseReference mItemsReference = mInventoryReference.child(GlobalConstants.FIREBASE_KEY_INVENTORYITEMS);
    List<FirebaseHandler.InventoryItem> itemData = new ArrayList<>();
    Context mContext;
    RecyclerView rv;
    Fragment cFragment;
    private final String TAG = "ItemRVA - Online";
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public ItemRVAOnline(Context context, RecyclerView recyclerView, Fragment callingFragment){
        mContext = context;
        rv = recyclerView;
        cFragment = callingFragment;
        mItemsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildAdded: an object was added to the firebase tree
                itemData.add(datasnapshotToItemConverter(snapshot));
                rv.scrollToPosition(itemData.size()-1); //todo: take out if annoying
                ItemRVAOnline.this.notifyItemInserted(itemData.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //onChildChanged: an object or its sub-objects was changed in the firebase tree
                executor.execute(() -> { //runs on the background thread
                    String changedItemID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedItemID);
                    handler.post(() -> { //runs on ui thread
                        //if item in recycler view has been changed, remove old item
                        //and add new item that is representative of firebase's new data
                        if(position != OUT_OF_BOUNDS){
                            itemData.remove(position);
                            itemData.add(position, datasnapshotToItemConverter(snapshot));
                            ItemRVAOnline.this.notifyItemChanged(position);
                        }
                    });
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //onChildRemoved: an object was removed from the firebase tree
                executor.execute(() -> { //background thread execution
                    String changedItemID = snapshot.getKey().toString();
                    handler.post(() -> { // ui thread execution
                        /* Position function is here because a race condition happens if it isn't */
                        int position = getPositionInRecyclerViewByID(changedItemID);
                        //remove item from recyclerview if it has been removed from the database
                        if(position != OUT_OF_BOUNDS){
                            itemData.remove(position);
                            ItemRVAOnline.this.notifyItemRemoved(position);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* Connects the data to the view based on position every time a tile is reconstructed */
        FirebaseHandler.InventoryItem item = itemData.get(position);
        holder.itemNameTV.setText(item.getItemName());
        holder.itemDateTV.setText(item.getItemDate());
        holder.itemQuantityTV.setText(item.getItemQuantity());
        holder.itemNeedfulCB.setChecked(item.getItemNeedful());
        holder.editBtn.setOnClickListener(view -> holder.ToggleEditMode_VH(item));
        holder.deleteBtn.setOnClickListener(view -> {
            try{ //potential runtime exception if user presses button too fast
                String inventoryItemID = itemData.get(holder.getAdapterPosition()).getItemID();
                FirebaseHandler.RemoveInventoryItemFromInventory(currentGroupID, currentInventoryID, inventoryItemID);
            }
            catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemData.size();
    }

    private FirebaseHandler.InventoryItem datasnapshotToItemConverter(DataSnapshot snap){
        /* takes a firebase dataSnapshot and converts it to a data object for the recycler view */
        String itemName = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_ITEMNAME).getValue()).toString();
        String itemDate = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_ITEMDATE).getValue()).toString();
        String itemQuantity = Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_ITEMQUANTITY).getValue()).toString();
        boolean itemNeedful = (boolean) Objects.requireNonNull(snap.child(FIREBASE_SUBKEY_ITEMNEEDFUL).getValue());
        FirebaseHandler.InventoryItem itemObj = new FirebaseHandler.InventoryItem(itemName, itemDate, itemQuantity, itemNeedful);
        itemObj.setItemID(snap.getKey());
        return itemObj;
    }

    private int getPositionInRecyclerViewByID(String id){
        /* returns the position of the tile in the recyclerview based on the id given */
        int position = OUT_OF_BOUNDS;
        for (int i = 0; i< itemData.size(); i++){
            if(itemData.get(i).getItemID().equals(id)){
                position = i;
                break;
            }
        }
        return position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        /* Data container for the recyclerview */
        public final CheckBox itemNeedfulCB;
        public final TextView itemNameTV;
        public final TextView itemDateTV;
        public final TextView itemQuantityTV;
        public final ProgressBar itemStatusPB;
        public final ImageButton editBtn;
        public final ImageButton deleteBtn;
        public final ImageButton reorderBtn;

        public ViewHolder(View itemview) {
            super(itemview);

            itemNeedfulCB = (CheckBox) itemview.findViewById(R.id.item_needful_checkbox);
            itemNameTV = (TextView) itemview.findViewById(R.id.item_title);
            itemDateTV = (TextView) itemview.findViewById(R.id.item_date_text);
            itemQuantityTV = (TextView) itemview.findViewById(R.id.item_quantity);
            itemStatusPB = (ProgressBar) itemview.findViewById(R.id.item_progressbar);
            editBtn = (ImageButton) itemview.findViewById(R.id.item_edit_btn);
            deleteBtn = (ImageButton) itemview.findViewById(R.id.item_delete_btn);
            reorderBtn = (ImageButton) itemview.findViewById(R.id.item_reorder_btn);

        }
        public void ToggleEditMode_VH(FirebaseHandler.InventoryItem item){
            if (itemNameTV.isFocusable()){
                //User clicked save button, so save
                String name = itemNameTV.getText().toString();
                item.setItemDate(itemDateTV.getText().toString());
                item.setItemQuantity(itemQuantityTV.getText().toString());
                item.setItemName(name);
                item.setItemNeedful(itemNeedfulCB.isChecked());
                //disable editing
                itemNameTV.setFocusable(false);
                itemDateTV.setFocusable(false);
                itemQuantityTV.setFocusable(false);
                //and Update Icon
                editBtn.setImageDrawable(GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_edit_default));
                //save to database
                FirebaseHandler.UpdateInventoryItem(currentGroupID, currentInventoryID, item, (FirebaseHandler.OnlineFragmentBehavior) cFragment);
            }
            else {
                //User Clicked Edit and wants to give input
                itemNameTV.setInputType(InputType.TYPE_CLASS_TEXT);
                itemDateTV.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                itemQuantityTV.setInputType(InputType.TYPE_CLASS_NUMBER);
                itemNameTV.setFocusableInTouchMode(true);
                itemDateTV.setFocusableInTouchMode(true);
                itemQuantityTV.setFocusableInTouchMode(true);
                //update Icon
                editBtn.setImageDrawable(GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_save_default));
            }
        }
    }
}
