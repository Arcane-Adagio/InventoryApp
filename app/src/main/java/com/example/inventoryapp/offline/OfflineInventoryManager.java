package com.example.inventoryapp.offline;

/* This class Follows the Singleton pattern to represent local inventory */

import static android.content.Context.MODE_PRIVATE;

import static com.example.inventoryapp.GlobalConstants.SHARED_PREF_FILENAME;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.InventoryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OfflineInventoryManager {


    public static List<JSONObject> InventoryJSONs = new ArrayList<>();
    public static List<String> InventoryNames = new ArrayList<>();
    private static final String TAG = "Offline Inventory Manager";
    public static List<List<InventoryItem>> InventoryItems = new ArrayList<>();
    private static final String ITEM_ATTRIBUTE_NAME = "name";
    private static final String ITEM_ATTRIBUTE_DATE = "date";
    private static final String ITEM_ATTRIBUTE_QUANTITY = "quantity";
    private static final String ITEM_ATTRIBUTE_NEEDFUL = "needful";

    private OfflineInventoryManager() {}

    public static String getInventoryJSON(){
        if(InventoryNames == null || InventoryNames.size() == 0)
            return "";
        else
            return convertInventoryToString();
    }

    public static void ImportJSONStringToInventory(String inventoryStringFromDatabase){
        JSONArray arr;
        List<String> names = new ArrayList<>();
        try {
            arr = new JSONArray(inventoryStringFromDatabase);
            for (int i=0; i<arr.length(); i++){
                //add each json object to object list
                InventoryJSONs.add(arr.getJSONObject(i));

                //Add each inventory object name to class list
                names.add(arr.getJSONObject(i).getString(ITEM_ATTRIBUTE_NAME));

                //Convert each inventory's items to lists and store them
                InventoryItems.add(convertJSONObjectToInventoryItemList(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        InventoryNames = names;
    }


    public static List<String> GetInventoryNames(){
        return (InventoryNames == null || InventoryNames.size() == 0) ? new ArrayList<>() : InventoryNames;
    }

    private static List<InventoryItem> convertJSONObjectToInventoryItemList(JSONObject obj){
        Iterator<String> keys = obj.keys();
        List<InventoryItem> items = new ArrayList<>();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                if (obj.get(key) instanceof JSONObject) {
                    String date = ((JSONObject) obj.get(key)).getString(ITEM_ATTRIBUTE_DATE);
                    String quantity = ((JSONObject) obj.get(key)).getString(ITEM_ATTRIBUTE_QUANTITY);
                    String needfulString = ((JSONObject) obj.get(key)).getString(ITEM_ATTRIBUTE_NEEDFUL);
                    boolean needful = Boolean.parseBoolean(needfulString);
                    items.add(new InventoryItem(key, date, quantity, needful));
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
                inventoryObj.put(ITEM_ATTRIBUTE_NAME, inventoryName);
                for (int item = 0; item<itemList.size(); item++){
                    /* For each item in inventory
                    *  get data from item class
                    *  put data in item object
                    *  add each item object to inventory obj*/
                    InventoryItem itemClass = itemList.get(item);
                    JSONObject itemObj = new JSONObject();
                    itemObj.put(ITEM_ATTRIBUTE_DATE, itemClass.getItemData());
                    itemObj.put(ITEM_ATTRIBUTE_QUANTITY, itemClass.getItemQuantity());
                    itemObj.put(ITEM_ATTRIBUTE_NEEDFUL, itemClass.getItemNeedfulString());
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

    public static boolean AddInventoryAndNotifyAdapter(InventoryRVAdapter adapter, String proposedInventoryName){
        /* New inventory name should not be a duplicate of an existing name */
        String defaultInventoryName;
        int counter = InventoryNames.size();

        if (InventoryNames.contains(proposedInventoryName))
            return false;
        InventoryNames.add(proposedInventoryName);
        InventoryItems.add(new ArrayList<>());
        if(adapter!=null){
            adapter.NotifyElementAdded();
            return true;
        }
        else
            Log.d(TAG, "AddInventoryAndNotifyAdapter: adapter was null");
        return false;
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
        Collections.replaceAll(InventoryNames, oldName, newName);
    }

    public static int GetPositionOfInventory(String inventoryName){
        return GetInventoryNames().indexOf(inventoryName);
    }

    public static void SaveUserInventory(Context context){
        SharedPreferences pref = ((Activity)context).getSharedPreferences(SHARED_PREF_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(context.getString(R.string.pref_inventory_key), getInventoryJSON());
        editor.commit();
    }

    public static void LoadUserInventory(Context context){
        SharedPreferences pref = ((Activity)context).getSharedPreferences(SHARED_PREF_FILENAME, MODE_PRIVATE);
        String savedString = pref.getString(context.getString(R.string.pref_inventory_key),"");
        ImportJSONStringToInventory(savedString);
    }

}
