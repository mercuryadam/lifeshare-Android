package com.lifeshare.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lifeshare.R;
import com.lifeshare.ui.ui.AllPendingConnectionFragment;
import com.lifeshare.ui.ui.BroadcastFragment;
import com.lifeshare.ui.ui.HomeFragment;
import com.lifeshare.ui.ui.ViewProfileFragment;
import com.lifeshare.utils.Const;

public class DashboardActivity extends AppCompatActivity {

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
           *//* AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();*//*
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean(Const.FROM_NOTIFICATION, false)) {
                NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
                navGraph.setStartDestination(R.id.navigation_requests);
                navController.setGraph(navGraph);
            }
        }

    }*/

    Fragment fragment1 = new HomeFragment();
    final Fragment fragment2 = new BroadcastFragment();
    final Fragment fragment3 = new AllPendingConnectionFragment();
    Fragment fragment4 = new ViewProfileFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.nav_host_fragment, fragment4, "4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, fragment1, "1").commit();

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean(Const.FROM_NOTIFICATION, false)) {
                navigation.setSelectedItemId(R.id.navigation_connection);
            }
        }

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fm.beginTransaction().remove(fragment1);
                    fragment1 = new HomeFragment();
                    fm.beginTransaction().hide(active).add(R.id.nav_host_fragment, fragment1, "1").commit();
                    active = fragment1;
                    return true;
                case R.id.navigation_requests:
                    fm.beginTransaction().hide(active).show(fragment2).commit();
                    active = fragment2;
                    return true;

                case R.id.navigation_connection:
                    fm.beginTransaction().hide(active).show(fragment3).commit();
                    active = fragment3;
                    return true;

                case R.id.navigation_profile:
                    fm.beginTransaction().remove(fragment4);
                    fragment4 = new ViewProfileFragment();
                    fm.beginTransaction().hide(active).add(R.id.nav_host_fragment, fragment4, "4").commit();
                    active = fragment4;
                    return true;
            }
            return false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}