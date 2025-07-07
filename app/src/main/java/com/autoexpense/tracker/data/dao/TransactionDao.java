package com.autoexpense.tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.autoexpense.tracker.data.entity.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {
    
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<Transaction> getTransactionById(long id);

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type);

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    LiveData<Double> getTotalByType(Transaction.TransactionType type);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalByTypeAndDateRange(Transaction.TransactionType type, Date startDate, Date endDate);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByTypeAndDateRange(Transaction.TransactionType type, Date startDate, Date endDate);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM transactions")
    LiveData<Integer> getTransactionCount();

    @Query("SELECT * FROM transactions WHERE is_auto = 1 ORDER BY date DESC")
    LiveData<List<Transaction>> getAutoTransactions();

    // 获取最近的交易记录
    @Query("SELECT * FROM transactions ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(int limit);

    // 按月份统计
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND strftime('%Y-%m', date/1000, 'unixepoch') = :yearMonth")
    LiveData<Double> getMonthlyTotalByType(Transaction.TransactionType type, String yearMonth);
}
