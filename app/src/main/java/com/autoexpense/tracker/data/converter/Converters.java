package com.autoexpense.tracker.data.converter;

import androidx.room.TypeConverter;

import com.autoexpense.tracker.data.entity.Transaction;

import java.util.Date;

public class Converters {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromTransactionType(Transaction.TransactionType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Transaction.TransactionType toTransactionType(String type) {
        return type == null ? null : Transaction.TransactionType.valueOf(type);
    }
}
