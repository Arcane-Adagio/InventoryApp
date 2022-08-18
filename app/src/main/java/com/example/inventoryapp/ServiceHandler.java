package com.example.inventoryapp;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.security.Provider;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServiceHandler extends IntentService {
    static final String TAG = "ServiceHandler";

    public ServiceHandler(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: serviceStart");
        String emptyString = "";
        String inventory = intent.getExtras().getString(GlobalActions.KEY_SHAREDINVENTORY, emptyString);
        if (Objects.equals(inventory, emptyString))
            return;
        Intent intent1 = new Intent(GlobalActions.EXPORT_ACTION);
        intent1.putExtra(GlobalActions.KEY_SHAREDINVENTORY, inventory);
        sendBroadcast(intent1);
        Log.d(TAG, "onHandleIntent: serviceEnd, Inventory: "+inventory);
    }
}
