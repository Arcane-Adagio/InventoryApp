package com.example.inventoryapp.extras;

import static com.example.inventoryapp.GlobalActions.Alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;

import java.util.Objects;


/* This class houses broadcasts receivers */

public class BroadcastHandler {
    public static android.content.BroadcastReceiver GetBatteryReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* This receiver monitors battery broadcasts to display an alert to a logged in user */
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level == 10 || (level > 0 && level <= 5))
                Alert(context, context.getString(R.string.alert_battery_prt1)+String.valueOf(level)+context.getString(R.string.alert_battery_prt2));
        }
    };

    public static class PowerReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* This receiver is called when power is connected to the device */
            JobServicer.ScheduleTestJob(context.getApplicationContext());
        }
    };

    public static class AppWideReceiver extends android.content.BroadcastReceiver {
        /* This func would be used to handle broadcasts that the app receives
        * The function is referenced in the manifest to activate app-wide (if i
        * ever get around to figuring out how that works) */
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, context.getString(R.string.toast_incomingbroadcast), Toast.LENGTH_SHORT).show();
        }
    }

    public static BroadcastReceiver SharedInventoryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* This receiver, in production, would be used to listen for
            * inventory lists to add to the UI. */
            String emptyString = "";
            String inventory = intent.getExtras().getString(GlobalActions.KEY_SHAREDINVENTORY, emptyString);
            if (Objects.equals(inventory, emptyString))
                return;
            //Do something
            Toast.makeText(context, context.getString(R.string.toast_broadcastreceived), Toast.LENGTH_SHORT).show();
        }
    };
}
