package com.example.inventoryapp.extras;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

public class JobServicer {
    public static class TestJobService extends JobService {

        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            //Jobs should be implemented on a seperate thread or service
            Toast.makeText(this, "Starting job: Storage is not low", Toast.LENGTH_SHORT).show();
            return true;
        }



        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            Toast.makeText(this, "Job Interrupted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public static void ScheduleTestJob(Context context){

        //indicates which job service to run
        ComponentName serviceComponent = new ComponentName(context, TestJobService.class);
        //creates job info object
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        //waits one second before starting
        builder.setMinimumLatency(1000);
        //starts within 3 seconds
        builder.setOverrideDeadline(3000);
        //makes sure the storage is not low
        builder.setRequiresStorageNotLow(true);
        //specifies to the system to use built-in job scheduler
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        //provides job info to job scheduler
        jobScheduler.schedule(builder.build());
    }
}
