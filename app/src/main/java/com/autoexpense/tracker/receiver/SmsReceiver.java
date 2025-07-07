package com.autoexpense.tracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.autoexpense.tracker.data.database.AppDatabase;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.utils.SmsParser;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        
                        Log.d(TAG, "收到短信 - 发送方: " + sender + ", 内容: " + messageBody);
                        
                        // 解析短信并创建交易记录
                        parseSmsAndCreateTransaction(context, sender, messageBody);
                    }
                }
            }
        }
    }

    private void parseSmsAndCreateTransaction(Context context, String sender, String messageBody) {
        executor.execute(() -> {
            try {
                Transaction transaction = SmsParser.parseTransactionFromSms(sender, messageBody);
                if (transaction != null) {
                    AppDatabase database = AppDatabase.getInstance(context);
                    long id = database.transactionDao().insert(transaction);
                    Log.d(TAG, "自动创建交易记录，ID: " + id);
                }
            } catch (Exception e) {
                Log.e(TAG, "解析短信失败", e);
            }
        });
    }
}
