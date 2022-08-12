package com.example.inventoryapp;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryItem {

    public enum ItemState{ Full, Good, Half, Low, Empty, NA}

    public static String itemName;
    private static boolean itemNeedful = false;
    private static String itemData;
    private static String itemQuantity;
    private static ItemState itemStatus;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final CheckBox itemNeedfulCB;
        public final TextView itemNameTV;
        public final TextView itemDataTV;
        public final TextView itemQuantityTV;
        public final ProgressBar itemStatusPB;
        public final ImageButton editBtn;
        public final ImageButton deleteBtn;
        public final ImageButton reorderBtn;

        public ViewHolder(@NonNull View view) {
            super(view);
            itemNeedfulCB = (CheckBox) view.findViewById(R.id.item_needful_checkbox);
            itemNameTV = (TextView) view.findViewById(R.id.item_title);
            itemDataTV = (TextView) view.findViewById(R.id.item_date_text);
            itemQuantityTV = (TextView) view.findViewById(R.id.item_quantity);
            itemStatusPB = (ProgressBar) view.findViewById(R.id.item_progressbar);
            editBtn = (ImageButton) view.findViewById(R.id.item_edit_btn);
            deleteBtn = (ImageButton) view.findViewById(R.id.item_delete_btn);
            reorderBtn = (ImageButton) view.findViewById(R.id.item_reorder_btn);
        }
    }

    public String getItemName() {
        return itemName != null ? itemName : null;
    }

    public boolean isItemNeedful(){
        return itemNeedful;
    }

    public String getItemData(){
        return itemData != null ? itemData : null;
    }

    public String getItemQuantity(){
        return itemQuantity != null ? itemQuantity : null;
    }

    public ItemState getItemStatus(){
        return itemStatus != null ? itemStatus : null;
    }
}
