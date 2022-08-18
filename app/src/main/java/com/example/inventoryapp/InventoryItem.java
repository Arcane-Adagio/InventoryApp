package com.example.inventoryapp;

import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/* Data class singleton for an inventory item */

public class InventoryItem {

    public enum ItemState{ Full, Good, Half, Low, Empty, NA}

    private String mItemName;
    private boolean mItemNeedful = false;
    private String mItemDate;
    private String mItemQuantity;
    private ItemState mItemStatus;
    private boolean mEditMode = true;

    public String toString()
    {
        return mItemName;
    }

    public InventoryItem (String name, String data, String quantity){
        mItemName = name;
        mItemDate = data;
        mItemQuantity = quantity;
    }

    public InventoryItem (String name, String data, String quantity, boolean itemNeedful){
        mItemName = name;
        mItemDate = data;
        mItemQuantity = quantity;
        mItemNeedful = itemNeedful;
    }

    public void setItemName(String itemName) {mItemName = itemName;}

    public void setItemDate(String ItemDate) {mItemDate = ItemDate;}

    public void setItemQuantity(String ItemQuantity) {mItemQuantity = ItemQuantity;}

    public void setItemNeedful(boolean ItemNeedful) {mItemNeedful = ItemNeedful;}

    public String getItemName() {
        return mItemName != null ? mItemName : null;
    }

    public boolean isItemNeedful(){
        return mItemNeedful;
    }

    public String getItemData(){
        return mItemDate != null ? mItemDate : null;
    }

    public String getItemQuantity(){
        return mItemQuantity != null ? mItemQuantity : null;
    }

    public String getItemNeedfulString(){
        if(mItemNeedful)
            return "true";
        else
            return "fasle";
    }
}
