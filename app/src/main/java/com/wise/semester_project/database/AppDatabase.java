package com.wise.semester_project.database;

import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.wise.semester_project.dao.InventoryDao;
import com.wise.semester_project.dao.SensorDao;
import com.wise.semester_project.dao.StorageAreaDao;
import com.wise.semester_project.dao.UserDao;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.model.Sensor;
import com.wise.semester_project.model.StorageArea;
import com.wise.semester_project.model.User;

@Database(entities = {InventoryItem.class, User.class, Sensor.class, StorageArea.class}, 
          version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "inventory_db";
    private static volatile AppDatabase instance;

    public abstract InventoryDao inventoryDao();
    public abstract UserDao userDao();
    public abstract SensorDao sensorDao();
    public abstract StorageAreaDao storageAreaDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建users表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `users` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`email` TEXT, " +
                "`password` TEXT, " +
                "`name` TEXT, " +
                "`createdAt` INTEGER NOT NULL" +
                ")"
            );
        }
    };
    
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建传感器表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `sensors` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sensorId` TEXT, " +
                "`type` TEXT, " +
                "`locationId` TEXT, " +
                "`temperature` REAL NOT NULL, " +
                "`humidity` REAL NOT NULL, " +
                "`lastUpdate` INTEGER NOT NULL" +
                ")"
            );
            
            // 创建仓库区域表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `storage_areas` (" +
                "`areaId` TEXT PRIMARY KEY NOT NULL, " +
                "`name` TEXT, " +
                "`description` TEXT, " +
                "`idealTemperature` REAL NOT NULL, " +
                "`idealHumidity` REAL NOT NULL, " +
                "`tempThreshold` REAL NOT NULL, " +
                "`humidityThreshold` REAL NOT NULL, " +
                "`sensorId` TEXT" +
                ")"
            );
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            try {
                instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
} 