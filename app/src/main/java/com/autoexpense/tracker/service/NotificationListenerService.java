package com.autoexpense.tracker.service;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.autoexpense.tracker.data.database.AppDatabase;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.utils.TransactionParser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    private static final String TAG = "NotificationListener";
    
    private ExecutorService executor;
    private AppDatabase database;
    
    // 支持的应用包名和关键词
    private static final String[] BANK_PACKAGES = {
        "com.eg.android.AlipayGphone",  // 支付宝
        "com.tencent.mm",               // 微信
        "com.unionpay",                 // 银联
        "com.icbc",                     // 工商银行
        "com.ccb.ccbnetpay",           // 建设银行
        "com.abc.mobile.android",       // 农业银行
        "com.bankcomm.Bankcomm",       // 交通银行
        "cmb.pb",                      // 招商银行
        "com.chinamworld.bocmbci",     // 中国银行
        "com.cmbchina.ccd.pluto.cmbActivity", // 招商银行信用卡
        "com.pingan.paces.ccms",       // 平安银行
        "com.spdb.mobilebank.per",     // 浦发银行
        "com.citic.bank.mobile"        // 中信银行
    };
    
    private static final String[] TRANSACTION_KEYWORDS = {
        "支付成功", "付款成功", "转账成功", "收款成功",
        "消费", "支出", "收入", "入账", "到账",
        "余额", "交易", "扣款", "充值"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        Log.d(TAG, "通知监听服务已启动");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        
        // 只处理银行和支付应用的通知
        if (!isBankOrPaymentApp(packageName)) {
            return;
        }
        
        // 提取通知内容
        String title = getNotificationTitle(notification);
        String text = getNotificationText(notification);
        String bigText = getNotificationBigText(notification);
        
        // 合并所有文本内容
        StringBuilder fullText = new StringBuilder();
        if (title != null) fullText.append(title).append(" ");
        if (text != null) fullText.append(text).append(" ");
        if (bigText != null) fullText.append(bigText);
        
        String notificationContent = fullText.toString().trim();
        
        Log.d(TAG, "收到通知 - 应用: " + getAppName(packageName) + ", 内容: " + notificationContent);
        
        // 检查是否包含交易关键词
        if (containsTransactionKeywords(notificationContent)) {
            parseAndSaveTransaction(notificationContent, packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // 通知被移除时的处理（如果需要）
    }

    private boolean isBankOrPaymentApp(String packageName) {
        for (String bankPackage : BANK_PACKAGES) {
            if (bankPackage.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTransactionKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : TRANSACTION_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String getNotificationTitle(Notification notification) {
        Bundle extras = notification.extras;
        if (extras != null) {
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            return title != null ? title.toString() : null;
        }
        return null;
    }

    private String getNotificationText(Notification notification) {
        Bundle extras = notification.extras;
        if (extras != null) {
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            return text != null ? text.toString() : null;
        }
        return null;
    }

    private String getNotificationBigText(Notification notification) {
        Bundle extras = notification.extras;
        if (extras != null) {
            CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            return bigText != null ? bigText.toString() : null;
        }
        return null;
    }

    private String getAppName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void parseAndSaveTransaction(String notificationContent, String packageName) {
        executor.execute(() -> {
            try {
                Transaction transaction = TransactionParser.parseFromNotification(notificationContent, packageName);
                if (transaction != null) {
                    transaction.setAuto(true);
                    long id = database.transactionDao().insert(transaction);
                    Log.d(TAG, "从通知创建交易记录，ID: " + id + ", 内容: " + notificationContent);
                } else {
                    Log.d(TAG, "无法解析交易信息: " + notificationContent);
                }
            } catch (Exception e) {
                Log.e(TAG, "解析通知交易失败", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
        Log.d(TAG, "通知监听服务已销毁");
    }
}
