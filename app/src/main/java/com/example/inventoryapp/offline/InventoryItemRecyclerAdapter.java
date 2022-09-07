package com.example.inventoryapp.offline;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.data.InventoryItem;
import com.example.inventoryapp.R;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemRecyclerAdapter extends RecyclerView.Adapter<InventoryItemRecyclerAdapter.ViewHolder> {

    private static final String TAG = "InventoryListRecyclerViewAdapter";
    private static InventoryItemRecyclerAdapter INSTANCE = null;
    private static List<InventoryItem> mItems = new ArrayList<>();
    private static String currentInventoryName;
    static RecyclerView mRecyclerView;



    private InventoryItemRecyclerAdapter(String inventoryName, List<InventoryItem> items, Context context){
        mItems = items;
        currentInventoryName = inventoryName;
    }

    public static InventoryItemRecyclerAdapter ConstructItemRecyclerViewIfNotCreated(String inventoryName, List<InventoryItem> items, Context context){
        //Safe construction
        if (INSTANCE == null){
            INSTANCE = new InventoryItemRecyclerAdapter(inventoryName, items, context);
            mItems = items;
            currentInventoryName = inventoryName;
        }
        return INSTANCE;
    }

    public static void UpdateCurrentInventoryName(String newName){
        currentInventoryName = newName;
    }

    public static InventoryItemRecyclerAdapter ConstructItemRecyclerView(
            String inventoryName, List<InventoryItem> items, Context context, int recyclerViewID){
        INSTANCE = new InventoryItemRecyclerAdapter(inventoryName, items, context);
        mItems = items;
        currentInventoryName = inventoryName;
        mRecyclerView = ((Activity) context).findViewById(recyclerViewID);
        return INSTANCE;
    }

    public static void SetCurrentInventory(String inventory){
        currentInventoryName = inventory;
    }

    public static InventoryItemRecyclerAdapter GetItemRecyclerViewINSTANCE(){
        if (INSTANCE == null)
            return null;
        else
            return INSTANCE;
    }

    public void AddItemToInventory(){
        InventoryItem newItem = new InventoryItem( "Item #"+String.valueOf(mItems.size()), "","");
        User.AddInventoryItem(currentInventoryName, newItem);
        notifyItemInserted(getItemCount());
        mRecyclerView.scrollToPosition(mItems.size()-1);
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
        /* Called to rebind a view with its respective class */
        InventoryItem item = mItems.get(position);
        holder.itemNameTV.setText(item.getItemName());
        holder.itemDateTV.setText(mItems.get(position).getItemData());
        holder.itemQuantityTV.setText(mItems.get(position).getItemQuantity());
        holder.itemStatusPB.setProgress(5);
        holder.itemNeedfulCB.setChecked(mItems.get(position).isItemNeedful());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        /* Overridden to get a static reference to recyclerview */
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
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
            itemNameTV.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        ToggleEditMode_VH();
                        return true;
                    }
                    return false;
                }
            });
            itemDateTV = (TextView) itemview.findViewById(R.id.item_date_text);
            itemQuantityTV = (TextView) itemview.findViewById(R.id.item_quantity);
            itemStatusPB = (ProgressBar) itemview.findViewById(R.id.item_progressbar);
            editBtn = (ImageButton) itemview.findViewById(R.id.item_edit_btn);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToggleEditMode_VH();
                }
            });
            deleteBtn = (ImageButton) itemview.findViewById(R.id.item_delete_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User.RemoveItemFromInventory(currentInventoryName, mItems.get(getAdapterPosition()));
                    notifyItemRemoved(getAdapterPosition());
                }
            });
            reorderBtn = (ImageButton) itemview.findViewById(R.id.item_reorder_btn);

        }
        public void ToggleEditMode_VH(){
            if (itemNameTV.isFocusable()){
                //User clicked save button, so save
                InventoryItem item = mItems.get(getAdapterPosition());
                item.setItemName(itemNameTV.getText().toString());
                item.setItemDate(itemDateTV.getText().toString());
                item.setItemQuantity(itemQuantityTV.getText().toString());
                item.setItemNeedful(itemNeedfulCB.isChecked());
                //disable editing
                itemNameTV.setFocusable(false);
                itemDateTV.setFocusable(false);
                itemQuantityTV.setFocusable(false);
                //and Update Icon
                editBtn.setImageDrawable(GlobalActions.GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_edit_default));
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


