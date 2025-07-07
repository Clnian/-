package com.autoexpense.tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.autoexpense.tracker.data.entity.Category;
import com.autoexpense.tracker.data.entity.Transaction;

import java.util.List;

@Dao
public interface CategoryDao {
    
    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(Transaction.TransactionType type);

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE name = :name")
    LiveData<Category> getCategoryByName(String name);

    @Query("SELECT * FROM categories WHERE is_default = 1")
    LiveData<List<Category>> getDefaultCategories();

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM categories")
    LiveData<Integer> getCategoryCount();
}
