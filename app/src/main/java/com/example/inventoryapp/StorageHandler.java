package com.example.inventoryapp;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageHandler {
    private Activity mCallingActivity;
    private final String TAG = "StorageHandler";
    private final String mFolderName = "InventoryAppFolder";
    private final String mFileName = "ExportedInventory.txt";


    StorageHandler(Activity activity){
        mCallingActivity = activity;
    }

    public void WriteToFile(){
        String inventoryText = User.getInventoryJSON();
        //Seeks permission and checks hardware write-ability before
        //performing file writing
        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            if(isExternalStorageWritable()){
                performWrite(inventoryText);
            }
            else{
                Log.d(TAG, "WriteToFile: truly is not writable!");
                makeToast(mCallingActivity.getString(R.string.Toast_StorageNotWritable));
            }
        }
        else{
            ActivityCompat.requestPermissions(mCallingActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
            if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isExternalStorageWritable()){
                performWrite(inventoryText);
            }
            else{
                Log.d(TAG, "Needs Storage Permission Or Not Writable");
                makeToast(mCallingActivity.getString(R.string.Toast_StorageNotWritable));
            }
        }
    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.d(TAG, "Yes, it is writable!");
            return true;
        }else{
            return false;
        }
    }

    private void performWrite(String noteText){
        //file writing execution
        File textFile = new File(commonDocumentDirPath(mFolderName), mFileName);
        try {
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write(noteText.getBytes());
            fos.close();
            makeToast(mCallingActivity.getString(R.string.Toast_FileSaved));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "WriteFile: "+e.toString());
        }
    }

    public static File commonDocumentDirPath(String FolderName) {
        /* Incorporated Scoped Storage
        *  Returns a path that can be wrote to
        * */
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + FolderName);
        else
            dir = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success) {
                dir = null;
            }
        }
        return dir;
    }

    private boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(mCallingActivity, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void makeToast(String msg){
        Toast.makeText(mCallingActivity, msg, Toast.LENGTH_SHORT).show();
    }
}
