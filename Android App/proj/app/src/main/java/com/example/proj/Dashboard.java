package com.example.proj;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.appcompat.widget.Toolbar;

import com.example.proj.Fragments.ProfileFragment;
import com.example.proj.Fragments.SettingsFragment;
import com.example.proj.Fragments.ToolsFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        AppBarLayout topAppBarLayout=findViewById(R.id.topAppBarLayout);

        // initializing the bottom navigation bar
        BottomNavigationView navigationView=findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.profileBottom);

        // initializing the 3 fragments for the bottom navigation
        ProfileFragment profileFragment=new ProfileFragment();
        ToolsFragment toolsFragment=new ToolsFragment();
        SettingsFragment settingsFragment=new SettingsFragment();

        // setting the default fragment to profile fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, profileFragment).commit();

        // on item selection listener
        navigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.profileBottom:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, profileFragment).commit();
                    //topAppBarLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.toolsBottom:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, toolsFragment).commit();
                    //topAppBarLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.settingsBottom:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
                    //topAppBarLayout.setVisibility(View.GONE);
                    break;
                default:
                    return false;
            }
            return true;
        });

    }
}