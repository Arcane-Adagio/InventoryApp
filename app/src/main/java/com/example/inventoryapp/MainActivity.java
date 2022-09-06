package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView;
    public static int fragmentContainerID = R.id.fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onStart() {
        super.onStart();
        navView = (BottomNavigationView) findViewById(R.id.bottomnav_app);
        getSupportFragmentManager().beginTransaction().replace(fragmentContainerID, new OfflineFragment()).commit();
        navView.setSelectedItemId(R.id.nav_offline);
        navView.setOnNavigationItemSelectedListener(GetListener());
    }


    private BottomNavigationView.OnNavigationItemSelectedListener GetListener(){
        return new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.nav_offline:
                        fragment = new OfflineFragment();
                        break;
                    case R.id.nav_online:
                        fragment = new OnlineLoginFragment();
                        break;
                    default:
                        fragment = new SettingsFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(fragmentContainerID, fragment).commit();
                return true;
            }
        };
    }
}