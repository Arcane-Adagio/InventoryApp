package com.example.inventoryapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ServerHandler {
    private final static String TAG = "ServerHandler";

    private final static String website = "http://10.0.3.2/inventoryapp/";
    private final static String webapi_addaccount = "http://10.0.3.2/inventoryapp/api/accounts/add";
    private final static String webapi_changeusername = "http://10.0.3.2/inventoryapp/api/accounts/changeusername";
    private final static String webapi_changepassword = "http://10.0.3.2/inventoryapp/api/accounts/changepassword";
    private final static String webapi_deleteaccount = "http://10.0.3.2/inventoryapp/api/accounts/remove";
    private final static String webapi_login = "http://10.0.3.2/inventoryapp/api/login";
    private final static String webapi_accounts = "http://10.0.3.2/inventoryapp/api/accounts";
    private final static String webapi_saveinventory = "http://10.0.3.2/inventoryapp/api/accounts/saveinventory";


    public static boolean CreateUser(String username, String password){
        JSONObject newuser = constructUserObject(username, password);
        return ServerHelper.sendHttpPostRequest(webapi_addaccount, newuser);
    }

    public static String GetListOfAccountsInDatabase(){
        return ServerHelper.downloadJSONusingHTTPGetRequest(webapi_accounts);
    }

    final static class Login extends AsyncTask<Void, Void, Pair<Boolean, String>> {
        //performs login into server
        private final WeakReference<Activity> parentRef;
        private final String mUsername, mPassword;

        public Login(final Activity parent, String uname, String pass) {
            parentRef = new WeakReference<Activity>(parent); mUsername = uname; mPassword = pass;
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... args) {
            Pair<Boolean, String> returnValues;
            String getURL = webapi_login+"?username="+mUsername+"&password="+mPassword;
            String response = ServerHelper.downloadJSONusingHTTPGetRequest(getURL);
            returnValues = loginResponseParser(response);
            return returnValues;
        }

        @Override /* Run on the UI thread */
        protected void onPostExecute(Pair<Boolean, String> loginSuccessful) {
            Activity parent = parentRef.get();
            if(!loginSuccessful.first)
                Toast.makeText(parent.getApplicationContext(), loginSuccessful.second, Toast.LENGTH_SHORT).show();
            else{
                //perform login
                User.initializeUserFromOnlineDB(loginSuccessful.second);
                GlobalActions.NavigateToActivity(parent, InventoryActivity.class);
            }
        }
    }

    final static class ChangeAccountUsername extends AsyncTask<Void, Void, Void> {
        private final String mUsername, mNewUsername, mPassword;
        public ChangeAccountUsername(String uname, String pass, String newUname){ mUsername = uname; mPassword = pass; mNewUsername = newUname;}
        @Override
        protected Void doInBackground(Void... args) {
            JSONObject postParameters = new JSONObject();
            try {
                postParameters.put("oldusername", mUsername);
                postParameters.put("newusername", mNewUsername);
                postParameters.put("password", mPassword);
                ServerHelper.sendHttpPostRequest(webapi_changeusername, postParameters);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    final static class ChangeAccountPassword extends AsyncTask<Void, Void, Void> {
        private final String mUsername, mPassword, mNewPassword;
        public ChangeAccountPassword(String uname, String pass, String newPass){ mUsername = uname; mPassword = pass; mNewPassword = newPass;}
        @Override
        protected Void doInBackground(Void... args) {
            JSONObject postParameters = new JSONObject();
            try {
                postParameters.put("username", mUsername);
                postParameters.put("oldpassword", mPassword);
                postParameters.put("newpassword", mNewPassword);
                ServerHelper.sendHttpPostRequest(webapi_changepassword, postParameters);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    final static class SaveInventory extends AsyncTask<Void, Void, Void> {
        private final String mUsername, mPassword, mInventory;
        public SaveInventory(String uname, String pass, String inven){ mUsername = uname; mPassword = pass; mInventory = inven;}
        @Override
        protected Void doInBackground(Void... args) {
            JSONObject postParameters = new JSONObject();
            try {
                postParameters.put("username", mUsername);
                postParameters.put("password"  , mPassword);
                postParameters.put("inventory", mInventory);
                ServerHelper.sendHttpPostRequest(webapi_saveinventory, postParameters);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    final static class DeleteAccount extends AsyncTask<Void, Void, Boolean> {
        private final String mUsername, mPassword;
        public DeleteAccount(String uname, String pass){ mUsername = uname; mPassword = pass;}
        @Override
        protected Boolean doInBackground(Void... args) {
            JSONObject newuser = constructUserObject(mUsername, mPassword);
            return ServerHelper.sendHttpPostRequest(webapi_deleteaccount, newuser);
        }
    }

    public static Pair<Boolean, String> loginResponseParser(String response){
        //setup return values
        if(response == null)
            return new Pair<>(false, "");
        Boolean loginSuccessful = false;
        String resultResponse = "";
        Pair<Boolean, String> returnValues;
        //update return variables
        try {
            JSONArray responseArray = new JSONArray(response);
            String result = getResultText(responseArray.getJSONObject(responseArray.length() - 1)); //should always be at the end
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

    public static String getResultText(JSONObject resultJSON){
        try {
            return resultJSON.getJSONObject("Result").getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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

    final static class workerGetRequester extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return ServerHelper.downloadJSONusingHTTPGetRequest(urls[0]);
        }
    }
}

