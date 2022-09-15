package com.example.inventoryapp.offline;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.InventoryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

/* This file, an abstract class inherits from the abstract class inventoryFragment to
* provide specific reusable functions to fragments that do not use internet capabilities
*  */

public class OfflineFragment extends InventoryFragment {
    public interface SimpleCallback{ void CallableFunction(String[] args);}
    public interface MenuCallback {
        int customMenuOptions();
        boolean customOnItemSelected(MenuItem menuItem);
    }

    public void SetupBottomNav(){
        BottomNavigationView nav = getActivity().findViewById(R.id.bottomnav_app);
        MenuItem item = nav.getMenu().findItem(R.id.offlineInventoryFragment);
        item.setChecked(true);
    }

    public static class OfflineFragmentHandler implements MenuProvider {
        private Fragment callingFragment;
        private MenuCallback menuCallback = null;

        public OfflineFragmentHandler(Fragment fragment){
            callingFragment = fragment;
        }

        public OfflineFragmentHandler(Fragment fragment, MenuCallback fragmentCallback){
            callingFragment = fragment;
            menuCallback = fragmentCallback;
        }

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            if(menuCallback != null)
                menuInflater.inflate(menuCallback.customMenuOptions(), menu);
            menuInflater.inflate(R.menu.home_appbar_menu, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            //if class was started without custom behavior, perform default action
            if(menuCallback == null)
                return DefaultMenuOptionSelection(menuItem, callingFragment.getContext(), callingFragment);
            //otherwise, handle custom callback
            if(menuCallback.customOnItemSelected(menuItem))
                return true;
            return DefaultMenuOptionSelection(menuItem, callingFragment.getContext(), callingFragment);
        }
    }
}
