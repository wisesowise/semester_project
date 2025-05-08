package com.wise.semester_project;

import android.app.Application;
import android.util.Log;
import com.wise.semester_project.database.AppDatabase;

public class InventoryApplication extends Application {
    private static final String TAG = "InventoryApplication";
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate started");
        try {
            database = AppDatabase.getInstance(this);
            Log.d(TAG, "Database initialized successfully in Application");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database in Application", e);
        }
    }

    public AppDatabase getDatabase() {
        if (database == null) {
            Log.w(TAG, "Database is null when getDatabase() is called");
        }
        return database;
    }
} 