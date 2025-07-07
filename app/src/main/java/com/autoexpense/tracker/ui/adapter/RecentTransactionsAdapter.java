package com.autoexpense.tracker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.data.entity.Transaction;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecentTransactionsAdapter extends RecyclerView.Adapter<RecentTransactionsAdapter.TransactionViewHolder> {
    
    private List<Transaction> transactions = new ArrayList<>();
    private Context context;
    private NumberFormat currencyFormat;
    private SimpleDateFormat timeFormat;
    private Map<String, Integer> categoryIcons;
    
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }
    
    private OnTransactionClickListener listener;
    
    public RecentTransactionsAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        initializeCategoryIcons();
    }
    
    private void initializeCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("餐饮", R.drawable.ic_restaurant);
        categoryIcons.put("交通", R.drawable.ic_directions_car);
        categoryIcons.put("购物", R.drawable.ic_shopping_cart);
        categoryIcons.put("娱乐", R.drawable.ic_movie);
        categoryIcons.put("医疗", R.drawable.ic_local_hospital);
        categoryIcons.put("教育", R.drawable.ic_school);
        categoryIcons.put("其他", R.drawable.ic_category);
    }
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_enhanced, parent, false);
        return new TransactionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }
    
    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }
    
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }
    
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCategoryIcon;
        private TextView tvCategory;
        private TextView tvAmount;
        private TextView tvDescription;
        private TextView tvDate;
        private Chip chipAuto;
        private ImageView ivSourceIcon;
        
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            chipAuto = itemView.findViewById(R.id.chip_auto);
            ivSourceIcon = itemView.findViewById(R.id.iv_source_icon);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick(transactions.get(getAdapterPosition()));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTransactionLongClick(transactions.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Transaction transaction) {
            tvCategory.setText(transaction.getCategory());
            tvDescription.setText(transaction.getDescription());
            tvDate.setText(timeFormat.format(transaction.getDate()));
            
            // Set category icon
            Integer iconRes = categoryIcons.get(transaction.getCategory());
            if (iconRes != null) {
                ivCategoryIcon.setImageResource(iconRes);
            } else {
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
            }
            
            // Set amount and color
            double amount = transaction.getAmount();
            String amountText;
            int amountColor;
            
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                amountText = "+" + currencyFormat.format(amount);
                amountColor = context.getColor(R.color.income_color);
            } else {
                amountText = "-" + currencyFormat.format(amount);
                amountColor = context.getColor(R.color.expense_color);
            }
            
            tvAmount.setText(amountText);
            tvAmount.setTextColor(amountColor);
            
            // Show auto tag if transaction is automatic
            if (transaction.isAuto()) {
                chipAuto.setVisibility(View.VISIBLE);
                ivSourceIcon.setVisibility(View.VISIBLE);
            } else {
                chipAuto.setVisibility(View.GONE);
                ivSourceIcon.setVisibility(View.GONE);
            }
        }
    }
}
