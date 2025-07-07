package com.autoexpense.tracker.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class AccessibilityUtils {
    
    private static final String SERVICE_NAME = "com.autoexpense.tracker/.service.AutoTrackingAccessibilityService";
    
    /**
     * 检查无障碍服务是否已启用
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        if (context == null) return false;
        
        int accessibilityEnabled = 0;
        final String service = SERVICE_NAME;
        
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(settingValue);
                
                while (splitter.hasNext()) {
                    String accessibilityService = splitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取无障碍服务的完整名称
     */
    public static String getServiceName() {
        return SERVICE_NAME;
    }
}
