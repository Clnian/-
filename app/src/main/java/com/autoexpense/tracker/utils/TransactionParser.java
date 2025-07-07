package com.autoexpense.tracker.utils;

import android.util.Log;

import com.autoexpense.tracker.data.entity.Transaction;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionParser {
    private static final String TAG = "TransactionParser";
    
    // 金额匹配模式
    private static final Pattern[] AMOUNT_PATTERNS = {
        Pattern.compile("([0-9]+\\.?[0-9]*)元"),
        Pattern.compile("¥([0-9]+\\.?[0-9]*)"),
        Pattern.compile("([0-9]+\\.?[0-9]*)[元¥]"),
        Pattern.compile("金额[：:]?([0-9]+\\.?[0-9]*)"),
        Pattern.compile("([0-9]+\\.?[0-9]*)[\\s]*元"),
        Pattern.compile("RMB([0-9]+\\.?[0-9]*)")
    };
    
    // 支出关键词
    private static final String[] EXPENSE_KEYWORDS = {
        "支付成功", "付款成功", "消费", "支出", "扣款", "转出", "购买", "缴费"
    };
    
    // 收入关键词
    private static final String[] INCOME_KEYWORDS = {
        "收款成功", "到账", "入账", "收入", "转入", "退款", "返现", "红包"
    };
    
    // 商户名称提取模式
    private static final Pattern[] MERCHANT_PATTERNS = {
        Pattern.compile("在(.{2,20}?)消费"),
        Pattern.compile("向(.{2,20}?)付款"),
        Pattern.compile("收款方[：:]?(.{2,20})"),
        Pattern.compile("商户[：:]?(.{2,20})"),
        Pattern.compile("商家[：:]?(.{2,20})")
    };
    
    /**
     * 从通知内容解析交易信息
     */
    public static Transaction parseFromNotification(String content, String packageName) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        Log.d(TAG, "解析通知内容: " + content);
        
        // 提取金额
        double amount = extractAmount(content);
        if (amount <= 0) {
            Log.d(TAG, "未找到有效金额");
            return null;
        }
        
        // 判断交易类型
        Transaction.TransactionType type = determineTransactionType(content);
        
        // 提取商户信息
        String merchant = extractMerchant(content);
        
        // 根据应用包名确定来源
        String source = getSourceFromPackage(packageName);
        
        // 智能分类
        String category = categorizeTransaction(merchant, source, type);
        
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription(generateDescription(source, merchant, content));
        transaction.setDate(new Date());
        transaction.setAuto(true);
        
        Log.d(TAG, "解析成功 - 金额: " + amount + ", 类型: " + type + ", 分类: " + category);
        return transaction;
    }
    
    /**
     * 从无障碍服务解析交易信息
     */
    public static Transaction parseFromAccessibility(String source, String amountText, String merchant, Transaction.TransactionType type) {
        double amount = extractAmount(amountText);
        if (amount <= 0) {
            return null;
        }
        
        String category = categorizeTransaction(merchant, source, type);
        
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription("自动记账: " + source + (merchant != null ? " - " + merchant : ""));
        transaction.setDate(new Date());
        transaction.setAuto(true);
        
        return transaction;
    }
    
    private static double extractAmount(String text) {
        if (text == null) return 0;
        
        for (Pattern pattern : AMOUNT_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String amountStr = matcher.group(1);
                    return Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "金额解析失败: " + matcher.group(1));
                }
            }
        }
        return 0;
    }
    
    private static Transaction.TransactionType determineTransactionType(String content) {
        String lowerContent = content.toLowerCase();
        
        // 检查收入关键词
        for (String keyword : INCOME_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return Transaction.TransactionType.INCOME;
            }
        }
        
        // 检查支出关键词
        for (String keyword : EXPENSE_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return Transaction.TransactionType.EXPENSE;
            }
        }
        
        // 默认为支出
        return Transaction.TransactionType.EXPENSE;
    }
    
    private static String extractMerchant(String content) {
        for (Pattern pattern : MERCHANT_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }
    
    private static String getSourceFromPackage(String packageName) {
        switch (packageName) {
            case "com.eg.android.AlipayGphone":
                return "支付宝";
            case "com.tencent.mm":
                return "微信支付";
            case "com.unionpay":
                return "银联";
            case "com.icbc":
                return "工商银行";
            case "com.ccb.ccbnetpay":
                return "建设银行";
            case "com.abc.mobile.android":
                return "农业银行";
            case "com.bankcomm.Bankcomm":
                return "交通银行";
            case "cmb.pb":
                return "招商银行";
            case "com.chinamworld.bocmbci":
                return "中国银行";
            default:
                return "银行转账";
        }
    }
    
    private static String categorizeTransaction(String merchant, String source, Transaction.TransactionType type) {
        if (type == Transaction.TransactionType.INCOME) {
            return "其他";
        }
        
        if (merchant == null) {
            return "其他";
        }
        
        String merchantLower = merchant.toLowerCase();
        
        // 餐饮类
        if (merchantLower.contains("餐厅") || merchantLower.contains("饭店") || 
            merchantLower.contains("美食") || merchantLower.contains("咖啡") ||
            merchantLower.contains("奶茶") || merchantLower.contains("麦当劳") ||
            merchantLower.contains("肯德基") || merchantLower.contains("星巴克") ||
            merchantLower.contains("外卖") || merchantLower.contains("食堂")) {
            return "餐饮";
        }
        
        // 交通类
        if (merchantLower.contains("加油") || merchantLower.contains("停车") ||
            merchantLower.contains("地铁") || merchantLower.contains("公交") ||
            merchantLower.contains("出租") || merchantLower.contains("滴滴") ||
            merchantLower.contains("uber") || merchantLower.contains("高铁") ||
            merchantLower.contains("火车") || merchantLower.contains("飞机")) {
            return "交通";
        }
        
        // 购物类
        if (merchantLower.contains("超市") || merchantLower.contains("商场") ||
            merchantLower.contains("淘宝") || merchantLower.contains("京东") ||
            merchantLower.contains("天猫") || merchantLower.contains("购物") ||
            merchantLower.contains("便利店") || merchantLower.contains("商店")) {
            return "购物";
        }
        
        // 娱乐类
        if (merchantLower.contains("电影") || merchantLower.contains("ktv") ||
            merchantLower.contains("游戏") || merchantLower.contains("娱乐") ||
            merchantLower.contains("健身") || merchantLower.contains("运动")) {
            return "娱乐";
        }
        
        // 医疗类
        if (merchantLower.contains("医院") || merchantLower.contains("药店") ||
            merchantLower.contains("诊所") || merchantLower.contains("体检")) {
            return "医疗";
        }
        
        return "其他";
    }
    
    private static String generateDescription(String source, String merchant, String originalContent) {
        StringBuilder desc = new StringBuilder("自动记账: ");
        desc.append(source);
        
        if (merchant != null && !merchant.isEmpty()) {
            desc.append(" - ").append(merchant);
        }
        
        return desc.toString();
    }
}
