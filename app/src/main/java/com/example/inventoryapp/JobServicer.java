package com.example.inventoryapp;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.widget.Toast;

public class JobServicer {
    public class TestJobService extends JobService {

        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            //Jobs should be implemented on a seperate thread or service
            Toast.makeText(this, "Starting job", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }

    public static void TestJob(Context context){

    }
}
