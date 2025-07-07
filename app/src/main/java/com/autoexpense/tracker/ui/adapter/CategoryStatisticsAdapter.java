package com.autoexpense.tracker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.ui.model.CategoryStatistic;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryStatisticsAdapter extends RecyclerView.Adapter<CategoryStatisticsAdapter.StatisticViewHolder> {
    private List<CategoryStatistic> statistics = new ArrayList<>();
    private Context context;
    private NumberFormat currencyFormat;

    public CategoryStatisticsAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_statistic, parent, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
        CategoryStatistic statistic = statistics.get(position);
        holder.bind(statistic);
    }

    @Override
    public int getItemCount() {
        return statistics.size();
    }

    public void setStatistics(List<CategoryStatistic> statistics) {
        this.statistics = statistics;
        notifyDataSetChanged();
    }

    class StatisticViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private TextView tvAmount;
        private TextView tvPercentage;
        private TextView tvCount;
        private ProgressBar progressBar;

        public StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            tvCount = itemView.findViewById(R.id.tv_count);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(CategoryStatistic statistic) {
            tvCategoryName.setText(statistic.getCategoryName());
            tvAmount.setText(currencyFormat.format(statistic.getAmount()));
            tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", statistic.getPercentage()));
            tvCount.setText(String.format(Locale.getDefault(), "%d笔", statistic.getCount()));
            
            // 设置进度条
            progressBar.setProgress((int) statistic.getPercentage());
        }
    }
}
