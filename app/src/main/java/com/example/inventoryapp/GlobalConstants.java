package com.example.inventoryapp;

import java.util.ArrayList;
import java.util.List;

public class GlobalConstants {
    public static final List<String> db_emptyValues =
            new ArrayList<String>(){{add("None"); add("null"); add(""); add("[]");}};
    public static final String db_loginSuccessful = "Login Successful";
    public static final int db_max_groupname_length = 15;
    public static final int db_max_code_length = 15;
    public static boolean online = true;
    public static final String EXPORT_ACTION = "com.example.inventoryapp.share";
    public static final int db_max_password_length = 15;
    public static final String FRAGMENT_ARG_INVENTORY_NAME = "inventoryName";
    public static final String SHARED_PREF_FILENAME = "com.example.inventoryapp.LOCALINVENTORY";

}
