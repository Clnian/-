package com.autoexpense.tracker.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autoexpense.tracker.data.entity.Category;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.data.repository.TransactionRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private TransactionRepository repository;
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<List<Category>> allCategories;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpense;
    private MutableLiveData<String> selectedPeriod;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
        allCategories = repository.getAllCategories();
        totalIncome = repository.getTotalByType(Transaction.TransactionType.INCOME);
        totalExpense = repository.getTotalByType(Transaction.TransactionType.EXPENSE);
        selectedPeriod = new MutableLiveData<>("本月");
    }

    // Transaction operations
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type) {
        return repository.getTransactionsByType(type);
    }

    public LiveData<List<Transaction>> getRecentTransactions(int limit) {
        return repository.getRecentTransactions(limit);
    }

    public void insert(Transaction transaction) {
        repository.insert(transaction);
    }

    public void update(Transaction transaction) {
        repository.update(transaction);
    }

    public void delete(Transaction transaction) {
        repository.delete(transaction);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    // Category operations
    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getCategoriesByType(Transaction.TransactionType type) {
        return repository.getCategoriesByType(type);
    }

    // Statistics
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Double> getTotalIncomeByPeriod(String period) {
        Date[] dateRange = getDateRangeByPeriod(period);
        return repository.getTotalByTypeAndDateRange(Transaction.TransactionType.INCOME, dateRange[0], dateRange[1]);
    }

    public LiveData<Double> getTotalExpenseByPeriod(String period) {
        Date[] dateRange = getDateRangeByPeriod(period);
        return repository.getTotalByTypeAndDateRange(Transaction.TransactionType.EXPENSE, dateRange[0], dateRange[1]);
    }

    public LiveData<List<Transaction>> getTransactionsByPeriod(String period) {
        Date[] dateRange = getDateRangeByPeriod(period);
        return repository.getTransactionsByDateRange(dateRange[0], dateRange[1]);
    }

    // Period selection
    public MutableLiveData<String> getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(String period) {
        selectedPeriod.setValue(period);
    }

    // Helper methods
    private Date[] getDateRangeByPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date();
        Date startDate;

        switch (period) {
            case "今天":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
            case "本周":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
            case "本月":
            default:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
            case "本年":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
        }

        return new Date[]{startDate, endDate};
    }
}
