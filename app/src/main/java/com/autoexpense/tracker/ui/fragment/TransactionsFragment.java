package com.autoexpense.tracker.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.ui.AddTransactionActivity;
import com.autoexpense.tracker.ui.adapter.TransactionAdapter;
import com.autoexpense.tracker.ui.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {
    
    private MainViewModel viewModel;
    private TransactionAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvNoTransactions;
    private FloatingActionButton fabAdd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSwipeRefresh();
        setupRecyclerView();
        setupFab();
        observeData();
    }
    
    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view_transactions);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);
        fabAdd = view.findViewById(R.id.fab_add);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.accent_color,
                R.color.income_color
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 模拟刷新
            swipeRefreshLayout.postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1500);
        });
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext());
        adapter.setOnTransactionClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeData() {
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                tvNoTransactions.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                tvNoTransactions.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Toast.makeText(requireContext(), "点击了交易: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        // TODO: 显示删除或编辑选项
        Toast.makeText(requireContext(), "长按了交易: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
    }
}
