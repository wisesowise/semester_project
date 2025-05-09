package com.wise.semester_project.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.wise.semester_project.dao.InventoryDao;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.InventoryItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryRepository {
    private final InventoryDao inventoryDao;
    private final ExecutorService executorService;

    public InventoryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        inventoryDao = db.inventoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<InventoryItem>> getAllItems() {
        return inventoryDao.getAllItems();
    }

    public void insert(InventoryItem item) {
        executorService.execute(() -> inventoryDao.insert(item));
    }

    public void update(InventoryItem item) {
        executorService.execute(() -> inventoryDao.update(item));
    }

    public void delete(InventoryItem item) {
        executorService.execute(() -> inventoryDao.delete(item));
    }
} 