package com.autoexpense.tracker.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.autoexpense.tracker.R;
import com.autoexpense.tracker.data.entity.Category;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.ui.viewmodel.MainViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private RadioGroup radioGroupType;
    private TextInputEditText etAmount;
    private TextInputEditText etDescription;
    private Spinner spinnerCategory;
    private Button btnSelectDate;
    private Button btnSave;
    
    private Date selectedDate;
    private SimpleDateFormat dateFormat;
    private List<Category> expenseCategories = new ArrayList<>();
    private List<Category> incomeCategories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupToolbar();
        setupViewModel();
        setupDatePicker();
        setupCategorySpinner();
        setupSaveButton();

        selectedDate = new Date();
        dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        updateDateButton();
    }

    private void initViews() {
        radioGroupType = findViewById(R.id.radio_group_type);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 观察分类数据
        viewModel.getCategoriesByType(Transaction.TransactionType.EXPENSE).observe(this, categories -> {
            if (categories != null) {
                expenseCategories.clear();
                expenseCategories.addAll(categories);
                if (radioGroupType.getCheckedRadioButtonId() == R.id.radio_expense) {
                    updateCategorySpinner(expenseCategories);
                }
            }
        });

        viewModel.getCategoriesByType(Transaction.TransactionType.INCOME).observe(this, categories -> {
            if (categories != null) {
                incomeCategories.clear();
                incomeCategories.addAll(categories);
                if (radioGroupType.getCheckedRadioButtonId() == R.id.radio_income) {
                    updateCategorySpinner(incomeCategories);
                }
            }
        });
    }

    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        selectedDate = newDate.getTime();
                        updateDateButton();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_expense) {
                updateCategorySpinner(expenseCategories);
            } else if (checkedId == R.id.radio_income) {
                updateCategorySpinner(incomeCategories);
            }
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void updateDateButton() {
        btnSelectDate.setText(dateFormat.format(selectedDate));
    }

    private void updateCategorySpinner(List<Category> categories) {
        categoryAdapter.clear();
        for (Category category : categories) {
            categoryAdapter.add(category.getName());
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private void saveTransaction() {
        // 验证输入
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError(getString(R.string.error_amount_required));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError(getString(R.string.error_invalid_amount));
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, R.string.error_category_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建交易对象
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCategory(spinnerCategory.getSelectedItem().toString());
        transaction.setDescription(etDescription.getText().toString().trim());
        transaction.setDate(selectedDate);
        transaction.setAuto(false);

        // 设置交易类型
        if (radioGroupType.getCheckedRadioButtonId() == R.id.radio_income) {
            transaction.setType(Transaction.TransactionType.INCOME);
        } else {
            transaction.setType(Transaction.TransactionType.EXPENSE);
        }

        // 保存交易
        viewModel.insert(transaction);
        
        Toast.makeText(this, "交易已保存", Toast.LENGTH_SHORT).show();
        finish();
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
