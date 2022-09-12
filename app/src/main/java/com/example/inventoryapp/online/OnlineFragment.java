package com.example.inventoryapp.online;

/* This is the base class for online fragments */

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.inventoryapp.GlobalActions;
import com.example.inventoryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class OnlineFragment extends Fragment implements FirebaseHandler.OnlineFragmentBehavior{
    private final String TAG = "Base Online Fragment Class";
    public static FirebaseUser currentUser;
    public static String currentGroupID;
    public static String currentInventoryID;
    public static DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();


    public void SetupBottomNav(){
        BottomNavigationView nav = getActivity().findViewById(R.id.bottomnav_app);
        MenuItem item = nav.getMenu().findItem(R.id.onlineLoginFragment);
        item.setChecked(true);
    }

    public void RenameAppBar(String newName){
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).setTitle(newName);
    }

    @Override
    public void HandleFragmentInvalidation() {
        /* Override this class to provide alert, then call super */
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_onlineFragment_to_onlineLoginFragment);
    }

    @Override
    public void HandleInventoryInvalidation() {
        //do something
    }

    public static class OnlineMenuProvider implements MenuProvider {
        private final Fragment callingFragment;

        public OnlineMenuProvider(Fragment fragment) {
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
}
