package com.autoexpense.tracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.autoexpense.tracker.data.dao.CategoryDao;
import com.autoexpense.tracker.data.dao.TransactionDao;
import com.autoexpense.tracker.data.database.AppDatabase;
import com.autoexpense.tracker.data.entity.Category;
import com.autoexpense.tracker.data.entity.Transaction;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<List<Category>> allCategories;
    private ExecutorService executor;

    public TransactionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        categoryDao = database.categoryDao();
        allTransactions = transactionDao.getAllTransactions();
        allCategories = categoryDao.getAllCategories();
        executor = Executors.newFixedThreadPool(2);
    }

    // Transaction operations
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type) {
        return transactionDao.getTransactionsByType(type);
    }

    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return transactionDao.getTransactionsByCategory(category);
    }

    public LiveData<List<Transaction>> getTransactionsByDateRange(Date startDate, Date endDate) {
        return transactionDao.getTransactionsByDateRange(startDate, endDate);
    }

    public LiveData<Double> getTotalByType(Transaction.TransactionType type) {
        return transactionDao.getTotalByType(type);
    }

    public LiveData<Double> getTotalByTypeAndDateRange(Transaction.TransactionType type, Date startDate, Date endDate) {
        return transactionDao.getTotalByTypeAndDateRange(type, startDate, endDate);
    }

    public LiveData<List<Transaction>> getRecentTransactions(int limit) {
        return transactionDao.getRecentTransactions(limit);
    }

    public void insert(Transaction transaction) {
        executor.execute(() -> transactionDao.insert(transaction));
    }

    public void update(Transaction transaction) {
        executor.execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        executor.execute(() -> transactionDao.delete(transaction));
    }

    public void deleteById(long id) {
        executor.execute(() -> transactionDao.deleteById(id));
    }

    // Category operations
    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getCategoriesByType(Transaction.TransactionType type) {
        return categoryDao.getCategoriesByType(type);
    }

    public LiveData<Category> getCategoryByName(String name) {
        return categoryDao.getCategoryByName(name);
    }

    public void insert(Category category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void update(Category category) {
        executor.execute(() -> categoryDao.update(category));
    }

    public void delete(Category category) {
        executor.execute(() -> categoryDao.delete(category));
    }
}
