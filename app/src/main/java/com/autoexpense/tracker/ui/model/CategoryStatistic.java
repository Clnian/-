package com.autoexpense.tracker.ui.model;

public class CategoryStatistic {
    private String categoryName;
    private double amount;
    private double percentage;
    private int count;

    public CategoryStatistic(String categoryName, double amount, double percentage, int count) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
        this.count = count;
    }

    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
