package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseHandler {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    public static class Group {
        private String groupID;
        private String groupName;
        private String groupCode;
        private String groupPasswordHashed;
        private String groupOwner;
        public List<Inventory> inventories;

        public Group(String name, String code, String password, String owner){
            groupName = name;
            groupCode = code;
            groupPasswordHashed = String.valueOf(password.hashCode());
            groupOwner = owner;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupID(String gid){
            groupID = gid;
        }

        public String getGroupID() {
            return groupID;
        }

        public String getGroupCode(){
            return groupCode;
        }

        public String getGroupPasswordHashed(){
            return groupPasswordHashed;
        }

        public String getGroupOwner(){
            return groupOwner;
        }
    }

    public static class Inventory {
        public String inventoryID;
        public String inventoryName;
        public List<InventoryItem> items;

        public Inventory(String name){
            inventoryName = name;
        }

        public void setInventoryID(String gid){
            inventoryID = gid;
        }

        public String getInventoryID() {
            return inventoryID;
        }
    }

    public static class InventoryItem {
        public String itemName;
        public String itemID;

        InventoryItem(String name){
            itemName = name;
        }

        public void setItemID(String _itemID){
            itemID = _itemID;
        }

        public String getItemID() {
            return itemID;
        }
    }

    public void AddGroup(Group group){
        DatabaseReference groupsRef = mRootRef.child("Groups");
        DatabaseReference newAdditionRef = groupsRef.push();
        group.setGroupID(newAdditionRef.getKey());
        newAdditionRef.setValue(group);
    }

    public void RemoveGroup(String groupID){
        DatabaseReference groupsRef = mRootRef.child("Groups");
        DatabaseReference groupRef = groupsRef.child(groupID);
        groupRef.removeValue();
    }

    public void AddInventoryToGroup(String groupID, Inventory inventory){
        DatabaseReference groupsRef = mRootRef.child("Groups");
        DatabaseReference groupRef = groupsRef.child(groupID);
        DatabaseReference inventoriesRef = groupRef.child("inventories");
        DatabaseReference newAdditionRef = inventoriesRef.push();
        inventory.setInventoryID(newAdditionRef.getKey());
        newAdditionRef.setValue(inventory);
    }

    public void AddInventoryItemToInventory(String groupID, String inventoryID, InventoryItem item){
        DatabaseReference groupsRef = mRootRef.child("Groups");
        DatabaseReference groupRef = groupsRef.child(groupID);
        DatabaseReference inventoriesRef = groupRef.child("inventories");
        DatabaseReference inventoryRef = inventoriesRef.child(inventoryID);
        DatabaseReference itemsRef = inventoryRef.child("items");
        DatabaseReference newAdditionRef = itemsRef.push();
        item.setItemID(newAdditionRef.getKey());
        newAdditionRef.setValue(item);
    }

}
