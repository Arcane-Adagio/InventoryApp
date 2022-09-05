package com.example.inventoryapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseHandler {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    public static class Group {
        public String groupID;
        public String groupName;
        public List<Inventory> inventories;

        public Group(String name){
            groupName = name;
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
