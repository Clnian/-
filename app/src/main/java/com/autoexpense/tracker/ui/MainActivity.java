package com.autoexpense.tracker.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.ui.adapter.TransactionAdapter;
import com.autoexpense.tracker.ui.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {
    private static final int REQUEST_SMS_PERMISSION = 1001;
    
    private MainViewModel viewModel;
    private TransactionAdapter adapter;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvBalance;
    private TextView tvNoTransactions;
    private RecyclerView recyclerView;
    private Spinner spinnerPeriod;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupPeriodSpinner();
        checkSmsPermission();

        currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }

    private void initViews() {
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        tvNoTransactions = findViewById(R.id.tv_no_transactions);
        recyclerView = findViewById(R.id.recycler_view_transactions);
        spinnerPeriod = findViewById(R.id.spinner_period);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_transaction);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        adapter.setOnTransactionClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 观察交易记录
        viewModel.getRecentTransactions(20).observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                tvNoTransactions.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                tvNoTransactions.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        // 观察统计数据
        viewModel.getTotalIncome().observe(this, income -> {
            double incomeValue = income != null ? income : 0.0;
            tvTotalIncome.setText(currencyFormat.format(incomeValue));
            updateBalance();
        });

        viewModel.getTotalExpense().observe(this, expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            tvTotalExpense.setText(currencyFormat.format(expenseValue));
            updateBalance();
        });
    }

    private void setupPeriodSpinner() {
        String[] periods = {"今天", "本周", "本月", "本年"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);
        spinnerPeriod.setSelection(2); // 默认选择"本月"

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = periods[position];
                viewModel.setSelectedPeriod(selectedPeriod);
                updateStatisticsByPeriod(selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateStatisticsByPeriod(String period) {
        viewModel.getTotalIncomeByPeriod(period).observe(this, income -> {
            double incomeValue = income != null ? income : 0.0;
            tvTotalIncome.setText(currencyFormat.format(incomeValue));
        });

        viewModel.getTotalExpenseByPeriod(period).observe(this, expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            tvTotalExpense.setText(currencyFormat.format(expenseValue));
        });

        viewModel.getTransactionsByPeriod(period).observe(this, transactions -> {
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

    private void updateBalance() {
        String incomeText = tvTotalIncome.getText().toString();
        String expenseText = tvTotalExpense.getText().toString();
        
        try {
            double income = parseAmount(incomeText);
            double expense = parseAmount(expenseText);
            double balance = income - expense;
            tvBalance.setText(currencyFormat.format(balance));
            
            // 设置余额颜色
            if (balance >= 0) {
                tvBalance.setTextColor(getColor(R.color.income_color));
            } else {
                tvBalance.setTextColor(getColor(R.color.expense_color));
            }
        } catch (Exception e) {
            tvBalance.setText(currencyFormat.format(0.0));
        }
    }

    private double parseAmount(String amountText) {
        try {
            return currencyFormat.parse(amountText).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) 
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    REQUEST_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO: 打开设置界面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // TODO: 打开交易详情或编辑界面
        Toast.makeText(this, "点击了交易: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("删除交易")
                .setMessage("确定要删除这条交易记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.delete(transaction);
                    Toast.makeText(this, "交易已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
