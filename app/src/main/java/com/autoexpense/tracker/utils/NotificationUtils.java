package com.autoexpense.tracker.utils;

import android.content.Context;
import android.provider.Settings;

public class NotificationUtils {
    
    private static final String SERVICE_NAME = "com.autoexpense.tracker/com.autoexpense.tracker.service.NotificationListenerService";
    
    /**
     * 检查通知监听服务是否已启用
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        if (context == null) return false;
        
        String enabledListeners = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners");
        
        return enabledListeners != null && enabledListeners.contains(SERVICE_NAME);
    }
    
    /**
     * 获取通知监听服务的完整名称
     */
    public static String getServiceName() {
        return SERVICE_NAME;
    }
}
