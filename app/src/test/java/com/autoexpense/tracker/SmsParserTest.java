package com.autoexpense.tracker;

import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.utils.SmsParser;

import org.junit.Test;
import static org.junit.Assert.*;

public class SmsParserTest {

    @Test
    public void testParseAlipayExpense() {
        String sender = "支付宝";
        String message = "您尾号1234的储蓄卡通过支付宝支出25.00元，余额1000.00元";
        
        Transaction transaction = SmsParser.parseTransactionFromSms(sender, message);
        
        assertNotNull(transaction);
        assertEquals(25.00, transaction.getAmount(), 0.01);
        assertEquals(Transaction.TransactionType.EXPENSE, transaction.getType());
        assertTrue(transaction.isAuto());
    }

    @Test
    public void testParseWechatExpense() {
        String sender = "微信支付";
        String message = "您尾号5678的银行卡微信支付支出50.00元";
        
        Transaction transaction = SmsParser.parseTransactionFromSms(sender, message);
        
        assertNotNull(transaction);
        assertEquals(50.00, transaction.getAmount(), 0.01);
        assertEquals(Transaction.TransactionType.EXPENSE, transaction.getType());
        assertTrue(transaction.isAuto());
    }

    @Test
    public void testParseBankIncome() {
        String sender = "95588";
        String message = "您的储蓄卡收入5000.00元，余额10000.00元";
        
        Transaction transaction = SmsParser.parseTransactionFromSms(sender, message);
        
        assertNotNull(transaction);
        assertEquals(5000.00, transaction.getAmount(), 0.01);
        assertEquals(Transaction.TransactionType.INCOME, transaction.getType());
        assertTrue(transaction.isAuto());
    }

    @Test
    public void testParseNonBankSms() {
        String sender = "10086";
        String message = "您的话费余额不足，请及时充值";
        
        Transaction transaction = SmsParser.parseTransactionFromSms(sender, message);
        
        assertNull(transaction);
    }

    @Test
    public void testParseInvalidAmount() {
        String sender = "支付宝";
        String message = "您的账户有新消息";
        
        Transaction transaction = SmsParser.parseTransactionFromSms(sender, message);
        
        assertNull(transaction);
    }
}
