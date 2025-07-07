package com.autoexpense.tracker.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "type")
    private TransactionType type; // INCOME or EXPENSE

    @ColumnInfo(name = "is_auto")
    private boolean isAuto; // 是否为自动记账

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    // 构造函数
    public Transaction() {
        this.createdAt = new Date();
    }

    public Transaction(double amount, String category, String description, Date date, TransactionType type) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
        this.isAuto = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // 枚举类型
    public enum TransactionType {
        INCOME, EXPENSE
    }
}
