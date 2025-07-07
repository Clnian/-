package com.autoexpense.tracker.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "type")
    private Transaction.TransactionType type;

    @ColumnInfo(name = "icon")
    private String icon; // 图标资源名称

    @ColumnInfo(name = "color")
    private String color; // 颜色代码

    @ColumnInfo(name = "is_default")
    private boolean isDefault; // 是否为默认分类

    // 构造函数
    public Category() {}

    public Category(String name, Transaction.TransactionType type, String icon, String color, boolean isDefault) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Transaction.TransactionType getType() {
        return type;
    }

    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
