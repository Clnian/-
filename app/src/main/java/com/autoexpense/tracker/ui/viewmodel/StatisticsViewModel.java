package com.autoexpense.tracker.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.data.repository.TransactionRepository;
import com.autoexpense.tracker.ui.model.CategoryStatistic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsViewModel extends AndroidViewModel {
    private TransactionRepository repository;
    private MutableLiveData<String> selectedPeriod;
    private LiveData<List<Transaction>> currentTransactions;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpense;
    private LiveData<List<CategoryStatistic>> categoryStatistics;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        selectedPeriod = new MutableLiveData<>("本月");
        
        // 根据选择的时间段获取数据
        currentTransactions = Transformations.switchMap(selectedPeriod, period -> {
            Date[] dateRange = getDateRangeByPeriod(period);
            return repository.getTransactionsByDateRange(dateRange[0], dateRange[1]);
        });
        
        // 计算总收入
        totalIncome = Transformations.switchMap(selectedPeriod, period -> {
            Date[] dateRange = getDateRangeByPeriod(period);
            return repository.getTotalByTypeAndDateRange(Transaction.TransactionType.INCOME, dateRange[0], dateRange[1]);
        });
        
        // 计算总支出
        totalExpense = Transformations.switchMap(selectedPeriod, period -> {
            Date[] dateRange = getDateRangeByPeriod(period);
            return repository.getTotalByTypeAndDateRange(Transaction.TransactionType.EXPENSE, dateRange[0], dateRange[1]);
        });
        
        // 计算分类统计
        categoryStatistics = Transformations.map(currentTransactions, this::calculateCategoryStatistics);
    }

    public LiveData<List<Transaction>> getCurrentTransactions() {
        return currentTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<CategoryStatistic>> getCategoryStatistics() {
        return categoryStatistics;
    }

    public MutableLiveData<String> getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setPeriod(String period) {
        selectedPeriod.setValue(period);
    }

    private List<CategoryStatistic> calculateCategoryStatistics(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, CategoryStatistic> categoryMap = new HashMap<>();
        double totalAmount = 0;

        // 计算每个分类的总金额
        for (Transaction transaction : transactions) {
            if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                totalAmount += amount;

                CategoryStatistic stat = categoryMap.get(category);
                if (stat == null) {
                    stat = new CategoryStatistic(category, amount, 0, 1);
                    categoryMap.put(category, stat);
                } else {
                    stat.setAmount(stat.getAmount() + amount);
                    stat.setCount(stat.getCount() + 1);
                }
            }
        }

        // 计算百分比
        List<CategoryStatistic> result = new ArrayList<>();
        for (CategoryStatistic stat : categoryMap.values()) {
            if (totalAmount > 0) {
                stat.setPercentage((stat.getAmount() / totalAmount) * 100);
            }
            result.add(stat);
        }

        // 按金额排序
        result.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        return result;
    }

    private Date[] getDateRangeByPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date();
        Date startDate;

        switch (period) {
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
            case "全部":
                calendar.set(2020, Calendar.JANUARY, 1);
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
