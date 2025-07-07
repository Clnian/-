package com.autoexpense.tracker.ui.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class StatisticsFragment extends Fragment {
    
    private StatisticsViewModel viewModel;
    private CategoryStatisticsAdapter adapter;
    private NumberFormat currencyFormat;
    
    // UI组件
    private TabLayout tabLayout;
    private MaterialCardView cardTotalIncome;
    private MaterialCardView cardTotalExpense;
    private MaterialCardView cardBalance;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvBalance;
    private CircularProgressIndicator progressIncome;
    private CircularProgressIndicator progressExpense;
    private RecyclerView recyclerViewCategories;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupTabLayout();
        observeData();
    }
    
    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        cardTotalIncome = view.findViewById(R.id.card_total_income);
        cardTotalExpense = view.findViewById(R.id.card_total_expense);
        cardBalance = view.findViewById(R.id.card_balance);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        progressIncome = view.findViewById(R.id.progress_income);
        progressExpense = view.findViewById(R.id.progress_expense);
        recyclerViewCategories = view.findViewById(R.id.recycler_view_categories);
    }
    
    private void setupRecyclerView() {
        adapter = new CategoryStatisticsAdapter(requireContext());
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCategories.setAdapter(adapter);
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
    
    private void observeData() {
        // 观察总收入
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            double incomeValue = income != null ? income : 0.0;
            animateValue(tvTotalIncome, incomeValue);
            updateProgressIndicator(progressIncome, incomeValue, viewModel.getTotalExpense().getValue());
        });
        
        // 观察总支出
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            animateValue(tvTotalExpense, expenseValue);
            updateProgressIndicator(progressExpense, expenseValue, viewModel.getTotalIncome().getValue());
            
            // 计算余额
            Double income = viewModel.getTotalIncome().getValue();
            double incomeValue = income != null ? income : 0.0;
            double balance = incomeValue - expenseValue;
            animateValue(tvBalance, balance);
        });
        
        // 观察分类统计
        viewModel.getCategoryStatistics().observe(getViewLifecycleOwner(), statistics -> {
            if (statistics != null) {
                adapter.setStatistics(statistics);
            }
        });
    }
    
    private void animateValue(TextView textView, double value) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) value);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            textView.setText(currencyFormat.format(animatedValue));
        });
        
        animator.start();
    }
    
    private void updateProgressIndicator(CircularProgressIndicator progress, Double currentValue, Double totalValue) {
        if (currentValue == null || totalValue == null || (currentValue + totalValue) == 0) {
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
}
