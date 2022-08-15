package com.example.inventoryapp;

/* This class Follows the Singleton pattern to represent the logged in user */

public class User {

    private static User INSTANCE = null;
    private static String mUsername;
    public static String mInventorys;

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

    public static void setUsername(String username){
        //Username should only be allowed to be set
        //when null
        if(INSTANCE == null)
            INSTANCE = new User();
        if (mUsername == null)
            mUsername = username;
    }

    public static void LogoutUser(){
        if(INSTANCE == null)
            INSTANCE = new User();
        mUsername = null;
    }

}
