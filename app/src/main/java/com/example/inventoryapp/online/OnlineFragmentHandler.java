package com.example.inventoryapp.online;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OnlineFragmentHandler implements MenuProvider{
    private Fragment callingFragment;

    public OnlineFragmentHandler(Fragment fragment) {
        callingFragment = fragment;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.online_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return GlobalActions.DefaultMenuOptionSelection(menuItem, callingFragment.getContext(), callingFragment);
    }

}
