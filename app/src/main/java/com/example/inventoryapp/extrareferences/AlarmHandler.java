package com.example.inventoryapp.extrareferences;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.inventoryapp.R;

import java.util.Calendar;
import java.util.concurrent.Callable;

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

    public static class MyAlertDialogFragmentWithCustomLayout extends DialogFragment {
        static Callable<Void> positiveCallback;

        public static MyAlertDialogFragmentWithCustomLayout newInstance(int layout, Callable<Void> positiveFunc){
            MyAlertDialogFragmentWithCustomLayout frag = new MyAlertDialogFragmentWithCustomLayout();
            positiveCallback = positiveFunc;
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            int layout = getArguments().getInt("layout");
            final int list = getArguments().getInt("list");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(layout, null);
            return builder.setView(view)
                    .setPositiveButton("Create", (dialogInterface, i) -> {
                        try {
                            positiveCallback.call();
                        } catch (Exception e) {
                            Log.d("error", "onCreateDialog: "+e.toString());
                            e.printStackTrace();
                        }
                    }).setNegativeButton("cancel", (dialogInterface, i) -> {
                        //do nothing
                    }).create();
        }
    }
}
