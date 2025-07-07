package com.autoexpense.tracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.utils.AccessibilityUtils;
import com.autoexpense.tracker.utils.NotificationUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            setupAccessibilityPreference();
            setupNotificationPreference();
            setupAutoTrackingPreference();
        }

        private void setupAccessibilityPreference() {
            Preference accessibilityPref = findPreference("accessibility_service");
            if (accessibilityPref != null) {
                accessibilityPref.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(getContext(), "请在无障碍设置中启用自动记账服务", Toast.LENGTH_LONG).show();
                    return true;
                });

                // 更新状态
                updateAccessibilityStatus();
            }
        }

        private void setupNotificationPreference() {
            Preference notificationPref = findPreference("notification_access");
            if (notificationPref != null) {
                notificationPref.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(getContext(), "请启用通知访问权限以监听银行通知", Toast.LENGTH_LONG).show();
                    return true;
                });

                // 更新状态
                updateNotificationStatus();
            }
        }

        private void setupAutoTrackingPreference() {
            SwitchPreferenceCompat autoTrackingPref = findPreference("auto_tracking_enabled");
            if (autoTrackingPref != null) {
                autoTrackingPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    if (enabled) {
                        // 检查权限
                        if (!AccessibilityUtils.isAccessibilityServiceEnabled(getContext()) ||
                            !NotificationUtils.isNotificationListenerEnabled(getContext())) {
                            Toast.makeText(getContext(), "请先启用无障碍和通知权限", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    return true;
                });
            }
        }

        private void updateAccessibilityStatus() {
            Preference accessibilityPref = findPreference("accessibility_service");
            if (accessibilityPref != null) {
                boolean enabled = AccessibilityUtils.isAccessibilityServiceEnabled(getContext());
                accessibilityPref.setSummary(enabled ? "已启用" : "未启用 - 点击设置");
            }
        }

        private void updateNotificationStatus() {
            Preference notificationPref = findPreference("notification_access");
            if (notificationPref != null) {
                boolean enabled = NotificationUtils.isNotificationListenerEnabled(getContext());
                notificationPref.setSummary(enabled ? "已启用" : "未启用 - 点击设置");
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateAccessibilityStatus();
            updateNotificationStatus();
        }
    }
}
