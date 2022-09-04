package com.example.inventoryapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.alarm_sample_text), Toast.LENGTH_LONG).show();
    }

    public void setAlarmTwentyFromNow(Context context)
    {
        //requests an alarm service to convert as an alarm manager
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //creates a pending intent
        Intent i = new Intent(context, AlarmHandler.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        //makes alarm wakeup the device to perform action every 20 minutes
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*20, pi);
    }
    public void setAlarmAt(Context context, int h, int m)
    {
        /* uses the calendar widget to set the exact time to trigger alarm */
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmHandler.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    }
    public void setCustomAlarm(Context context, int seconds)
    {
        /* Sets off alarm in x (provided) seconds */
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmHandler.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(1000*seconds), pi);
    }
    public void cancelAlarm(Context context)
    {
        //Cancels scheduled alarm via submitted pending intent submitted to the system
        Intent intent = new Intent(context, AlarmHandler.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
