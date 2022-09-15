package com.example.inventoryapp.offline;

import static com.example.inventoryapp.GlobalConstants.FRAGMENT_ARG_INVENTORY_NAME;
import static com.example.inventoryapp.online.OnlineFragment.currentGroupID;
import static com.example.inventoryapp.online.OnlineFragment.currentInventoryID;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.Dialogs;
import com.example.inventoryapp.online.FirebaseHandler;

import java.util.ArrayList;
import java.util.List;

public class InventoryRVAdapter extends RecyclerView.Adapter<InventoryRVAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryRVAdapter INSTANCE = null;
    private static List<String> mInventoryNames = new ArrayList<>();
    private static RecyclerView mRecyclerView;
    private static OfflineFragment.SimpleCallback navCallback;

    private InventoryRVAdapter(List<String> imageNames){
        mInventoryNames = imageNames;
    }

    public static InventoryRVAdapter ConstructHomeRecyclerViewIfNotCreated(List<String> invNames
            , OfflineFragment.SimpleCallback navigationCallback){
        if (INSTANCE == null){
            INSTANCE = new InventoryRVAdapter(invNames);
            mInventoryNames = invNames;
            navCallback = navigationCallback;
        }
        return INSTANCE;
    }

    public static InventoryRVAdapter ReconstructRecyclerView(List<String> invNames){
        INSTANCE = new InventoryRVAdapter(invNames);
        mInventoryNames = invNames;
        return INSTANCE;
    }

    public static InventoryRVAdapter GetHomeRecyclerViewINSTANCE(){
        if (INSTANCE == null)
            return null;
        else
            return INSTANCE;
    }

    public void NotifyElementAdded(){
        //if(mInventoryNames.size() == 0)
            //on first boot, make sure adapter gets first
            //mInventoryNames = OfflineInventoryManager.InventoryNames;
        notifyItemInserted(getItemCount());
        mRecyclerView.scrollToPosition(getItemCount()-1);
        Log.d(TAG, "AddInventory: "+String.valueOf(mInventoryNames));
    }

    public void DeleteInventory(String inventoryName, int position){
        //potential runtime exception if user presses button too fast
        try{
            OfflineInventoryManager.RemoveInventory(inventoryName);
            notifyItemRemoved(position);
        }
        catch (Exception e){
            Log.d(TAG, "onBindViewHolder: "+e.getMessage());
        }
    }

    public void RenameInventory(int position, String newName){
        mInventoryNames.set(position, newName);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.inventoryName.setText(mInventoryNames.get(position));
        holder.parentLayout.setOnClickListener(view ->
                Log.d("Debug", "onBind: "+String.valueOf(mInventoryNames.get(holder.getAdapterPosition()))));
    }

    @Override
    public int getItemCount() {
        if(mInventoryNames == null)
            mInventoryNames = new ArrayList<>();
        return mInventoryNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView inventoryName;
        LinearLayout parentLayout;
        ImageButton editBtn;
        ImageButton deleteBtn;
        ImageButton reorderBtn;

        public ViewHolder(View itemview) {
            super(itemview);
            inventoryName = itemview.findViewById(R.id.inventory_title_edittext);
            parentLayout = itemview.findViewById(R.id.tile_inventory);
            editBtn = (ImageButton) itemview.findViewById(R.id.inv_edit_btn);
            editBtn.setOnClickListener(view -> navCallback.CallableFunction(new String[] {inventoryName.getText().toString()}));
            deleteBtn = (ImageButton) itemview.findViewById(R.id.inv_delete_btn);
            deleteBtn.setOnClickListener(view -> Dialogs.AreYouSureDialog(view.getContext(), new Dialogs.DialogListener() {
                        @Override
                        public boolean submissionCallback(String[] args) {
                            DeleteInventory( inventoryName.getText().toString(),getAdapterPosition());
                            return true;
                        }

                        @Override
                        public void cancelCallback() {

                        }
                    })
            );
            reorderBtn = (ImageButton) itemview.findViewById(R.id.inv_reorder_btn);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }
}
