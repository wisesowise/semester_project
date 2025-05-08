package com.wise.semester_project.database;

import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.wise.semester_project.model.InventoryItem;

@Database(entities = {InventoryItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "inventory_db";
    private static volatile AppDatabase instance;

    public abstract InventoryDao inventoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            try {
                instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .allowMainThreadQueries() // 临时允许主线程查询，用于调试
                    .build();
                Log.d(TAG, "Database instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating database instance", e);
                throw e;
            }
        }
        return instance;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 由于我们目前只有一个版本，这个迁移是空的
            // 当需要升级数据库版本时，在这里添加迁移逻辑
        }
    };
} 