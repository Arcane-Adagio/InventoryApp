package com.example.inventoryapp.online;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.inventoryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class FirebaseHandler {
    private static final String TAG = "Firebase Handler";
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    public static final String FIREBASE_KEY_GROUPS = "Groups";
    public static final String FIREBASE_KEY_INVENTORIES = "Inventories";
    public static final String FIREBASE_KEY_INVENTORYITEMS = "Items";
    public static final String FIREBASE_KEY_MEMBERS = "Members";
    public static FirebaseAuth mCurrentUser;

    public static interface OnlineFragmentBehavior{
        void HandleFragmentInvalidation();
        void HandleInventoryInvalidation();
    }

    public static class Group {
        private String groupID;
        private String groupName;
        private String groupCode;
        private String groupPasswordHashed;
        private String groupOwner;
        public List<Inventory> inventories;
        public List<String> members = new ArrayList<>();

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

        public List<String> getMembers(){return members;}

        public void setMembers(List<String> newMemberList){members = newMemberList;}
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

        public String getInventoryName() {
            return inventoryName;
        }
    }

    public static class InventoryItem {
        public String itemName;
        public String itemID;
        public String itemDate;
        public Boolean itemNeedful;
        public String itemQuantity;


        InventoryItem(String name){
            itemName = name;
            itemDate = "";
            itemNeedful = false;
            itemQuantity = "";
        }

        InventoryItem(String name, String date){
            itemName = name;
            itemDate = date;
            itemNeedful = false;
            itemQuantity = "";
        }

        InventoryItem(String name, String date, String qty, boolean needful){
            //All parameters for reading from database
            itemName = name;
            itemDate = date;
            itemNeedful = needful;
            itemQuantity = qty;
        }

        public void setItemID(String _itemID){
            itemID = _itemID;
        }

        public String getItemID() {
            return itemID;
        }

        public String getItemDate() {
            return itemDate;
        }

        public String getItemName() {
            return itemName;
        }

        public String getItemQuantity() {
            return itemQuantity;
        }

        public boolean getItemNeedful() {
            return itemNeedful;
        }

        public void setItemName(String name) {itemName = name;}

        public void setItemDate(String ItemDate) {itemDate = ItemDate;}

        public void setItemQuantity(String ItemQuantity) {itemQuantity = ItemQuantity;}

        public void setItemNeedful(boolean ItemNeedful) {itemNeedful = ItemNeedful;}
    }



    public void AddGroup(Group group){
        /* By using a runTransaction method on this function,
        * a race condition is introduced. Dont do it. */
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        DatabaseReference newGroupRef = groupsRef.push();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        group.setGroupID(newGroupRef.getKey());

        String test = group.getGroupID();
        Log.d(TAG, "AddGroup: "+test);

        //new
        newGroupRef.setValue(group);
        DatabaseReference membersRef = newGroupRef.child(FIREBASE_KEY_MEMBERS);
        membersRef.child(user.getUid()).setValue(user.getDisplayName());

        //newGroupRef.runTransaction(PerformAddGroupTransaction(newGroupRef, group, user.getUid(), user.getDisplayName()));
    }

    public void RemoveGroup(Group group){
        /* If the user owns the group, delete the group,
        * if the user does not own the group, leave the group
        * */
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        DatabaseReference groupRef = groupsRef.child(group.getGroupID());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(Objects.equals(group.getGroupOwner(), user.getUid()))
            groupRef.runTransaction(PerformDeletionTransaction(groupRef));
        else{
            DatabaseReference membersRef = groupRef.child(FIREBASE_KEY_MEMBERS);
            DatabaseReference memberRef = membersRef.child(user.getUid());
            memberRef.runTransaction(PerformDeletionTransaction(memberRef));
        }
    }

    public void AddMemberToGroup(String groupID, FirebaseUser currentUser){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        DatabaseReference groupRef = groupsRef.child(groupID);
        DatabaseReference membersRef = groupRef.child(FIREBASE_KEY_MEMBERS);
        membersRef.runTransaction(PerformSetKeyValueTransaction(membersRef, currentUser.getUid(),currentUser.getDisplayName()));

    }

    public void RenameGroup(String groupID, String newName, FirebaseUser currentUser, OnlineFragmentBehavior callback){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(groupID)){
                    if(currentUser.getUid().equals(snapshot.child(groupID).child("groupOwner").getValue())){
                        DatabaseReference groupRef = groupsRef.child(groupID);
                        DatabaseReference groupNameRef = groupRef.child("groupName");
                        groupNameRef.runTransaction(PerformSetValueTransaction(groupNameRef, newName));
                    }
                }
                else{
                    //means group got deleted and a callback should be provided here
                    callback.HandleFragmentInvalidation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void RenameInventory(String groupID, String inventoryID, String newName, FirebaseUser currentUser, OnlineFragmentBehavior callback){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(groupID)){
                    DataSnapshot groupObj = snapshot.child(groupID);
                    DatabaseReference groupRef = groupsRef.child(groupID);
                    /* Only the owner is in charge of inventory names */
                    if(currentUser.getUid().equals(groupObj.child("groupOwner").getValue())){
                        if(groupObj.child(FIREBASE_KEY_INVENTORIES).hasChild(inventoryID)){
                            DataSnapshot inventoryObj = groupObj.child(FIREBASE_KEY_INVENTORIES).child(inventoryID);
                            DatabaseReference inventoryRef = groupRef.child(FIREBASE_KEY_INVENTORIES).child(inventoryID);
                            DatabaseReference inventoryNameRef = inventoryRef.child("inventoryName");
                            inventoryNameRef.runTransaction(PerformSetValueTransaction(inventoryNameRef, newName));
                        }
                    }
                    else {
                        //Notify user that only the owner can rename the inventory
                    }
                }
                else{
                    //means group got deleted and a callback should be provided here
                    callback.HandleFragmentInvalidation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public <T> void AddInventoryToGroup(String groupID, Inventory inventory, OnlineFragmentBehavior callback){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(groupID)){
                    DatabaseReference groupRef = groupsRef.child(groupID);
                    DatabaseReference inventoriesRef = groupRef.child(FIREBASE_KEY_INVENTORIES);
                    DatabaseReference newAdditionRef = inventoriesRef.push();
                    inventory.setInventoryID(newAdditionRef.getKey());
                    newAdditionRef.runTransaction(PerformSetValueTransaction(newAdditionRef, inventory));
                }
                else{
                    //means group got deleted
                    callback.HandleFragmentInvalidation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void RemoveInventoryFromGroup(String groupID, String inventoryID){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        DatabaseReference groupRef = groupsRef.child(groupID);
        DatabaseReference inventoriesRef = groupRef.child(FIREBASE_KEY_INVENTORIES);
        DatabaseReference inventoryRef = inventoriesRef.child(inventoryID);
        inventoryRef.runTransaction(PerformDeletionTransaction(inventoryRef));
    }

    public void AddInventoryItemToInventory(String groupID, String inventoryID, InventoryItem item, OnlineFragmentBehavior callback){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(groupID)){
                    if(snapshot.child(groupID).child(FIREBASE_KEY_INVENTORIES).hasChild(inventoryID)){
                        /* if inventory is still valid, add item to inventory */
                        DatabaseReference groupRef = groupsRef.child(groupID);
                        DatabaseReference inventoriesRef = groupRef.child(FIREBASE_KEY_INVENTORIES);
                        DatabaseReference inventoryRef = inventoriesRef.child(inventoryID);
                        DatabaseReference itemsRef = inventoryRef.child(FIREBASE_KEY_INVENTORYITEMS);
                        DatabaseReference newAdditionRef = itemsRef.push();
                        item.setItemID(newAdditionRef.getKey());
                        newAdditionRef.runTransaction(PerformSetValueTransaction(newAdditionRef, item));
                    }
                    else{
                        //means the inventory got deleted
                        callback.HandleInventoryInvalidation();
                    }
                }
                else{
                    //means group or inventory got deleted
                    callback.HandleFragmentInvalidation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void RemoveInventoryItemFromInventory(String groupID, String inventoryID, String inventoryItemID){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        DatabaseReference groupRef = groupsRef.child(groupID);
        DatabaseReference inventoriesRef = groupRef.child(FIREBASE_KEY_INVENTORIES);
        DatabaseReference inventoryRef = inventoriesRef.child(inventoryID);
        DatabaseReference itemsRef = inventoryRef.child(FIREBASE_KEY_INVENTORYITEMS);
        DatabaseReference inventoryItemRef = itemsRef.child(inventoryItemID);
        inventoryItemRef.runTransaction(PerformDeletionTransaction(inventoryItemRef));
    }

    public void UpdateInventoryItem(String groupID, String inventoryID, InventoryItem item, OnlineFragmentBehavior callback){
        DatabaseReference groupsRef = mRootRef.child(FIREBASE_KEY_GROUPS);
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(groupID)){
                    if(snapshot.child(groupID).child(FIREBASE_KEY_INVENTORIES).hasChild(inventoryID)){
                        /* if inventory is still valid, add item to inventory */
                        DatabaseReference groupRef = groupsRef.child(groupID);
                        DatabaseReference inventoriesRef = groupRef.child(FIREBASE_KEY_INVENTORIES);
                        DatabaseReference inventoryRef = inventoriesRef.child(inventoryID);
                        DatabaseReference itemsRef = inventoryRef.child(FIREBASE_KEY_INVENTORYITEMS);
                        DatabaseReference inventoryItemRef = itemsRef.child(item.getItemID());
                        inventoryItemRef.runTransaction(PerformSetValueTransaction(inventoryItemRef.child("itemDate"), item.getItemDate()));
                        inventoryItemRef.runTransaction(PerformSetValueTransaction(inventoryItemRef.child("itemNeedful"), item.getItemNeedful()));
                        inventoryItemRef.runTransaction(PerformSetValueTransaction(inventoryItemRef.child("itemQuantity"), item.getItemQuantity()));
                        inventoryItemRef.runTransaction(PerformSetValueTransaction(inventoryItemRef.child("itemName"), item.getItemName()));
                    }
                }
                else{
                    //means group or inventory got deleted
                    callback.HandleFragmentInvalidation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private <T> Transaction.Handler PerformSetValueTransaction(DatabaseReference ref, T value){
        //Firebase Built-in method to prevent corruption from simultaneous accesses
        return new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                ref.setValue(value);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        };
    }

    private <T> Transaction.Handler PerformSetKeyValueTransaction(DatabaseReference ref, String key, T value){
        //Firebase Built-in method to prevent corruption from simultaneous accesses
        return new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                ref.child(key).setValue(value);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        };
    }

    private Transaction.Handler PerformDeletionTransaction(DatabaseReference ref){
        //Firebase Built-in method to prevent corruption from simultaneous accesses
        return new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                ref.removeValue();
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        };
    }

    private <T> Transaction.Handler PerformAddGroupTransaction(DatabaseReference groupRef, T groupObj, String uID, String displayName){
        //Firebase Built-in method to prevent corruption from simultaneous accesses
        return new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                groupRef.setValue(groupObj);
                DatabaseReference membersRef = groupRef.child(FIREBASE_KEY_MEMBERS);
                membersRef.child(uID).setValue(displayName);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        };
    }

    public static void LogoutBehavior(Fragment callingFragment){
        /* To logout, the user and main recyclerview adapter needs to be sanitized */
        FirebaseAuth.getInstance().signOut();
        NavController navController = NavHostFragment.findNavController(callingFragment);
        try{
            navController.navigate(R.id.action_onlineFragment_to_onlineLoginFragment);
        }
        catch (Exception e){
            Log.d(TAG, "LogoutBehaviorException: "+e.toString());
        }
    }

}
