package com.example.inventoryapp.offline;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;

public class OfflineFragmentHandler implements MenuProvider {
    public interface Callback{
        int customMenuOptions();
        boolean customOnItemSelected(MenuItem menuItem);
    }
    private Fragment callingFragment;
    private Callback callback = null;

    public OfflineFragmentHandler(Fragment fragment){
        callingFragment = fragment;
    }

    public OfflineFragmentHandler(Fragment fragment, Callback fragmentCallback){
        callingFragment = fragment;
        callback = fragmentCallback;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        if(callback != null)
            menuInflater.inflate(callback.customMenuOptions(), menu);
        menuInflater.inflate(R.menu.home_appbar_menu, menu);
    }


    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        //if class was started without custom behavior, perform default action
        if(callback == null)
            return GlobalActions.DefaultMenuOptionSelection(menuItem, callingFragment.getContext(), callingFragment);
        //otherwise, handle custom callback
        if(callback.customOnItemSelected(menuItem))
            return true;
        return GlobalActions.DefaultMenuOptionSelection(menuItem, callingFragment.getContext(), callingFragment);
    }
}
