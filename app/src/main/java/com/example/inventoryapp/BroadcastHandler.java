package com.example.inventoryapp;

import static com.example.inventoryapp.GlobalActions.Alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

import java.util.Objects;

public class BroadcastHandler {
    public static android.content.BroadcastReceiver GetBatteryReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level == 10)
                Alert(context, "The battery is at "+String.valueOf(level)+" percent!");
            else if (level <= 5){
                Alert(context, "The battery is at "+String.valueOf(level)+" percent!");
            }
        }
    };

    public static class AppWideReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Did something", Toast.LENGTH_SHORT).show();
        }
    }

    public static BroadcastReceiver SampleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "here i is", Toast.LENGTH_SHORT).show();
            String emptyString = "";
            String inventory = intent.getExtras().getString("SharedInventory", emptyString);
            if (Objects.equals(inventory, emptyString))
                return;
            //Do something
            Toast.makeText(context, "Received inventory Broadcast!", Toast.LENGTH_SHORT).show();

        }
    };


}
