package com.example.inventoryapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemRecyclerAdapter extends RecyclerView.Adapter<InventoryItemRecyclerAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryItemRecyclerAdapter INSTANCE = null;
    private static List<String> mInventoryNames = new ArrayList<>();
    private static Context mContext;

    private InventoryItemRecyclerAdapter(List<String> imageNames, Context context){
        mInventoryNames = imageNames;
        mContext = context;
    }

    public static InventoryItemRecyclerAdapter ConstructItemRecyclerViewIfNotCreated(List<String> imageNames, Context context){
        if (INSTANCE == null){
            INSTANCE = new InventoryItemRecyclerAdapter(imageNames, context);
            mInventoryNames = imageNames;
            mContext = context;
        }
        return INSTANCE;
    }

    public static InventoryItemRecyclerAdapter GetItemRecyclerViewINSTANCE(){
        if (INSTANCE == null)
            return null;
        else
            return INSTANCE;
    }

    public void AddInventory2(){
        notifyItemInserted(getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_inventory_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        /*
        holder.itemNameTV.setText(items.get(position).getItemName());
        Log.d("error", "onBindViewHolder: " + items.get(position).getItemName() + " " + String.valueOf(position) + items.toString());
        holder.itemDataTV.setText(items.get(position).getItemData());
        holder.itemQuantityTV.setText(items.get(position).getItemQuantity());
        holder.itemStatusPB.setProgress(5);
        holder.itemNeedfulCB.setChecked(items.get(position).isItemNeedful());
        */

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

        /*
        public final CheckBox itemNeedfulCB;
        public final TextView itemNameTV;
        public final TextView itemDataTV;
        public final TextView itemQuantityTV;
        public final ProgressBar itemStatusPB;
        public final ImageButton editBtn;
        public final ImageButton deleteBtn;
        public final ImageButton reorderBtn;
         */

        public ViewHolder(View itemview) {
            super(itemview);

            /*
            itemNeedfulCB = (CheckBox) view.findViewById(R.id.item_needful_checkbox);
            itemNameTV = (TextView) view.findViewById(R.id.item_title);
            itemDataTV = (TextView) view.findViewById(R.id.item_date_text);
            itemQuantityTV = (TextView) view.findViewById(R.id.item_quantity);
            itemStatusPB = (ProgressBar) view.findViewById(R.id.item_progressbar);
            editBtn = (ImageButton) view.findViewById(R.id.item_edit_btn);
            deleteBtn = (ImageButton) view.findViewById(R.id.item_delete_btn);
            reorderBtn = (ImageButton) view.findViewById(R.id.item_reorder_btn);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecyclerViewFragment.recyclerAdapter.RemoveItem(getAdapterPosition());
                }
            });
            */


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


