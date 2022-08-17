package com.example.inventoryapp;

/* This class Follows the Singleton pattern to represent the logged in user */

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class User {

    private static User INSTANCE = null;
    private static String mUsername;
    public static List<String> Inventorys = new ArrayList<String>();
    public static List<JSONObject> InventoryJSONs = new ArrayList<JSONObject>();
    public static List<String> InventoryNames = new ArrayList<String>();
    public static List<List<InventoryItem>> InventoryItems = new ArrayList<>();
    public static JSONArray test;
    public static boolean sample = false;

    private User() {};

    public static User getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new User();
        }
        return(INSTANCE);
    }

    public static String getUsername(){
        if(INSTANCE == null)
            INSTANCE = new User();
        return mUsername;
    }

    public static String getInventoryJSON(){
        if(INSTANCE == null)
            INSTANCE = new User();
        if(InventoryNames == null || InventoryNames.size() == 0)
            return "";
        else
            return convertInventoryToString();
    }

    public static void setUsername(String username){
        //Username should only be allowed to be set
        //when null
        if(INSTANCE == null)
            INSTANCE = new User();
        if (mUsername == null)
            mUsername = username;
    }

    public static void setInventorys(String inventory){
        //Username should only be allowed to be set
        //when null
        if(INSTANCE == null)
            INSTANCE = new User();
        if (InventoryNames == null || InventoryNames.size() == 0){
            if (sample || inventory == "" || inventory.length() < 3)
                DemoConvert();
            else
                ConvertStringToInventory(inventory);
        }
    }

    public static void LogoutUser(){
        if(INSTANCE == null)
            INSTANCE = new User();
        mUsername = null;
    }

    public static void DemoConvert(){
        Log.d("user", SampleText().toString());
        JSONArray arr = SampleText();
        List<String> names = new ArrayList<String>();
        try {
            for (int i=0; i<arr.length(); i++){
                //add each json object to object list
                InventoryJSONs.add(arr.getJSONObject(i));

                //Add each inventory object name to class list
                names.add(arr.getJSONObject(i).getString("name"));

                //Convert each inventory's items to lists and store them
                InventoryItems.add(convertJSONObjectToInventoryItemList(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("User", "GetInventoryNames: "+names.get(0));
        Log.d("User", "GetInventoryNames: "+String.valueOf(names.size()));
        Log.d("User", "GetInventoryNames: "+String.valueOf(InventoryItems.size()));
        Log.d("User", "GetInventoryString: "+convertInventoryToString());
        InventoryNames = names;

    }

    public static void ConvertStringToInventory(String inventoryStringFromDatabase){
        if (sample)
            return;
        JSONArray arr = null;
        List<String> names = new ArrayList<String>();
        try {
            arr = new JSONArray(inventoryStringFromDatabase);
            for (int i=0; i<arr.length(); i++){
                //add each json object to object list
                InventoryJSONs.add(arr.getJSONObject(i));

                //Add each inventory object name to class list
                names.add(arr.getJSONObject(i).getString("name"));

                //Convert each inventory's items to lists and store them
                InventoryItems.add(convertJSONObjectToInventoryItemList(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("User", "GetInventoryNames: "+names.get(0));
        Log.d("User", "GetInventoryNames: "+String.valueOf(names.size()));
        Log.d("User", "GetInventoryNames: "+String.valueOf(InventoryItems.size()));
        Log.d("User", "GetInventoryString: "+convertInventoryToString());
        InventoryNames = names;
    }

    private static JSONArray SampleText(){
        JSONObject inv1 = new JSONObject();
        JSONObject Item1 = new JSONObject();
        JSONObject Item2 = new JSONObject();
        JSONObject Item3 = new JSONObject();
        JSONArray Inventory = new JSONArray();
        JSONObject inv2 = new JSONObject();
        JSONObject inv3 = new JSONObject();
        JSONObject[] testing = new JSONObject[]{inv1, inv2, inv3};
        try {
            for (int i = 0; i < testing.length; i++){
                Item1.put("date", String.valueOf(i));
                Item1.put("quantity",String.valueOf(i));
                Item1.put("needful","true");
                testing[i].put("Item1",Item1);
                testing[i].put("name","Sample Inventory "+String.valueOf(i));
                Inventory.put(testing[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Inventory;
    }


    public static List<String> GetInventoryNames(){
        if (InventoryNames == null || InventoryNames.size() == 0){
            return null;
        }
        else
            return InventoryNames;
    }

    private static List<InventoryItem> convertJSONObjectToInventoryItemList(JSONObject obj){
        Iterator<String> keys = obj.keys();
        List<InventoryItem> items = new ArrayList<InventoryItem>();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                if (obj.get(key) instanceof JSONObject) {
                    String itemName = key;
                    String date = ((JSONObject) obj.get(key)).getString("date");
                    String quantity = ((JSONObject) obj.get(key)).getString("quantity");
                    String needfulString = ((JSONObject) obj.get(key)).getString("needful");
                    boolean needful = Boolean.parseBoolean(needfulString);
                    items.add(new InventoryItem(itemName, date, quantity, needful));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return items;
    }

    private static String convertInventoryToString(){
        JSONArray inventoryArray = new JSONArray();
        for(int inventory = 0; inventory < InventoryNames.size(); inventory++){
            /* For each inventory list, create inventory object
            *  add inventory name to inventory object
            *  iterate over inventory items*/
            JSONObject inventoryObj = new JSONObject();
            String inventoryName = InventoryNames.get(inventory);
            List<InventoryItem> itemList = InventoryItems.get(inventory);
            try {
                inventoryObj.put("name", inventoryName);
                for (int item = 0; item<itemList.size(); item++){
                    /* For each item in inventory
                    *  get data from item class
                    *  put data in item object
                    *  add each item object to inventory obj*/
                    InventoryItem itemClass = itemList.get(item);
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("date", itemClass.getItemData());
                    itemObj.put("quantity", itemClass.getItemQuantity());
                    itemObj.put("needful", itemClass.getItemNeedfulString());
                    inventoryObj.put(itemClass.getItemName(), itemObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            inventoryArray.put(inventoryObj);
        }
        return inventoryArray.toString();
    }

    public static List<InventoryItem> GetInventoryItems(String inventoryName){
        int position = GetInventoryNames().indexOf(inventoryName);
        return InventoryItems.get(position);
    }

    public static void RemoveInventory(String inventoryName){
        int position = GetInventoryNames().indexOf(inventoryName);
        InventoryNames.remove(position);
        InventoryItems.remove(position);
    }

    public static void AddInventory(){
        /* New inventory name should not be a duplicate of an existing name */
        String defaultInventoryName;
        int counter = InventoryNames.size();
        do {
            defaultInventoryName = "Inventory #"+String.valueOf(counter++);
        }
        while (InventoryNames.contains(defaultInventoryName));
        InventoryNames.add(defaultInventoryName);
        InventoryItems.add(new ArrayList<InventoryItem>());
    }

    public static void AddInventoryItem(String inventoryName, InventoryItem newItem){
        int position = GetInventoryNames().indexOf(inventoryName);
        InventoryItems.get(position).add(newItem);
    }

    public static void RemoveItemFromInventory(String inventoryName, InventoryItem item){
        int position = GetInventoryNames().indexOf(inventoryName);
        InventoryItems.get(position).remove(item);
    }

    public static void RenameInventory(String oldName, String newName){
        int position = GetInventoryNames().indexOf(oldName);
        Collections.replaceAll(InventoryNames, oldName, newName);
    }

    public static int GetPositionOfInventory(String inventoryName){
        return GetInventoryNames().indexOf(inventoryName);
    }


}
