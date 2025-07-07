package com.autoexpense.tracker.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.ui.adapter.CategoryStatisticsAdapter;
import com.autoexpense.tracker.ui.viewmodel.StatisticsViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    
    private StatisticsViewModel viewModel;
    private CategoryStatisticsAdapter adapter;
    private NumberFormat currencyFormat;
    
    // UI组件
    private TabLayout tabLayout;
    private MaterialCardView cardTotalIncome;
    private MaterialCardView cardTotalExpense;
    private MaterialCardView cardBalance;
    private CircularProgressIndicator progressIncome;
    private CircularProgressIndicator progressExpense;
    private RecyclerView recyclerViewCategories;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        
        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupTabLayout();
        
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }
    
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        cardTotalIncome = findViewById(R.id.card_total_income);
        cardTotalExpense = findViewById(R.id.card_total_expense);
        cardBalance = findViewById(R.id.card_balance);
        progressIncome = findViewById(R.id.progress_income);
        progressExpense = findViewById(R.id.progress_expense);
        recyclerViewCategories = findViewById(R.id.recycler_view_categories);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("统计分析");
        }
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        
        // 观察统计数据
        viewModel.getTotalIncome().observe(this, income -> {
            double incomeValue = income != null ? income : 0.0;
            animateCardValue(cardTotalIncome, incomeValue, true);
            updateProgressIndicator(progressIncome, incomeValue, viewModel.getTotalExpense().getValue());
        });
        
        viewModel.getTotalExpense().observe(this, expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            animateCardValue(cardTotalExpense, expenseValue, false);
            updateProgressIndicator(progressExpense, expenseValue, viewModel.getTotalIncome().getValue());
        });
        
        // 观察分类统计
        viewModel.getCategoryStatistics().observe(this, statistics -> {
            if (statistics != null) {
                adapter.setStatistics(statistics);
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new CategoryStatisticsAdapter(this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(adapter);
        
        // 添加动画
        recyclerViewCategories.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }
    
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("本月"));
        tabLayout.addTab(tabLayout.newTab().setText("本年"));
        tabLayout.addTab(tabLayout.newTab().setText("全部"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String period = tab.getText().toString();
                viewModel.setPeriod(period);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void animateCardValue(MaterialCardView card, double value, boolean isIncome) {
        // 找到卡片中的金额TextView并添加动画
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) value);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            // 更新UI显示
            updateCardDisplay(card, animatedValue, isIncome);
        });
        
        animator.start();
    }
    
    private void updateCardDisplay(MaterialCardView card, float value, boolean isIncome) {
        // 这里需要根据实际的布局来更新显示
        // 示例代码，需要根据实际布局调整
    }
    
    private void updateProgressIndicator(CircularProgressIndicator progress, Double currentValue, Double totalValue) {
        if (currentValue == null || totalValue == null || totalValue == 0) {
            progress.setProgress(0);
            return;
        }
        
        int progressValue = (int) ((currentValue / (currentValue + totalValue)) * 100);
        
        ValueAnimator animator = ValueAnimator.ofInt(progress.getProgress(), progressValue);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            progress.setProgress((Integer) animation.getAnimatedValue());
        });
        animator.start();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
