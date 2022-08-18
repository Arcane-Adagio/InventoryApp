package com.example.inventoryapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryRecyclerViewerAdapter extends RecyclerView.Adapter<InventoryRecyclerViewerAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryRecyclerViewerAdapter INSTANCE = null;
    private static List<String> mInventoryNames = new ArrayList<>();
    private static RecyclerView mRecyclerView;

    private InventoryRecyclerViewerAdapter(List<String> imageNames, Context context){
        mInventoryNames = imageNames;
    }

    public static InventoryRecyclerViewerAdapter ConstructHomeRecyclerViewIfNotCreated(List<String> invNames, Context context){
        if (INSTANCE == null){
            INSTANCE = new InventoryRecyclerViewerAdapter(invNames, context);
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
        User.AddInventory();
        notifyItemInserted(getItemCount());
        mRecyclerView.scrollToPosition(getItemCount()-1);
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
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), InventoryItemActivity.class);
                    intent.putExtra("inventoryName", inventoryName.getText().toString());
                    view.getContext().startActivity(intent);
                }
            });
            deleteBtn = (ImageButton) itemview.findViewById(R.id.inv_delete_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User.RemoveInventory(inventoryName.getText().toString());
                    notifyItemRemoved(getAdapterPosition());
                }
            });
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
}
