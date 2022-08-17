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


        public void ToggleEditMode_VH(){
            if (itemNameTV.isFocusable()){
                //User clicked save and shouldnt be able to edit anymore
                itemNameTV.setFocusable(false);
                itemDataTV.setFocusable(false);
                itemQuantityTV.setFocusable(false);
                //Update Icon
                editBtn.setImageDrawable(GlobalActions.GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_edit_default));
            }
            else {
                //User Clicked Edit and wants to give input
                itemNameTV.setInputType(InputType.TYPE_CLASS_TEXT);
                itemDataTV.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                itemQuantityTV.setInputType(InputType.TYPE_CLASS_NUMBER);
                itemNameTV.setFocusableInTouchMode(true);
                itemDataTV.setFocusableInTouchMode(true);
                itemQuantityTV.setFocusableInTouchMode(true);
                //update Icon
                editBtn.setImageDrawable(GlobalActions.GetDrawableFromInt(editBtn.getContext(), R.drawable.ic_save_default));
            }
        }
    }

    public void ToggleEditMode(){
        mEditMode = !mEditMode;
    }

    public boolean isInEditMode(){
        return mEditMode;
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

    public ItemState getItemStatus(){
        return mItemStatus != null ? mItemStatus : null;
    }
}
