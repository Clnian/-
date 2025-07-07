package com.autoexpense.tracker.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.ui.AddTransactionActivity;
import com.autoexpense.tracker.ui.adapter.QuickStatsAdapter;
import com.autoexpense.tracker.ui.adapter.RecentTransactionsAdapter;
import com.autoexpense.tracker.ui.viewmodel.MainViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    
    private MainViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager2 statsViewPager;
    private DotsIndicator dotsIndicator;
    private RecyclerView recentTransactionsRecyclerView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private MaterialCardView balanceCard;
    private TextView tvBalance;
    private TextView tvBalanceChange;
    private FloatingActionButton fabAdd;
    
    private QuickStatsAdapter statsAdapter;
    private RecentTransactionsAdapter transactionsAdapter;
    private NumberFormat currencyFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSwipeRefresh();
        setupStatsViewPager();
        setupRecentTransactions();
        setupFab();
        observeData();
        
        // Start shimmer animation
        shimmerFrameLayout.startShimmer();
    }
    
    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        statsViewPager = view.findViewById(R.id.stats_view_pager);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        recentTransactionsRecyclerView = view.findViewById(R.id.recent_transactions_recycler_view);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_frame_layout);
        balanceCard = view.findViewById(R.id.balance_card);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvBalanceChange = view.findViewById(R.id.tv_balance_change);
        fabAdd = view.findViewById(R.id.fab_add);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.accent_color,
                R.color.income_color
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
        });
    }
    
    private void setupStatsViewPager() {
        statsAdapter = new QuickStatsAdapter(this);
        statsViewPager.setAdapter(statsAdapter);
        dotsIndicator.attachTo(statsViewPager);
        
        // Add page transformer for smooth transitions
        statsViewPager.setPageTransformer(new DepthPageTransformer());
    }
    
    private void setupRecentTransactions() {
        transactionsAdapter = new RecentTransactionsAdapter(requireContext());
        recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentTransactionsRecyclerView.setAdapter(transactionsAdapter);
        
        // Add item decoration for spacing
        recentTransactionsRecyclerView.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(
                        requireContext(),
                        androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
        );
    }
    
    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeData() {
        // Observe total income
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            double incomeValue = income != null ? income : 0.0;
            updateStatsData();
            hideShimmer();
        });
        
        // Observe total expense
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            updateStatsData();
            hideShimmer();
        });
        
        // Observe recent transactions
        viewModel.getRecentTransactions(10).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                transactionsAdapter.setTransactions(transactions);
                hideShimmer();
            }
        });
    }
    
    private void updateStatsData() {
        Double income = viewModel.getTotalIncome().getValue();
        Double expense = viewModel.getTotalExpense().getValue();
        
        double incomeValue = income != null ? income : 0.0;
        double expenseValue = expense != null ? expense : 0.0;
        double balance = incomeValue - expenseValue;
        
        // Update balance card
        tvBalance.setText(currencyFormat.format(balance));
        
        // Update balance change (mock data for now)
        tvBalanceChange.setText("+5.2%");
        tvBalanceChange.setTextColor(requireContext().getColor(R.color.income_color));
        
        // Update stats adapter
        statsAdapter.updateStats(incomeValue, expenseValue, balance);
    }
    
    private void refreshData() {
        // Simulate refresh delay
        swipeRefreshLayout.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
            // Trigger data refresh in ViewModel if needed
        }, 1500);
    }
    
    private void hideShimmer() {
        if (shimmerFrameLayout.isShimmerStarted()) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
    }
    
    // Custom page transformer for ViewPager2
    private static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) {
                view.setAlpha(0f);
            } else if (position <= 0) {
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) {
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                view.setTranslationZ(-1f);
                
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else {
                view.setAlpha(0f);
            }
        }
    }
}
