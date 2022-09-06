package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryItemActivityOnline extends AppCompatActivity {

    private final String TAG = "Inventory Item Activity Online";
    public static final String KEY_GROUPID = "groupID";
    public static final String KEY_INVENTORYNAME = "inventoryName";
    public static final String KEY_INVENTORYID = "inventoryID";
    private String mCurrentGroupID;
    private String mCurrentInventoryID;
    FloatingActionButton addition_fab;
    InventoryItemRVAdapter invItem_rva;
    int rv_id = R.id.inventoryitemlist_view;
    RecyclerView rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_inventoryitem);
    }

    @Override
    protected void onStart() {
        super.onStart();
        addition_fab = (FloatingActionButton) findViewById(R.id.inventoryitem_fab);
        addition_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddInventoryItem();
            }
        });
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString(KEY_GROUPID) != null){
                mCurrentGroupID = extras.getString(KEY_GROUPID);
                mCurrentInventoryID = extras.getString(KEY_INVENTORYID);
                Objects.requireNonNull(getSupportActionBar()).setTitle(extras.getString(KEY_INVENTORYNAME));
            }
        }
        SetupRecyclerView();
    }

    private void SetupRecyclerView(){
        rv=(RecyclerView) findViewById(rv_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        invItem_rva =new InventoryItemRVAdapter(this);
        rv.setAdapter(invItem_rva);
    }

    private void AddInventoryItem(){
        new FirebaseHandler().AddInventoryItemToInventory(
                mCurrentGroupID, mCurrentInventoryID, new FirebaseHandler.InventoryItem(""));
    }

    public class InventoryItemRVAdapter extends RecyclerView.Adapter<ViewHolder>{
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef = mRootReference.child(FirebaseHandler.FIREBASE_KEY_GROUPS);
        DatabaseReference groupRef = groupsRef.child(mCurrentGroupID);
        DatabaseReference mInventoriesReference = groupRef.child(FirebaseHandler.FIREBASE_KEY_INVENTORIES);
        DatabaseReference mInventoryReference = mInventoriesReference.child(mCurrentInventoryID);
        DatabaseReference mItemsReference = mInventoryReference.child(FirebaseHandler.FIREBASE_KEY_INVENTORYITEMS);
        List<FirebaseHandler.InventoryItem> itemData = new ArrayList<>();
        Context mContext;
        RecyclerView rv;

        public InventoryItemRVAdapter(Context context){
            mContext = context;
            rv = (RecyclerView) ((AppCompatActivity)context).findViewById(rv_id);
            mItemsReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    itemData.add(datasnapshotToItemConverter(snapshot));
                    rv.scrollToPosition(itemData.size()-1); //todo: take out if annoying
                    InventoryItemRVAdapter.this.notifyItemInserted(itemData.size()-1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String changedItemID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedItemID);
                    if(position != 1){
                        itemData.remove(position);
                        itemData.add(position, datasnapshotToItemConverter(snapshot));
                        InventoryItemRVAdapter.this.notifyItemChanged(position);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String changedItemID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedItemID);
                    if(position != -1){
                        itemData.remove(position);
                        InventoryItemRVAdapter.this.notifyItemRemoved(position);
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory_item, parent, false);
            final ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FirebaseHandler.InventoryItem item = itemData.get(position);
            holder.itemNameTV.setText(item.getItemName());
            holder.itemDateTV.setText(item.getItemDate());
            holder.itemQuantityTV.setText(item.getItemQuantity());
            holder.itemNeedfulCB.setChecked(item.getItemNeedful());
            holder.editBtn.setOnClickListener(view -> holder.ToggleEditMode_VH(item));
            holder.deleteBtn.setOnClickListener(view -> {
                String inventoryItemID = itemData.get(holder.getAdapterPosition()).getItemID();
                new FirebaseHandler().RemoveInventoryItemFromInventory(mCurrentGroupID, mCurrentInventoryID, inventoryItemID);
            });
        }

        @Override
        public int getItemCount() {
            return itemData.size();
        }

        private FirebaseHandler.InventoryItem datasnapshotToItemConverter(DataSnapshot snap){
            /*
            Object nameObj = snap.child("itemName").getValue();
            String itemName = (nameObj != null) ? nameObj.toString() : "";
            Object dateObj = snap.child("itemDate").getValue();
            String itemDate = (dateObj != null) ? dateObj.toString() : "";
            Object qtyObj = snap.child("itemQuantity").getValue();
            String itemQuantity = (qtyObj != null) ? qtyObj.toString() : "";
            Object booleanObj = snap.child("itemNeedful").getValue();
            boolean itemNeedful = (booleanObj != null) ? Boolean.getBoolean(qtyObj.toString()) : false;

             */

            String itemName = Objects.requireNonNull(snap.child("itemName").getValue()).toString();
            String itemDate = Objects.requireNonNull(snap.child("itemDate").getValue()).toString();
            String itemQuantity = Objects.requireNonNull(snap.child("itemQuantity").getValue()).toString();
            boolean itemNeedful = (boolean) Objects.requireNonNull(snap.child("itemNeedful").getValue());

            FirebaseHandler.InventoryItem itemObj = new FirebaseHandler.InventoryItem(itemName, itemDate, itemQuantity, itemNeedful);
            itemObj.setItemID(snap.getKey());
            return itemObj;
        }

        private int getPositionInRecyclerViewByID(String id){
            int position = -1;
            for (int i = 0; i< itemData.size(); i++){
                if(itemData.get(i).getItemID().equals(id)){
                    position = i;
                    break;
                }
            }
            return position;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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
                editBtn.setImageDrawable(GlobalActions.GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_edit_default));
                //save to database
                new FirebaseHandler().UpdateInventoryItem(mCurrentGroupID, mCurrentInventoryID, item);
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
                editBtn.setImageDrawable(GlobalActions.GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_save_default));
            }
        }
    }
}