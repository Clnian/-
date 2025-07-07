package com.autoexpense.tracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuickStatsAdapter extends RecyclerView.Adapter<QuickStatsAdapter.StatsViewHolder> {
    
    private Fragment fragment;
    private List<StatsItem> statsItems;
    private NumberFormat currencyFormat;
    
    public QuickStatsAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        this.statsItems = new ArrayList<>();
        initializeDefaultStats();
    }
    
    private void initializeDefaultStats() {
        statsItems.add(new StatsItem("本月收入", 0.0, R.color.income_color, "income"));
        statsItems.add(new StatsItem("本月支出", 0.0, R.color.expense_color, "expense"));
        statsItems.add(new StatsItem("本月结余", 0.0, R.color.primary_color, "balance"));
    }
    
    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_stats, parent, false);
        return new StatsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        StatsItem item = statsItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return statsItems.size();
    }
    
    public void updateStats(double income, double expense, double balance) {
        if (statsItems.size() >= 3) {
            statsItems.get(0).amount = income;
            statsItems.get(1).amount = expense;
            statsItems.get(2).amount = balance;
            notifyDataSetChanged();
        }
    }
    
    class StatsViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvTitle;
        private TextView tvAmount;
        private TextView tvChange;
        private CircularProgressIndicator progressIndicator;
        
        public StatsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.stats_card);
            tvTitle = itemView.findViewById(R.id.tv_stats_title);
            tvAmount = itemView.findViewById(R.id.tv_stats_amount);
            tvChange = itemView.findViewById(R.id.tv_stats_change);
            progressIndicator = itemView.findViewById(R.id.progress_indicator);
        }
        
        public void bind(StatsItem item) {
            tvTitle.setText(item.title);
            tvAmount.setText(currencyFormat.format(item.amount));
            
            // Set card background color
            cardView.setCardBackgroundColor(fragment.getResources().getColor(item.colorRes, null));
            
            // Set progress (mock data for now)
            int progress = (int) (Math.random() * 100);
            progressIndicator.setProgress(progress);
            
            // Set change percentage (mock data)
            double changePercent = (Math.random() - 0.5) * 20; // -10% to +10%
            String changeText = String.format(Locale.getDefault(), "%.1f%%", changePercent);
            tvChange.setText(changeText);
            
            if (changePercent >= 0) {
                tvChange.setTextColor(fragment.getResources().getColor(R.color.income_color, null));
            } else {
                tvChange.setTextColor(fragment.getResources().getColor(R.color.expense_color, null));
            }
        }
    }
    
    private static class StatsItem {
        String title;
        double amount;
        int colorRes;
        String type;
        
        StatsItem(String title, double amount, int colorRes, String type) {
            this.title = title;
            this.amount = amount;
            this.colorRes = colorRes;
            this.type = type;
        }
    }
}
