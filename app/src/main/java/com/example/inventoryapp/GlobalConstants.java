package com.example.inventoryapp;

import java.util.ArrayList;
import java.util.List;

public class GlobalConstants {
    public static final List<String> db_emptyValues =
            new ArrayList<String>(){{add("None"); add("null"); add(""); add("[]");}};
    public static final String db_loginSuccessful = "Login Successful";
    public static final int db_max_groupname_length = 15;
    public static final int db_max_code_length = 15;
    public static final int db_max_password_length = 15;
}
