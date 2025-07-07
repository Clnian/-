package com.autoexpense.tracker.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.data.entity.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions = new ArrayList<>();
    private Context context;
    private OnTransactionClickListener listener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        this.dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
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
        private TextView tvCategory;
        private TextView tvAmount;
        private TextView tvDescription;
        private TextView tvDate;
        private TextView tvAutoTag;
        private View viewCategoryColor;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAutoTag = itemView.findViewById(R.id.tv_auto_tag);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);

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

            // 设置金额和颜色
            double amount = transaction.getAmount();
            String amountText;
            int amountColor;

            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                amountText = "+" + currencyFormat.format(amount);
                amountColor = context.getColor(R.color.income_color);
                viewCategoryColor.setBackgroundColor(context.getColor(R.color.income_color));
            } else {
                amountText = "-" + currencyFormat.format(amount);
                amountColor = context.getColor(R.color.expense_color);
                viewCategoryColor.setBackgroundColor(context.getColor(R.color.expense_color));
            }

            tvAmount.setText(amountText);
            tvAmount.setTextColor(amountColor);

            // 显示自动记账标识
            if (transaction.isAuto()) {
                tvAutoTag.setVisibility(View.VISIBLE);
            } else {
                tvAutoTag.setVisibility(View.GONE);
            }
        }
    }
}
