package com.example.inventoryapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class TestRecyclerView extends RecyclerView.Adapter<TestRecyclerView.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private List<String> mInventoryNames = new ArrayList<>();
    private Context mContext;

    public TestRecyclerView(List<String> imageNames, Context context){
        mInventoryNames = imageNames;
        mContext = context;
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
                Log.d("Debug", "onBind: "+String.valueOf(mInventoryNames.get(position)));

            }
        });
    }

    @Override
    public int getItemCount() {
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
}
