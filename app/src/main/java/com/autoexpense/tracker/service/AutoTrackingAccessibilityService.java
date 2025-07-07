package com.autoexpense.tracker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.autoexpense.tracker.data.database.AppDatabase;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.utils.TransactionParser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoTrackingAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoTrackingService";
    
    private ExecutorService executor;
    private AppDatabase database;
    
    // 支持的应用包名
    private static final String[] SUPPORTED_PACKAGES = {
        "com.eg.android.AlipayGphone",  // 支付宝
        "com.tencent.mm",               // 微信
        "com.unionpay",                 // 银联
        "com.icbc",                     // 工商银行
        "com.ccb.ccbnetpay",           // 建设银行
        "com.abc.mobile.android",       // 农业银行
        "com.bankcomm.Bankcomm",       // 交通银行
        "cmb.pb",                      // 招商银行
        "com.chinamworld.bocmbci"      // 中国银行
    };

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        Log.d(TAG, "无障碍服务已启动");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
        
        // 只处理支持的应用
        if (!isSupportedPackage(packageName)) {
            return;
        }

        int eventType = event.getEventType();
        
        // 处理不同类型的事件
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                handleWindowEvent(event, packageName);
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotificationEvent(event, packageName);
                break;
        }
    }

    private boolean isSupportedPackage(String packageName) {
        for (String supportedPackage : SUPPORTED_PACKAGES) {
            if (supportedPackage.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void handleWindowEvent(AccessibilityEvent event, String packageName) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        try {
            // 根据不同应用解析交易信息
            switch (packageName) {
                case "com.eg.android.AlipayGphone":
                    parseAlipayTransaction(rootNode);
                    break;
                case "com.tencent.mm":
                    parseWeChatTransaction(rootNode);
                    break;
                default:
                    parseBankTransaction(rootNode, packageName);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析交易信息失败", e);
        } finally {
            rootNode.recycle();
        }
    }

    private void handleNotificationEvent(AccessibilityEvent event, String packageName) {
        CharSequence text = event.getText().toString();
        if (text != null) {
            String notificationText = text.toString();
            Log.d(TAG, "收到通知: " + notificationText + " 来自: " + packageName);
            
            // 解析通知中的交易信息
            parseNotificationTransaction(notificationText, packageName);
        }
    }

    private void parseAlipayTransaction(AccessibilityNodeInfo rootNode) {
        // 查找支付宝支付成功页面的关键元素
        String amount = findTextByKeywords(rootNode, "支付成功", "付款", "¥", "元");
        String merchant = findTextByKeywords(rootNode, "收款方", "商家");
        
        if (amount != null && !amount.isEmpty()) {
            createTransactionFromAccessibility("支付宝", amount, merchant, Transaction.TransactionType.EXPENSE);
        }
    }

    private void parseWeChatTransaction(AccessibilityNodeInfo rootNode) {
        // 查找微信支付成功页面的关键元素
        String amount = findTextByKeywords(rootNode, "支付成功", "转账", "¥", "元");
        String merchant = findTextByKeywords(rootNode, "收款方", "商户");
        
        if (amount != null && !amount.isEmpty()) {
            createTransactionFromAccessibility("微信支付", amount, merchant, Transaction.TransactionType.EXPENSE);
        }
    }

    private void parseBankTransaction(AccessibilityNodeInfo rootNode, String packageName) {
        // 通用银行应用交易解析
        String amount = findTextByKeywords(rootNode, "交易金额", "转账金额", "支付金额", "¥", "元");
        String type = findTextByKeywords(rootNode, "转入", "转出", "收入", "支出");
        
        if (amount != null && !amount.isEmpty()) {
            Transaction.TransactionType transactionType = 
                (type != null && (type.contains("转入") || type.contains("收入"))) 
                ? Transaction.TransactionType.INCOME 
                : Transaction.TransactionType.EXPENSE;
            
            createTransactionFromAccessibility("银行转账", amount, null, transactionType);
        }
    }

    private void parseNotificationTransaction(String notificationText, String packageName) {
        executor.execute(() -> {
            try {
                Transaction transaction = TransactionParser.parseFromNotification(notificationText, packageName);
                if (transaction != null) {
                    transaction.setAuto(true);
                    long id = database.transactionDao().insert(transaction);
                    Log.d(TAG, "从通知创建交易记录，ID: " + id);
                }
            } catch (Exception e) {
                Log.e(TAG, "解析通知交易失败", e);
            }
        });
    }

    private String findTextByKeywords(AccessibilityNodeInfo node, String... keywords) {
        if (node == null) return null;
        
        CharSequence text = node.getText();
        if (text != null) {
            String textStr = text.toString();
            for (String keyword : keywords) {
                if (textStr.contains(keyword)) {
                    return textStr;
                }
            }
        }
        
        // 递归查找子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                String result = findTextByKeywords(child, keywords);
                child.recycle();
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    private void createTransactionFromAccessibility(String source, String amountText, String merchant, Transaction.TransactionType type) {
        executor.execute(() -> {
            try {
                Transaction transaction = TransactionParser.parseFromAccessibility(source, amountText, merchant, type);
                if (transaction != null) {
                    transaction.setAuto(true);
                    long id = database.transactionDao().insert(transaction);
                    Log.d(TAG, "从无障碍服务创建交易记录，ID: " + id);
                }
            } catch (Exception e) {
                Log.e(TAG, "创建交易记录失败", e);
            }
        });
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
        Log.d(TAG, "无障碍服务已销毁");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                         AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;
        
        setServiceInfo(info);
        Log.d(TAG, "无障碍服务已连接");
    }
}
