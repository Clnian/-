package com.autoexpense.tracker.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.ui.fragment.DashboardFragment;
import com.autoexpense.tracker.ui.fragment.StatisticsFragment;
import com.autoexpense.tracker.ui.fragment.TransactionsFragment;
import com.autoexpense.tracker.ui.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainContainerActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    
    // Fragment instances
    private DashboardFragment dashboardFragment;
    private TransactionsFragment transactionsFragment;
    private StatisticsFragment statisticsFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        
        initViews();
        setupBottomNavigation();
        
        // Show dashboard fragment by default
        if (savedInstanceState == null) {
            showDashboardFragment();
        }
    }
    
    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_dashboard) {
            showDashboardFragment();
            return true;
        } else if (itemId == R.id.nav_transactions) {
            showTransactionsFragment();
            return true;
        } else if (itemId == R.id.nav_statistics) {
            showStatisticsFragment();
            return true;
        } else if (itemId == R.id.nav_settings) {
            showSettingsFragment();
            return true;
        }
        
        return false;
    }
    
    private void showDashboardFragment() {
        if (dashboardFragment == null) {
            dashboardFragment = new DashboardFragment();
        }
        showFragment(dashboardFragment, "DASHBOARD");
    }
    
    private void showTransactionsFragment() {
        if (transactionsFragment == null) {
            transactionsFragment = new TransactionsFragment();
        }
        showFragment(transactionsFragment, "TRANSACTIONS");
    }
    
    private void showStatisticsFragment() {
        if (statisticsFragment == null) {
            statisticsFragment = new StatisticsFragment();
        }
        showFragment(statisticsFragment, "STATISTICS");
    }
    
    private void showSettingsFragment() {
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        showFragment(settingsFragment, "SETTINGS");
    }
    
    private void showFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide all fragments
        hideAllFragments(transaction);
        
        // Show or add the target fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        
        transaction.commit();
    }
    
    private void hideAllFragments(FragmentTransaction transaction) {
        if (dashboardFragment != null && dashboardFragment.isAdded()) {
            transaction.hide(dashboardFragment);
        }
        if (transactionsFragment != null && transactionsFragment.isAdded()) {
            transaction.hide(transactionsFragment);
        }
        if (statisticsFragment != null && statisticsFragment.isAdded()) {
            transaction.hide(statisticsFragment);
        }
        if (settingsFragment != null && settingsFragment.isAdded()) {
            transaction.hide(settingsFragment);
        }
    }
    
    @Override
    public void onBackPressed() {
        // If not on dashboard, go to dashboard
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_dashboard) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else {
            super.onBackPressed();
        }
    }
}
