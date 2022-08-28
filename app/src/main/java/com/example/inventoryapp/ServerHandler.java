package com.example.inventoryapp;

import android.database.Cursor;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerHandler {
    private final static String TAG = "ServerHandler";
    private final static String website = "http://10.0.3.2/inventoryapp/";
    private final static String webapi_addaccount = "http://10.0.3.2/inventoryapp/api/accounts/add";
    private final static String webapi_changeusername = "http://10.0.3.2/inventoryapp/api/accounts/changeusername";
    private final static String webapi_changepassword = "http://10.0.3.2/inventoryapp/api/accounts/changepassword";
    private final static String webapi_deleteaccount = "http://10.0.3.2/inventoryapp/api/accounts/remove";
    private final static String webapi_login = "http://10.0.3.2/inventoryapp/api/login";
    private final static String webapi_accounts = "http://10.0.3.2/inventoryapp/api/accounts";

    
    public static boolean CreateUser(String username, String password){
        JSONObject newuser = constructUserObject(username, password);
        return ServerHelper.sendHttpPostRequest(webapi_addaccount, newuser);
    }

    public static Pair<Boolean, String> Login(String username, String password){
        Pair<Boolean, String> returnValues;
        String getURL = webapi_login+"?username="+username+"&password="+password;
        String response = ServerHelper.downloadJSONusingHTTPGetRequest(getURL);
        returnValues = loginResponseParser(response);
        return returnValues;
    }

    public static String GetListOfAccountsInDatabase(){
        return ServerHelper.downloadJSONusingHTTPGetRequest(webapi_accounts);
    }


    public static boolean DeleteAccount(String username, String password){
        JSONObject newuser = constructUserObject(username, password);
        return ServerHelper.sendHttpPostRequest(webapi_deleteaccount, newuser);
    }

    public static Pair<Boolean, String> loginResponseParser(String response){
        //setup return values
        Boolean loginSuccessful = false;
        String resultResponse = "";
        Pair<Boolean, String> returnValues;
        //update return variables
        try {
            JSONArray responseArray = new JSONArray(response);
            JSONObject resultJSON = responseArray.getJSONObject(responseArray.length() - 1); //should always be at the end
            String result = resultJSON.getJSONObject("Result").getString("text");
            if(result.equals("login successful")){
                loginSuccessful = true;
                resultResponse = responseArray.getJSONObject(0).toString();
                returnValues = new Pair<>(loginSuccessful, resultResponse);
            }
            else{
                loginSuccessful = false;
                resultResponse = result;
                returnValues = new Pair<>(loginSuccessful, resultResponse);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            returnValues = new Pair<>(false, "unknown error occurred");
        }
        return returnValues;
    }

    public static JSONObject constructUserObject(String username, String password){
        JSONObject userobject = new JSONObject();
        try {
            userobject.put("username", username);
            userobject.put("password", password);
            return userobject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

