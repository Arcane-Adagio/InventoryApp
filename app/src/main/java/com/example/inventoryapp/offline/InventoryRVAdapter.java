package com.example.inventoryapp.offline;

import static com.example.inventoryapp.GlobalConstants.FRAGMENT_ARG_INVENTORY_NAME;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.R;

import java.util.ArrayList;
import java.util.List;

public class InventoryRVAdapter extends RecyclerView.Adapter<InventoryRVAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryRVAdapter INSTANCE = null;
    private static List<String> mInventoryNames = new ArrayList<>();
    private static RecyclerView mRecyclerView;

    private InventoryRVAdapter(List<String> imageNames){
        mInventoryNames = imageNames;
    }

    public static InventoryRVAdapter ConstructHomeRecyclerViewIfNotCreated(List<String> invNames, Activity context){
        if (INSTANCE == null){
            INSTANCE = new InventoryRVAdapter(invNames);
            mInventoryNames = invNames;
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
        OfflineInventoryManager.RemoveInventory(inventoryName);
        notifyItemRemoved(position);
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
        CardView parentLayout;
        ImageButton editBtn;
        ImageButton deleteBtn;
        ImageButton reorderBtn;

        public ViewHolder(View itemview) {
            super(itemview);
            inventoryName = itemview.findViewById(R.id.inventory_title_edittext);
            parentLayout = itemview.findViewById(R.id.tile_inventory);
            editBtn = (ImageButton) itemview.findViewById(R.id.inv_edit_btn);
            editBtn.setOnClickListener(view -> NavigateToItemFragment(inventoryName.getText().toString()));
            deleteBtn = (ImageButton) itemview.findViewById(R.id.inv_delete_btn);
            deleteBtn.setOnClickListener(view -> DeleteInventory( inventoryName.getText().toString(),getAdapterPosition()));
            reorderBtn = (ImageButton) itemview.findViewById(R.id.inv_reorder_btn);
        }
    }

    public static void ResetRecyclerView(){
        INSTANCE = null;
        mInventoryNames = new ArrayList<>();
        mRecyclerView = null;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    private void NavigateToItemFragment(String inventoryName){
        Fragment callingFragment = OfflineInventoryFragment.GetFragmentReference();
        Bundle bundle = new Bundle();
        NavController navController = NavHostFragment.findNavController(callingFragment);
        bundle.putString(FRAGMENT_ARG_INVENTORY_NAME, inventoryName);
        navController.navigate(R.id.action_offlineInventoryFragment_to_offlineItemFragment, bundle);
    }

}
