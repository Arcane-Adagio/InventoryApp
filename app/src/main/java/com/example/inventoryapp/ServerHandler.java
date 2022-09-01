package com.example.inventoryapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import kotlin.Triple;

public class ServerHandler {
    private final static String TAG = "ServerHandler";

    private final static String website = "http://10.0.3.2/inventoryapp/";
    private final static String webapi_addaccount = "http://10.0.3.2/inventoryapp/api/accounts/add";
    private final static String webapi_changeusername = "http://10.0.3.2/inventoryapp/api/accounts/changeusername";
    private final static String webapi_changepassword = "http://10.0.3.2/inventoryapp/api/accounts/changepassword";
    private final static String webapi_deleteaccount = "http://10.0.3.2/inventoryapp/api/accounts/remove";
    private final static String webapi_login = "http://10.0.3.2/inventoryapp/api/login";
    private final static String webapi_accounts = "http://10.0.3.2/inventoryapp/api/accounts";
    private final static String webapi_iscodevalid = "http://10.0.3.2/inventoryapp/api/groups/code";
    private final static String webapi_saveinventory = "http://10.0.3.2/inventoryapp/api/accounts/saveinventory";
    private final static String webapi_getMyIP = "http://10.0.3.2/inventoryapp/api/testme";
    private final static String webapi_creategroup = "http://10.0.3.2/inventoryapp/api/groups/add";
    private final static String webapi_getgroupinventorys = "http://10.0.3.2/inventoryapp/api/groups/getAllInventory";


    public static Pair<Boolean, String> CreateUser(String username, String password){
        /* Commmunicates with server and returns a boolean indicating whether there was an error connecting
        * with the server and a String indicating the response from the server */
        JSONObject newuser = constructUserObject(username, password);
        Pair<Boolean, String> response = ServerHelper.sendHttpPostRequest(webapi_addaccount, newuser);
        return new Pair<>(response.first, getResultText(response.second));
    }

    public static String GetListOfAccountsInDatabase(){
        return ServerHelper.downloadJSONusingHTTPGetRequest(webapi_accounts);
    }

    final static class Login extends AsyncTask<Void, Void, Triple<Boolean, String, String>> {
        //performs login into server
        private final WeakReference<Activity> parentRef;
        private final String mUsername, mPassword;

        public Login(final Activity parent, String uname, String pass) {
            parentRef = new WeakReference<Activity>(parent); mUsername = uname; mPassword = pass;
        }

        @Override
        protected Triple<Boolean, String, String> doInBackground(Void... args) {
            String url = webapi_login+"?username="+mUsername+"&password="+mPassword;
            String loginResponse = ServerHelper.downloadJSONusingHTTPGetRequest(url);
            Pair<Boolean, String> loginResult = loginResponseParser(loginResponse);
            if(loginResult.first){
                url = webapi_getgroupinventorys+"?username="+mUsername+"&password="+mPassword;
                String groupInventorys = ServerHelper.downloadJSONusingHTTPGetRequest(url);
                return new Triple<>(loginResult.first, loginResult.second, groupInventorys);
            }
            return new Triple<>(loginResult.first, loginResult.second, "");
        }

        @Override /* Run on the UI thread */
        protected void onPostExecute(Triple<Boolean, String, String> loginSuccessful) {
            Activity parent = parentRef.get();
            if(!loginSuccessful.getFirst())
                Toast.makeText(parent.getApplicationContext(), loginSuccessful.getSecond(), Toast.LENGTH_SHORT).show();
            else{
                //perform login
                User.initializeUserFromOnlineDB(loginSuccessful.getSecond(), loginSuccessful.getThird());
                GlobalActions.NavigateToActivity(parent, InventoryActivity.class);
            }
        }
    }

    final static class isGroupCodeValid extends AsyncTask<Void, Void, Boolean> {
        private final String mGroupCode;
        private final WeakReference<EditText> mTextview;
        public isGroupCodeValid(String groupCode, EditText textview)
        {
            mGroupCode = groupCode;
            mTextview = new WeakReference<>(textview);
        }
        @Override
        protected Boolean doInBackground(Void... args) {
            String fullURL = webapi_iscodevalid+"?group_code="+mGroupCode;
            String result = getResultText(ServerHelper.downloadJSONusingHTTPGetRequest(fullURL));
            return (Objects.equals(result, "true") || Objects.equals(result, "True"));
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            EditText codeEditText = mTextview.get();
            if(isValid){
                codeEditText.getBackground().clearColorFilter();
            }
            else {
                codeEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                codeEditText.setText("");
                codeEditText.setHint("Group Code Already Taken");
            }
        }
    }

    final static class CreateGroup extends AsyncTask<Void, Void, Pair<Boolean, String>> {
        private final String mCode, mGroupName, mGroupPassword;
        private final WeakReference<Context> mContext;
        public CreateGroup(String code, String groupName, String groupPassword, Context context) {
            mCode = code; mGroupName = groupName; mGroupPassword = groupPassword; mContext = new WeakReference<>(context);
        }
        @Override
        protected Pair<Boolean, String> doInBackground(Void... args) {
            JSONObject postParameters = new JSONObject();
            boolean result1 = false;
            String result2 = "Encountered Local Error";
            try {
                postParameters.put("username", User.getUsername());
                postParameters.put("password", User.getPassword());
                postParameters.put("group_code", mCode);
                postParameters.put("group_name", mGroupName);
                postParameters.put("group_password", mGroupPassword);
                Pair<Boolean, String> response = ServerHelper.sendHttpPostRequest(webapi_creategroup, postParameters);
                result1 = response.first;
                result2 = getResultText(response.second);
                Log.d(TAG, "doInBackground: "+result2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new Pair<>(result1, result2);
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            Toast.makeText(mContext.get(), result.second, Toast.LENGTH_SHORT).show();
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

    final static class SaveInventory extends AsyncTask<Context, Void, Pair<Boolean, String>> {
        private final String mUsername, mPassword, mInventory;
        private final Context mContext;
        public SaveInventory(String uname, String pass, String inven, Context context)
        { mUsername = uname; mPassword = pass; mInventory = inven; mContext = context; }
        @Override
        protected Pair<Boolean, String> doInBackground(Context... args) {
            JSONObject postParameters = new JSONObject();
            try {
                postParameters.put("username", mUsername);
                postParameters.put("password"  , mPassword);
                postParameters.put("inventory", mInventory);
                Log.d(TAG, "Saving inventory: "+mInventory);
                return ServerHelper.sendHttpPostRequest(webapi_saveinventory, postParameters);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: JSON parsing error");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> RequestProcessed) {
            Toast.makeText(mContext, getResultText(RequestProcessed.second), Toast.LENGTH_SHORT).show();
        }
    }

    final static class DeleteAccount extends AsyncTask<Void, Void, Boolean> {
        private final String mUsername, mPassword;
        public DeleteAccount(String uname, String pass){ mUsername = uname; mPassword = pass;}
        @Override
        protected Boolean doInBackground(Void... args) {
            JSONObject newuser = constructUserObject(mUsername, mPassword);
            return ServerHelper.sendHttpPostRequest(webapi_deleteaccount, newuser).first;
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
            if(result.equals(GlobalConstants.db_loginSuccessful)){
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

    public static String getResultText(String responseString){
        try {
            JSONArray responseArray = new JSONArray(responseString);
            JSONObject resultJSON = responseArray.getJSONObject(responseArray.length() - 1);
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

    final static class TestServerConnection extends AsyncTask<Void, Integer, String> {
        /* Function used for online debugging */
        private final WeakReference<Activity> parentRef;

        public TestServerConnection(final Activity parent)
        {
            parentRef = new WeakReference<Activity>(parent);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return ServerHelper.downloadJSONusingHTTPGetRequest(webapi_getMyIP);
        }

        @Override
        protected void onPostExecute(String db_result)
        {
            Activity parent = parentRef.get();

            try
            {
                //JSONArray jsonArray = new JSONArray(db_result);
                //ArrayList<String> a = new ArrayList<String>();
                Toast.makeText(parent, db_result, Toast.LENGTH_SHORT).show();
            }
            catch(Exception ex)
            {
                Log.d("JSONObject", "You had an exception");
                ex.printStackTrace();
            }
        }
    }
}

