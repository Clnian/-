package com.autoexpense.tracker.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.utils.AccessibilityUtils;
import com.autoexpense.tracker.utils.NotificationUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupAccessibilityPreference();
        setupNotificationPreference();
        setupAutoTrackingPreference();
        setupDataManagementPreferences();
        setupAboutPreferences();
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

            updateNotificationStatus();
        }
    }

    private void setupAutoTrackingPreference() {
        SwitchPreferenceCompat autoTrackingPref = findPreference("auto_tracking_enabled");
        if (autoTrackingPref != null) {
            autoTrackingPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                if (enabled) {
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

    private void setupDataManagementPreferences() {
        Preference exportPref = findPreference("export_data");
        if (exportPref != null) {
            exportPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "导出功能开发中...", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        Preference importPref = findPreference("import_data");
        if (importPref != null) {
            importPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "导入功能开发中...", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        Preference clearPref = findPreference("clear_data");
        if (clearPref != null) {
            clearPref.setOnPreferenceClickListener(preference -> {
                // TODO: 显示确认对话框
                Toast.makeText(getContext(), "清除数据功能开发中...", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    private void setupAboutPreferences() {
        Preference versionPref = findPreference("app_version");
        if (versionPref != null) {
            try {
                String versionName = requireContext().getPackageManager()
                        .getPackageInfo(requireContext().getPackageName(), 0).versionName;
                versionPref.setSummary(versionName);
            } catch (Exception e) {
                versionPref.setSummary("1.0.0");
            }
        }

        Preference privacyPref = findPreference("privacy_policy");
        if (privacyPref != null) {
            privacyPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "隐私政策功能开发中...", Toast.LENGTH_SHORT).show();
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
