package com.autoexpense.tracker.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.autoexpense.tracker.data.dao.CategoryDao;
import com.autoexpense.tracker.data.dao.TransactionDao;
import com.autoexpense.tracker.data.entity.Category;
import com.autoexpense.tracker.data.entity.Transaction;
import com.autoexpense.tracker.data.converter.Converters;

@Database(
    entities = {Transaction.class, Category.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "expense_tracker_db";
    private static volatile AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .addCallback(new DatabaseCallback(context))
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // 数据库回调，用于初始化默认数据
    private static class DatabaseCallback extends RoomDatabase.Callback {
        private Context context;

        public DatabaseCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
            super.onCreate(db);
            // 在这里可以添加初始化默认分类的逻辑
            initializeDefaultCategories();
        }

        private void initializeDefaultCategories() {
            // 在后台线程中初始化默认分类
            new Thread(() -> {
                CategoryDao categoryDao = INSTANCE.categoryDao();
                
                // 支出分类
                categoryDao.insert(new Category("餐饮", Transaction.TransactionType.EXPENSE, "food", "#FF5722", true));
                categoryDao.insert(new Category("交通", Transaction.TransactionType.EXPENSE, "transport", "#2196F3", true));
                categoryDao.insert(new Category("购物", Transaction.TransactionType.EXPENSE, "shopping", "#E91E63", true));
                categoryDao.insert(new Category("娱乐", Transaction.TransactionType.EXPENSE, "entertainment", "#9C27B0", true));
                categoryDao.insert(new Category("医疗", Transaction.TransactionType.EXPENSE, "medical", "#F44336", true));
                categoryDao.insert(new Category("教育", Transaction.TransactionType.EXPENSE, "education", "#3F51B5", true));
                categoryDao.insert(new Category("其他", Transaction.TransactionType.EXPENSE, "other", "#607D8B", true));
                
                // 收入分类
                categoryDao.insert(new Category("工资", Transaction.TransactionType.INCOME, "salary", "#4CAF50", true));
                categoryDao.insert(new Category("奖金", Transaction.TransactionType.INCOME, "bonus", "#8BC34A", true));
                categoryDao.insert(new Category("投资", Transaction.TransactionType.INCOME, "investment", "#CDDC39", true));
                categoryDao.insert(new Category("其他", Transaction.TransactionType.INCOME, "other", "#FFC107", true));
            }).start();
        }
    }
}
