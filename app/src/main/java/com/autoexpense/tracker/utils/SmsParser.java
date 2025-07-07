package com.autoexpense.tracker.utils;

import android.util.Log;

import com.autoexpense.tracker.data.entity.Transaction;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {
    private static final String TAG = "SmsParser";

    // 银行短信模式
    private static final Pattern[] EXPENSE_PATTERNS = {
            // 支付宝支付
            Pattern.compile("您尾号(\\d{4})的.*?支付宝.*?支出.*?(\\d+\\.\\d{2})元"),
            // 微信支付
            Pattern.compile("您尾号(\\d{4})的.*?微信.*?支出.*?(\\d+\\.\\d{2})元"),
            // 银行卡消费
            Pattern.compile("您尾号(\\d{4})的.*?消费.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("您的.*?卡.*?消费.*?(\\d+\\.\\d{2})元"),
            // 通用支出模式
            Pattern.compile("支出.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("消费.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("扣款.*?(\\d+\\.\\d{2})元")
    };

    private static final Pattern[] INCOME_PATTERNS = {
            // 收入模式
            Pattern.compile("您尾号(\\d{4})的.*?收入.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("您的.*?卡.*?收入.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("入账.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("转入.*?(\\d+\\.\\d{2})元"),
            Pattern.compile("存入.*?(\\d+\\.\\d{2})元")
    };

    // 商户名称提取模式
    private static final Pattern MERCHANT_PATTERN = Pattern.compile("在(.{2,20}?)消费");
    private static final Pattern ALIPAY_MERCHANT_PATTERN = Pattern.compile("支付宝-(.{2,20})");
    private static final Pattern WECHAT_MERCHANT_PATTERN = Pattern.compile("微信支付-(.{2,20})");

    public static Transaction parseTransactionFromSms(String sender, String messageBody) {
        if (messageBody == null || messageBody.trim().isEmpty()) {
            return null;
        }

        // 检查是否是银行或支付平台的短信
        if (!isBankOrPaymentSms(sender, messageBody)) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setDate(new Date());
        transaction.setAuto(true);

        // 尝试解析支出
        for (Pattern pattern : EXPENSE_PATTERNS) {
            Matcher matcher = pattern.matcher(messageBody);
            if (matcher.find()) {
                String amountStr = matcher.groupCount() >= 2 ? matcher.group(2) : matcher.group(1);
                try {
                    double amount = Double.parseDouble(amountStr);
                    transaction.setAmount(amount);
                    transaction.setType(Transaction.TransactionType.EXPENSE);
                    
                    // 尝试提取商户信息
                    String merchant = extractMerchant(messageBody);
                    transaction.setCategory(categorizeByMerchant(merchant));
                    transaction.setDescription("自动记账: " + (merchant != null ? merchant : "消费"));
                    
                    Log.d(TAG, "解析到支出交易: " + amount + "元, 商户: " + merchant);
                    return transaction;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "金额解析失败: " + amountStr, e);
                }
            }
        }

        // 尝试解析收入
        for (Pattern pattern : INCOME_PATTERNS) {
            Matcher matcher = pattern.matcher(messageBody);
            if (matcher.find()) {
                String amountStr = matcher.groupCount() >= 2 ? matcher.group(2) : matcher.group(1);
                try {
                    double amount = Double.parseDouble(amountStr);
                    transaction.setAmount(amount);
                    transaction.setType(Transaction.TransactionType.INCOME);
                    transaction.setCategory("其他");
                    transaction.setDescription("自动记账: 收入");
                    
                    Log.d(TAG, "解析到收入交易: " + amount + "元");
                    return transaction;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "金额解析失败: " + amountStr, e);
                }
            }
        }

        Log.d(TAG, "未能解析交易信息");
        return null;
    }

    private static boolean isBankOrPaymentSms(String sender, String messageBody) {
        // 检查发送方是否是银行或支付平台
        String[] bankKeywords = {"银行", "支付宝", "微信支付", "95588", "95533", "95599", "95555"};
        String[] messageKeywords = {"消费", "支出", "收入", "入账", "转账", "扣款"};

        boolean isBankSender = false;
        for (String keyword : bankKeywords) {
            if (sender.contains(keyword)) {
                isBankSender = true;
                break;
            }
        }

        boolean hasTransactionKeyword = false;
        for (String keyword : messageKeywords) {
            if (messageBody.contains(keyword)) {
                hasTransactionKeyword = true;
                break;
            }
        }

        return isBankSender && hasTransactionKeyword;
    }

    private static String extractMerchant(String messageBody) {
        // 尝试不同的商户提取模式
        Matcher matcher = MERCHANT_PATTERN.matcher(messageBody);
        if (matcher.find()) {
            return matcher.group(1);
        }

        matcher = ALIPAY_MERCHANT_PATTERN.matcher(messageBody);
        if (matcher.find()) {
            return matcher.group(1);
        }

        matcher = WECHAT_MERCHANT_PATTERN.matcher(messageBody);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static String categorizeByMerchant(String merchant) {
        if (merchant == null) {
            return "其他";
        }

        String merchantLower = merchant.toLowerCase();
        
        // 餐饮类
        if (merchantLower.contains("餐厅") || merchantLower.contains("饭店") || 
            merchantLower.contains("美食") || merchantLower.contains("咖啡") ||
            merchantLower.contains("奶茶") || merchantLower.contains("麦当劳") ||
            merchantLower.contains("肯德基") || merchantLower.contains("星巴克")) {
            return "餐饮";
        }
        
        // 交通类
        if (merchantLower.contains("加油") || merchantLower.contains("停车") ||
            merchantLower.contains("地铁") || merchantLower.contains("公交") ||
            merchantLower.contains("出租") || merchantLower.contains("滴滴")) {
            return "交通";
        }
        
        // 购物类
        if (merchantLower.contains("超市") || merchantLower.contains("商场") ||
            merchantLower.contains("淘宝") || merchantLower.contains("京东") ||
            merchantLower.contains("天猫") || merchantLower.contains("购物")) {
            return "购物";
        }
        
        // 娱乐类
        if (merchantLower.contains("电影") || merchantLower.contains("KTV") ||
            merchantLower.contains("游戏") || merchantLower.contains("娱乐")) {
            return "娱乐";
        }

        return "其他";
    }
}
