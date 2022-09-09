package com.example.inventoryapp.online;

import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_GROUPID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_INVENTORYID;
import static com.example.inventoryapp.GlobalConstants.ONLINE_KEY_INVENTORYNAME;
import static com.example.inventoryapp.GlobalConstants.OUT_OF_BOUNDS;
import static com.example.inventoryapp.GlobalConstants.db_max_code_length;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class OnlineItemFragment extends Fragment implements FirebaseHandler.OnlineFragmentBehavior{

    private final String TAG = "Inventory Item Activity Online";
    private static String mCurrentGroupID = "";
    private String mCurrentInventoryID;
    FloatingActionButton addition_fab;
    FloatingActionButton rename_fab;
    InventoryItemRVAdapter invItem_rva;
    int rv_id = R.id.inventoryitemlist_view;
    RecyclerView rv;
    Activity cActivity;

    public OnlineItemFragment() {
        // Required empty public constructor
    }

    public static String getCurrentGroupID(){
        /* possible idea to automatically go back to the groups page when
        * group is deleted.
        * Another idea is using the lifecycle
        * Another idea is a static enum in groups, which keeps track of the fragment in view */
        return mCurrentGroupID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentGroupID = getArguments().getString(ONLINE_KEY_GROUPID);
            mCurrentInventoryID = getArguments().getString(ONLINE_KEY_INVENTORYID);
            Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(this.getArguments().getString(ONLINE_KEY_INVENTORYNAME));
        }
        cActivity = getActivity();
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
        if (GlobalActions.DefaultMenuOptionSelection(item,cActivity, this))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_online_item, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        addition_fab = (FloatingActionButton) getView().findViewById(R.id.inventoryitem_fab);
        addition_fab.setOnClickListener(view -> AddInventoryItem());
        rename_fab = (FloatingActionButton) getView().findViewById(R.id.fab_renameInventory);
        rename_fab.setOnClickListener(view -> ShowRenameInventoryDialog());
        SetupRecyclerView();
    }

    private void SetupRecyclerView(){
        rv=(RecyclerView) getView().findViewById(rv_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(cActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        invItem_rva =new InventoryItemRVAdapter(cActivity);
        rv.setAdapter(invItem_rva);
    }

    private void AddInventoryItem(){
        new FirebaseHandler().AddInventoryItemToInventory(
                mCurrentGroupID, mCurrentInventoryID, new FirebaseHandler.InventoryItem(""), this);
    }

    @Override
    public void HandleFragmentInvalidation() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineFragment_to_onlineLoginFragment);
    }

    @Override
    public void HandleInventoryInvalidation() {
        Bundle bundle = new Bundle();
        bundle.putString(ONLINE_KEY_GROUPID,mCurrentGroupID);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineItemFragment_to_onlineInventoryFragment, bundle);
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
                    if(position != OUT_OF_BOUNDS){
                        itemData.remove(position);
                        itemData.add(position, datasnapshotToItemConverter(snapshot));
                        InventoryItemRVAdapter.this.notifyItemChanged(position);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String changedItemID = snapshot.getKey().toString();
                    int position = getPositionInRecyclerViewByID(changedItemID);
                    if(position != OUT_OF_BOUNDS){
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
            String itemName = Objects.requireNonNull(snap.child("itemName").getValue()).toString();
            String itemDate = Objects.requireNonNull(snap.child("itemDate").getValue()).toString();
            String itemQuantity = Objects.requireNonNull(snap.child("itemQuantity").getValue()).toString();
            boolean itemNeedful = (boolean) Objects.requireNonNull(snap.child("itemNeedful").getValue());
            FirebaseHandler.InventoryItem itemObj = new FirebaseHandler.InventoryItem(itemName, itemDate, itemQuantity, itemNeedful);
            itemObj.setItemID(snap.getKey());
            return itemObj;
        }

        private int getPositionInRecyclerViewByID(String id){
            int position = OUT_OF_BOUNDS;
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
                new FirebaseHandler().UpdateInventoryItem(mCurrentGroupID, mCurrentInventoryID, item, OnlineItemFragment.this);
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

    public void ShowRenameInventoryDialog(){
        final Dialog dialog = new Dialog(cActivity);
        dialog.setContentView(R.layout.dlog_renameinventory);
        Button submitBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_submit);
        Button cancelBtn = (Button) dialog.findViewById(R.id.btn_renameInventory_cancel);
        EditText nameEditText = (EditText)dialog.findViewById(R.id.edittext_renameInventory);
        //Set Max length of each edit text to make sure it matches the length allotted by the database
        nameEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(db_max_code_length) });
        //when focus has been lost, check if code is valid
        submitBtn.setOnClickListener(v -> {
            RenameInventoryBehavior(nameEditText, dialog);
        });
        cancelBtn.setOnClickListener(v -> {
            nameEditText.getBackground().clearColorFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void RenameInventoryBehavior(EditText nameEditText, Dialog dialog){
        if(nameEditText.getText() == null)
            return;
        if(nameEditText.getText().toString().isEmpty()){
            nameEditText.setHint("Please Enter A Valid Name");
            nameEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        String newName = nameEditText.getText().toString();
        new FirebaseHandler().RenameInventory(mCurrentGroupID, mCurrentInventoryID, newName, FirebaseAuth.getInstance().getCurrentUser(), this);
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(this.getArguments().getString(newName));
        dialog.dismiss();
    }
}