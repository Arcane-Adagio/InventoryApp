package com.example.inventoryapp.offline;

import android.app.Activity;
import android.content.Context;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.MainActivity;
import com.example.inventoryapp.R;

import java.util.ArrayList;
import java.util.List;

public class InventoryRecyclerViewerAdapter extends RecyclerView.Adapter<InventoryRecyclerViewerAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryRecyclerViewerAdapter INSTANCE = null;
    private static List<String> mInventoryNames = new ArrayList<>();
    private static RecyclerView mRecyclerView;
    private static FragmentActivity cActivity;

    private InventoryRecyclerViewerAdapter(List<String> imageNames, Context context){
        mInventoryNames = imageNames;
    }

    public static InventoryRecyclerViewerAdapter ConstructHomeRecyclerViewIfNotCreated(List<String> invNames, Activity context){
        if (INSTANCE == null){
            INSTANCE = new InventoryRecyclerViewerAdapter(invNames, context);
            cActivity = (FragmentActivity) context;
            mInventoryNames = invNames;
        }
        return INSTANCE;
    }

    public static InventoryRecyclerViewerAdapter GetHomeRecyclerViewINSTANCE(){
        if (INSTANCE == null)
            return null;
        else
            return INSTANCE;
    }

    public void AddInventory(){
        String newInventoryName = User.AddInventory();
        //mInventoryNames.add(newInventoryName);
        notifyItemInserted(getItemCount());
        mRecyclerView.scrollToPosition(getItemCount()-1);
        Log.d(TAG, "AddInventory: "+String.valueOf(mInventoryNames));
    }

    public void DeleteInventory(String inventoryName, int position){
        User.RemoveInventory(inventoryName);
        //mInventoryNames.remove(position);
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
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Debug", "onBind: "+String.valueOf(mInventoryNames.get(holder.getAdapterPosition())));

            }
        });
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
        bundle.putString("inventoryName", inventoryName);
        navController.navigate(R.id.action_offlineInventoryFragment_to_offlineItemFragment, bundle);
    }

}
